import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Collection {
    static PriorityQueue<Organization> queue;

    public static void main(String[] args) throws Exception {

        queue = new PriorityQueue<>(
                Comparator.comparingLong(Organization::getId)
        );
        CommandHistory commandHistory = new CommandHistory(6); // ← История команд

        String xmlFilename = System.getenv("XML_FILENAME");
        //System.out.println(xmlFilename);

        // Загрузка
        if(xmlFilename != null && !xmlFilename.isEmpty()) {
            PriorityQueue<Organization> loadedQueue = OrganizationXmlHandler.loadQueue(/*"organizations.xml"*/xmlFilename);
            System.out.println("✅ Данные загружены, размер: " + loadedQueue.size());

            while (!loadedQueue.isEmpty()) {
                Organization org = loadedQueue.poll();
                queue.offer(org);
            }
        }
        else{
            System.out.println("Имя файла для хранения данных не задано. Данные не загружены");
        }
        //loadedQueue.forEach(System.out::println);

        List<String> script_cmds = null;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Введи команду, собака:");
            String cmd = "";
            if(script_cmds != null && !script_cmds.isEmpty()){
                cmd = script_cmds.getFirst();
                script_cmds.removeFirst();
            }
            else {
                cmd = scanner.nextLine();
                commandHistory.add(cmd); // ← Сохраняем команду
            }
            //такая дебильная обработка потому, что внутри аргументов команды может быть пробел
            String[] cmd_args = new String[3];// = cmd.split(" ");
            if(cmd.trim().contains(" ")) {
                cmd_args[0] = cmd.substring(0, cmd.indexOf(" "));

                cmd = cmd.substring(cmd.indexOf(" ") + 1);
                if(cmd.startsWith("{")) cmd_args[1] = cmd;
                else{
                    if(cmd.contains(" ")) {
                        cmd_args[1] = cmd.substring(0, cmd.indexOf(" "));
                        cmd_args[2] = cmd.substring(cmd.indexOf(" ") + 1);
                    }
                    else {
                        cmd_args[1] = cmd;
                    }
                }

                //System.out.println(Arrays.toString(cmd_args));

                //cmd_args[1] = cmd.substring(cmd.indexOf(" ") + 1);
            }
            else {
                cmd_args[0] = cmd.trim();
            }
            switch (cmd_args[0]) {
                case "help" -> System.out.println("""
                        Доступные аргументы:
                        help - вывод справки по доступным командам
                        info - вывод информации о коллекции
                        show - вывод всех элементов коллекции
                        add {element} - добавление элемента в коллекцию
                        update id {element} - обновление значения элемента коллекции по id
                        remove_by_id id - удаление элемента из коллекции по id
                        clear - очистка коллекции
                        save - сохранение коллекции в файл
                        execute_script file_name - считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
                        remove_head - вывести первый элемент коллекции и удалить его
                        remove_greater {элемент} - удалить из коллекции все элементы превышающие заданный
                        history - вывести последние 6 команд
                        remove_any_by_official_address officialAddress - удалить из коллекции один элемент, значение поля officialAddress которого эквивалентно заданному
                        print_descending - вывести элементы коллекции в порядке убывания
                        print_field_descending_type - вывести значения поля type всех элементов в порядке убывания 
                        exit - завершение программы (без сохранения в файл)
                        """);
                case "info" -> {
                    System.out.println("Вывод информации о коллекции");
                    getInfo();
                }
                case "show" -> {
                    System.out.println("Вывод всех элементов коллекции");
                    printQueue(queue,false,false);
                }
                case "add" -> {
                    System.out.println("Добавление элемента в коллекцию");
                    try {
                        if (cmd_args.length < 2 || !cmd_args[1].startsWith("{") || !cmd_args[1].endsWith("}") || !cmd_args[1].contains(";")){
                            throw new IllegalArgumentException();
                        }

                        List<String> parts = parseAddCommand(cmd_args[1]);

//                        System.out.println("Разделённые части:");
//                        for (int i = 0; i < parts.size(); i++) {
//                            System.out.println("[" + i + "] = " + parts.get(i));
//                        }

                        if (parts.toArray().length != 5) {
                            throw new IllegalArgumentException();
                        }
                        Organization org = null;
                        try {
                            org = new Organization(parts.get(0),                            //имя
                                    parts.get(1).substring(1, parts.get(1).length() - 1),   //координаты
                                    Float.parseFloat(parts.get(2)),                         //годовой оборот
                                    parts.get(3),                                           //тип
                                    parts.get(4).substring(1, parts.get(4).length() - 1));  //адрес
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }
                        if (org != null && org.valid) {

                                System.out.println(org);
                                queue.offer(org); // или queue.add(org) — выбросит исключение при ошибке
                                printQueue(queue,false,false);
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат. Формат аргумента {наименование;{x;y};годовой_оборот;тип;{адрес;индекс}}");
                        continue;
                    }
                }
                case "update" -> {
                    System.out.println("Обновление элемента коллекции по идентификатору " + cmd_args[1]);
                    try {
                        if (cmd_args.length != 3) {
                            throw new IllegalArgumentException();
                        }

                        long id = Long.parseLong(cmd_args[1].trim());
                        queue.removeIf(org -> org.getId() == id);

                        List<String> parts = parseAddCommand(cmd_args[2]);

                        if (parts.toArray().length != 5) {
                            throw new IllegalArgumentException();
                        }
                        Organization org = null;
                        try {
                            org = new Organization(parts.get(0),                            //имя
                                    parts.get(1).substring(1, parts.get(1).length() - 1),   //координаты
                                    Float.parseFloat(parts.get(2)),                         //годовой оборот
                                    parts.get(3),                                           //тип
                                    parts.get(4).substring(1, parts.get(4).length() - 1));  //адрес
                            org.setId(id);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }
                        if (org != null && org.valid) {

                            System.out.println(org);
                            queue.offer(org); // или queue.add(org) — выбросит исключение при ошибке
                            printQueue(queue,false,false);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат");
                        continue;
                    }
                }
                case "remove_by_id" -> {
                    System.out.println("Удаление элемента из коллекции по идентификатору " + cmd_args[1]);
                    try {
                        if (cmd_args.length < 2) {
                            throw new IllegalArgumentException();
                        }

                        long id = Long.parseLong(cmd_args[1].trim());
                        queue.removeIf(org -> org.getId() == id);
                        getInfo();
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат");
                        continue;
                    }
                }
                case "clear" -> {
                    System.out.println("Очистка коллекции");
                    queue.clear();
                    getInfo();
                }
                case "save" -> {
                    if(xmlFilename != null && !xmlFilename.isEmpty()) {
                        System.out.println("Сохранение коллекции в файл organizations.xml");
                        OrganizationXmlHandler.saveQueue(queue, /*"organizations.xml"*/xmlFilename);
                        System.out.println("✅ Данные сохранены");
                    }
                    else {
                        System.out.println("Имя файла для хранения данных не задано. Данные не сохранены");
                    }
                }
                case "remove_head" -> {
                    System.out.println("Вывод и удаление первого элемента коллекции");
                    Organization org = queue.poll();
                    System.out.println(org);
                    getInfo();
                }
                case "remove_greater" -> {
                    System.out.println("Удаление из коллекции всех элементов превышающих заданный " + cmd_args[1]);
                    try {
                        if (cmd_args.length < 2 || !cmd_args[1].startsWith("{") || !cmd_args[1].endsWith("}") || !cmd_args[1].contains(";")){
                            throw new IllegalArgumentException();
                        }

                        List<String> parts = parseAddCommand(cmd_args[1]);

                        if (parts.toArray().length != 5) {
                            throw new IllegalArgumentException();
                        }
                        Organization org = null;
                        try {
                            org = new Organization(parts.get(0),                            //имя
                                    parts.get(1).substring(1, parts.get(1).length() - 1),   //координаты
                                    Float.parseFloat(parts.get(2)),                         //годовой оборот
                                    parts.get(3),                                           //тип
                                    parts.get(4).substring(1, parts.get(4).length() - 1));  //адрес
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }
                        if (org != null && org.valid) {

                            // 🔍 Ищем организацию в очереди с такими же свойствами
                            Long thresholdId = null;
                            for (Organization existing : queue) {
                                //if (org.equals(existing)) {
                                if (org.hasSamePropertiesAs(existing)) {
                                    thresholdId = existing.getId();
                                    break;
                                }
                            }

                            if (thresholdId == null) {
                                System.out.println("⚠️ Организация с такими свойствами не найдена в коллекции. Ничего не удалено.");
                            } else {
                                // 🗑️ Удаляем все элементы с ID > thresholdId
                                Iterator<Organization> iterator = queue.iterator();
                                int removedCount = 0;
                                while (iterator.hasNext()) {
                                    if (iterator.next().getId() > thresholdId) {
                                        iterator.remove();
                                        removedCount++;
                                    }
                                }
                                System.out.println("✅ Удалено элементов: " + removedCount);
                            }

                            printQueue(queue,false,false);
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат. Формат аргумента {наименование;{x;y};годовой_оборот;тип;{адрес;индекс}}");
                    }
                }
                case "history" -> {
                    System.out.println("Вывод последних 6 команд");
                    List<String> history = commandHistory.getHistory();
                    if (history.isEmpty()) {
                        System.out.println("  (пусто)");
                    } else {
                        for (int i = 0; i < history.size(); i++) {
                            System.out.printf("  %d. %s%n", i + 1, history.get(i));
                        }
                    }
                }
                case "execute_script" -> {
                    System.out.println("Выполнение скрипта из файла " + cmd_args[1]);
                    try {
                        if (cmd_args.length < 2) {
                            throw new IllegalArgumentException();
                        }
                        script_cmds = loadLinesModern(cmd_args[1]);
//                        for (String script_cmd : script_cmds) {
//                            System.out.println(script_cmd);
//                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат");
                        continue;
                    }
                }
                case "print_descending" -> {
                    System.out.println("Вывод элементов коллекции в порядке убывания");
                    printQueue(queue,true,false);
//                    List<Organization> list = new ArrayList<>(queue);
//                    list.sort(Comparator.comparingLong(Organization::getId).reversed());
//
//                    for (Organization org : list) {
//                        System.out.println(org);
//                    }
                }
                case "print_field_descending_type"  -> {
                    System.out.println("Вывод значений поля type всех элементов в порядке убывания");
                    printQueue(queue,true,true);
                }
                case "exit" -> {
                    System.out.println("Завершение программы");
                    return;
                }
            }
        }
    }

    public static List<String> loadLinesModern(String filename) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        return lines;
    }

    public static void getInfo() {
        System.out.println("В коллекции " + queue.size() + " элемента(ов)");
    }

    public static List<String> parseAddCommand(String input) {
        List<String> parts = new ArrayList<>();

        String content = input.trim();
        // Убираем внешние фигурные скобки
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
        }

        int depth = 0; // уровень вложенности {}
        StringBuilder current = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '{') {
                depth++;
                current.append(c);
            }
            else if (c == '}') {
                depth--;
                current.append(c);
            }
            else if (c == ';' && depth == 0) {
                // Разделяем только когда не внутри скобок
                parts.add(current.toString().trim());
                current = new StringBuilder();
            }
            else {
                current.append(c);
            }
        }

        // Добавляем последний блок
        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }

        return parts;
    }

    public static void printQueue(PriorityQueue<Organization> queue,boolean reverse,boolean type_only) {
        if (queue == null || queue.isEmpty()) {
            System.out.println("📭 Очередь пуста.");
            return;
        }

        // Создаём временную копию для безопасного извлечения
        PriorityQueue<Organization> tempQueue = null;
        if(!reverse) {
            tempQueue = new PriorityQueue<>(queue);
        }
        else {
            tempQueue = new PriorityQueue<>(
                    queue.size(),
                    Comparator.comparingLong(Organization::getId).reversed()
            );
            tempQueue.addAll(queue);
        }

        if(!type_only) {
            System.out.println("\n" + "=".repeat(150));
            System.out.printf("📋 Содержимое очереди (%d элементов) - в порядке приоритета:%n", queue.size());
            System.out.println("=".repeat(150));
            System.out.printf("%-5s %-20s %-30s %-10s %-12s %-12s %-12s%n",
                    "ID", "Название", "Тип", "Оборот", "Дата", "Координаты", "Адрес");
            System.out.println("-".repeat(150));

            while (!tempQueue.isEmpty()) {
                Organization org = tempQueue.poll();
                // Форматируем тип: заменяем подчёркивания на пробелы и делаем читаемым
                String readableType = org.getType().name();

                System.out.printf("%-5d %-20s %-30s %,12.2f %-12s %-12s %-12s%n",
                        org.getId(),
                        org.getName(),
                        readableType,
                        org.getAnnualTurnover(),
                        org.getCreationDate(),
                        org.getCoordinates().toString(),
                        org.getOfficialAddress().toString());
            }
            System.out.println("=".repeat(150) + "\n");
        }
        else {
            System.out.println("\n" + "=".repeat(40));
            System.out.printf("📋 Содержимое очереди (%d элементов) - в порядке приоритета:%n", queue.size());
            System.out.println("=".repeat(40));
            System.out.printf("%-5s %-30s%n",
                    "ID", "Тип");
            System.out.println("-".repeat(40));

            while (!tempQueue.isEmpty()) {
                Organization org = tempQueue.poll();
                // Форматируем тип: заменяем подчёркивания на пробелы и делаем читаемым
                String readableType = org.getType().name();

                System.out.printf("%-5d %-30ss%n",
                        org.getId(),
                        readableType);
            }
            System.out.println("=".repeat(40) + "\n");
        }
    }

}