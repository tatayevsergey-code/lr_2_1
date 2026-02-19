import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;

public class OrganizationXmlHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Сохранение очереди в XML-файл
     */
    public static void saveQueue(PriorityQueue<Organization> queue, String filename) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("organizations");
        doc.appendChild(root);

        for (Organization org : queue) {
            Element orgElement = doc.createElement("organization");

            //appendElement(doc, orgElement, "id", String.valueOf(org.getId()));
            appendElement(doc, orgElement, "name", org.getName());

            // Coordinates
            Element coordsElement = doc.createElement("coordinates");
            appendElement(doc, coordsElement, "x", String.valueOf(org.getCoordinates().getX()));
            appendElement(doc, coordsElement, "y", String.valueOf(org.getCoordinates().getY()));
            orgElement.appendChild(coordsElement);

            // CreationDate
            appendElement(doc, orgElement, "creationDate",
                    org.getCreationDate() != null ? org.getCreationDate().format(DATE_FORMATTER) : "");

            // AnnualTurnover
            appendElement(doc, orgElement, "annualTurnover", String.valueOf(org.getAnnualTurnover()));

            // Type
            appendElement(doc, orgElement, "type", org.getType() != null ? org.getType().name() : "");

            // Address
            Element addressElement = doc.createElement("officialAddress");
            appendElement(doc, addressElement, "street", org.getOfficialAddress().getStreet());
            appendElement(doc, addressElement, "zipCode", org.getOfficialAddress().getZipCode());
            orgElement.appendChild(addressElement);

            root.appendChild(orgElement);
        }

        // Запись в файл
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        try (FileWriter writer = new FileWriter(filename)) {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        }
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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList orgList = doc.getElementsByTagName("organization");

        for (int i = 0; i < orgList.getLength(); i++) {
            Element orgElement = (Element) orgList.item(i);

            //long id = Long.parseLong(getElementValue(orgElement, "id"));
            String name = getElementValue(orgElement, "name");
            float annualTurnover = Float.parseFloat(getElementValue(orgElement, "annualTurnover"));
            String typeName = getElementValue(orgElement, "type");
            OrganizationType type = OrganizationType.valueOf(typeName);

            // Coordinates
            Element coordsElement = (Element) orgElement.getElementsByTagName("coordinates").item(0);
            long x = Long.parseLong(getElementValue(coordsElement, "x"));
            long y = Long.parseLong(getElementValue(coordsElement, "y"));
            Coordinates coordinates = new Coordinates(x, y);

            // CreationDate
            String dateStr = getElementValue(orgElement, "creationDate");
            LocalDate creationDate = (dateStr != null && !dateStr.isEmpty())
                    ? LocalDate.parse(dateStr, DATE_FORMATTER)
                    : LocalDate.now();

            // Address
            Element addressElement = (Element) orgElement.getElementsByTagName("officialAddress").item(0);
            String street = getElementValue(addressElement, "street");
            String zipCode = getElementValue(addressElement, "zipCode");
            Address officialAddress = new Address(street, zipCode);

            // Создание организации
            Organization org = new Organization( name, coordinates, creationDate,
                    annualTurnover, type, officialAddress);
            queue.add(org);
        }

        return queue;
    }

    private static void appendElement(Document doc, Element parent, String tagName, String value) {
        Element element = doc.createElement(tagName);
        element.setTextContent(value != null ? value : "");
        parent.appendChild(element);
    }

    private static String getElementValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }
}
