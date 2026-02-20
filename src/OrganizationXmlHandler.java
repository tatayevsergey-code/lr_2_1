import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganizationXmlHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    /**
     * Сохранение очереди в XML-файл
     */
    public static void saveQueue(PriorityQueue<Organization> queue, String filename) throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            // XML Declaration
            bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bufferedWriter.newLine();

            // Root element
            bufferedWriter.write("<organizations>");
            bufferedWriter.newLine();

            for (Organization org : queue) {
                bufferedWriter.write("  <organization>");
                bufferedWriter.newLine();

                // ID
                writeTag(bufferedWriter, "id", String.valueOf(org.getId()), 4);

                // Name
                writeTag(bufferedWriter, "name", escapeXml(org.getName()), 4);

                // Coordinates
                bufferedWriter.write("    <coordinates>");
                bufferedWriter.newLine();
                writeTag(bufferedWriter, "x", String.valueOf(org.getCoordinates().getX()), 6);
                writeTag(bufferedWriter, "y", String.valueOf(org.getCoordinates().getY()), 6);
                bufferedWriter.write("    </coordinates>");
                bufferedWriter.newLine();

                // CreationDate
                String dateStr = org.getCreationDate() != null
                        ? org.getCreationDate().format(DATE_FORMATTER)
                        : "";
                writeTag(bufferedWriter, "creationDate", dateStr, 4);

                // AnnualTurnover
                writeTag(bufferedWriter, "annualTurnover", String.valueOf(org.getAnnualTurnover()), 4);

                // Type
                writeTag(bufferedWriter, "type", org.getType() != null ? org.getType().name() : "", 4);

                // Address
                bufferedWriter.write("    <officialAddress>");
                bufferedWriter.newLine();
                writeTag(bufferedWriter, "street", escapeXml(org.getOfficialAddress().getStreet()), 6);
                writeTag(bufferedWriter, "zipCode", escapeXml(org.getOfficialAddress().getZipCode()), 6);
                bufferedWriter.write("    </officialAddress>");
                bufferedWriter.newLine();

                bufferedWriter.write("  </organization>");
                bufferedWriter.newLine();
            }

            bufferedWriter.write("</organizations>");
            bufferedWriter.newLine();
        }
    }

    // Запись отдельного тега с отступом
    private static void writeTag(BufferedWriter writer, String tagName, String value, int indent) throws IOException {
        writer.write(" ".repeat(indent));
        writer.write("<");
        writer.write(tagName);
        writer.write(">");
        writer.write(value != null ? value : "");
        writer.write("</");
        writer.write(tagName);
        writer.write(">");
        writer.newLine();
    }

    // Кодирование XML-спецсимволов
    private static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Загрузка очереди из XML-файла
     */
    public static PriorityQueue<Organization> loadQueue(String filename) throws Exception {
        PriorityQueue<Organization> queue = new PriorityQueue<>(
                java.util.Comparator.comparingLong(Organization::getId)
        );

        File file = new File(filename);
        if (!file.exists()) {
            return queue;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            StringBuilder xmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line);
            }

            String xml = xmlContent.toString();

            // Поиск всех блоков <organization>...</organization>
            Pattern orgPattern = Pattern.compile("<organization>(.*?)</organization>", Pattern.DOTALL);
            Matcher orgMatcher = orgPattern.matcher(xml);

            while (orgMatcher.find()) {
                String orgBlock = orgMatcher.group(1);

                try {
                    long id = Long.parseLong(getTagValue(orgBlock, "id"));
                    String name = unescapeXml(getTagValue(orgBlock, "name"));
                    float annualTurnover = Float.parseFloat(getTagValue(orgBlock, "annualTurnover"));
                    String typeName = getTagValue(orgBlock, "type");
                    OrganizationType type = OrganizationType.valueOf(typeName);

                    // Coordinates
                    String coordsBlock = getTagContent(orgBlock, "coordinates");
                    long x = Long.parseLong(getTagValue(coordsBlock, "x"));
                    long y = Long.parseLong(getTagValue(coordsBlock, "y"));
                    Coordinates coordinates = new Coordinates(x, y);

                    // CreationDate
                    String dateStr = getTagValue(orgBlock, "creationDate");
                    LocalDate creationDate = (dateStr != null && !dateStr.isEmpty())
                            ? LocalDate.parse(dateStr, DATE_FORMATTER)
                            : LocalDate.now();

                    // Address
                    String addressBlock = getTagContent(orgBlock, "officialAddress");
                    String street = unescapeXml(getTagValue(addressBlock, "street"));
                    String zipCode = unescapeXml(getTagValue(addressBlock, "zipCode"));
                    Address officialAddress = new Address(street, zipCode);

                    Organization org = new Organization(name, coordinates, creationDate,
                            annualTurnover, type, officialAddress);
                    org.setId(id);
                    queue.add(org);

                } catch (Exception e) {
                    System.err.println("Ошибка парсинга организации: " + e.getMessage());
                }
            }
        }

        return queue;
    }

    // Получить содержимое тега (например, <id>123</id> -> "123")
    private static String getTagValue(String xml, String tagName) {
        String regex = "<" + tagName + ">(.*?)</" + tagName + ">";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    // Получить содержимое блока с вложенными тегами
    private static String getTagContent(String xml, String tagName) {
        String regex = "<" + tagName + ">(.*?)</" + tagName + ">";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    // Декодирование XML-спецсимволов
    private static String unescapeXml(String str) {
        if (str == null) return "";
        return str.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }

}
