import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Collection<T extends Collectible<T>> {  // ← ДОБАВЬТЕ <T extends Collectible<T>>

    // УБЕРИТЕ static, ДОБАВЬТЕ generic тип
    private PriorityQueue<T> queue;  // ← Было: static PriorityQueue queue;

    public static void main(String[] args) throws Exception {
        // Создайте экземпляр Collection с Organization
        Collection<Organization> collection = new Collection<>();
        collection.run(args);  // ← Вынесите логику в метод run
    }

    public void run(String[] args) throws Exception {
        //queue = new PriorityQueue<>(Comparator.comparingLong(Collectible::getId));

        queue = new PriorityQueue<>(
                Comparator.comparingLong((T item) -> item.getId())
        );

        CommandHistory commandHistory = new CommandHistory(6);

        String xmlFilename = System.getenv("XML_FILENAME");

        // Загрузка
        if(xmlFilename != null && !xmlFilename.isEmpty()) {
            PriorityQueue<Organization> loadedQueue = OrganizationXmlHandler.loadQueue(xmlFilename);
            System.out.println("✅ Данные загружены, размер: " + loadedQueue.size());

            while (!loadedQueue.isEmpty()) {
                queue.offer((T) loadedQueue.poll());  // ← Приведение типа
            }
        } else {
            System.out.println("Имя файла для хранения данных не задано. Данные не загружены");
        }

        List<String> script_cmds = null;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введи команду, собака: ");
            String cmd = " ";

            if(script_cmds != null && !script_cmds.isEmpty()){
                cmd = script_cmds.getFirst();
                script_cmds.removeFirst();
            } else {
                cmd = scanner.nextLine();
                commandHistory.add(cmd);
            }

            // ... парсинг команд остаётся без изменений ...
            String[] cmd_args = new String[3];
            if(cmd.trim().contains("  ")) {
                cmd_args[0] = cmd.substring(0, cmd.indexOf("  "));
                cmd = cmd.substring(cmd.indexOf("  ") + 1);
                if(cmd.startsWith("{ ")) cmd_args[1] = cmd;
                else{
                    if(cmd.contains("  ")) {
                        cmd_args[1] = cmd.substring(0, cmd.indexOf("  "));
                        cmd_args[2] = cmd.substring(cmd.indexOf("  ") + 1);
                    } else {
                        cmd_args[1] = cmd;
                    }
                }
            } else {
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
                    execute_script file_name - считать и исполнить скрипт из указанного файла
                    remove_head - вывести первый элемент коллекции и удалить его
                    remove_greater {элемент} - удалить из коллекции все элементы превышающие заданный
                    history - вывести последние 6 команд
                    remove_any_by_official_address officialAddress - удалить один элемент по адресу
                    print_descending - вывести элементы коллекции в порядке убывания
                    print_field_descending_type - вывести значения поля type в порядке убывания
                    exit - завершение программы (без сохранения в файл)
                    """);

                case "info" -> {
                    System.out.println("Вывод информации о коллекции");
                    getInfo();
                }

                case "show" -> {
                    System.out.println("Вывод всех элементов коллекции");
                    printQueue(queue, false, false);
                }

                case "add" -> {
                    System.out.println("Добавление элемента в коллекцию");
                    try {
                        if (cmd_args.length < 2 || !cmd_args[1].startsWith("{") ||
                                !cmd_args[1].endsWith("}") || !cmd_args[1].contains(";")){
                            throw new IllegalArgumentException();
                        }

                        List<String> parts = parseAddCommand(cmd_args[1]);

                        if (parts.toArray().length != 5) {
                            throw new IllegalArgumentException();
                        }

                        Organization org = null;
                        try {
                            org = new Organization(
                                    parts.get(0),
                                    parts.get(1).substring(1, parts.get(1).length() - 1),
                                    Float.parseFloat(parts.get(2)),
                                    parts.get(3),
                                    parts.get(4).substring(1, parts.get(4).length() - 1)
                            );
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }

                        if (org != null && org.valid) {
                            System.out.println(org);
                            queue.offer((T) org);  // ← Приведение типа
                            printQueue(queue, false, false);
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат. Формат: {наименование;{x;y};годовой_оборот;тип;{адрес;индекс}}");
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
                            org = new Organization(
                                    parts.get(0),
                                    parts.get(1).substring(1, parts.get(1).length() - 1),
                                    Float.parseFloat(parts.get(2)),
                                    parts.get(3),
                                    parts.get(4).substring(1, parts.get(4).length() - 1)
                            );
                            org.setId(id);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }

                        if (org != null && org.valid) {
                            System.out.println(org);
                            queue.offer((T) org);
                            printQueue(queue, false, false);
                        }
                    } catch (IllegalArgumentException e) {
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
                    } catch (IllegalArgumentException e) {
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
                        System.out.println("Сохранение коллекции в файл");
                        OrganizationXmlHandler.saveQueue((PriorityQueue<Organization>) queue, xmlFilename);
                        System.out.println("✅ Данные сохранены");
                    } else {
                        System.out.println("Имя файла для хранения данных не задано. Данные не сохранены");
                    }
                }

                case "remove_head" -> {
                    System.out.println("Вывод и удаление первого элемента коллекции");
                    T org = queue.poll();
                    System.out.println(org);
                    getInfo();
                }

                case "remove_greater" -> {
                    System.out.println("Удаление из коллекции всех элементов превышающих заданный " + cmd_args[1]);
                    try {
                        if (cmd_args.length < 2 || !cmd_args[1].startsWith("{") ||
                                !cmd_args[1].endsWith("}") || !cmd_args[1].contains(";")){
                            throw new IllegalArgumentException();
                        }

                        List<String> parts = parseAddCommand(cmd_args[1]);

                        if (parts.toArray().length != 5) {
                            throw new IllegalArgumentException();
                        }

                        Organization org = null;
                        try {
                            org = new Organization(
                                    parts.get(0),
                                    parts.get(1).substring(1, parts.get(1).length() - 1),
                                    Float.parseFloat(parts.get(2)),
                                    parts.get(3),
                                    parts.get(4).substring(1, parts.get(4).length() - 1)
                            );
                        } catch (IllegalArgumentException e) {
                            System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                        }

                        if (org != null && org.valid) {
                            Long thresholdId = null;
                            for (T existing : queue) {
                                if (org.hasSamePropertiesAs((Organization) existing)) {
                                    thresholdId = existing.getId();
                                    break;
                                }
                            }

                            if (thresholdId == null) {
                                System.out.println("⚠️ Организация с такими свойствами не найдена в коллекции. Ничего не удалено.");
                            } else {
                                Iterator<T> iterator = queue.iterator();
                                int removedCount = 0;
                                while (iterator.hasNext()) {
                                    if (iterator.next().getId() > thresholdId) {
                                        iterator.remove();
                                        removedCount++;
                                    }
                                }
                                System.out.println("✅ Удалено элементов: " + removedCount);
                            }

                            printQueue(queue, false, false);
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат. Формат: {наименование;{x;y};годовой_оборот;тип;{адрес;индекс}}");
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
                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат");
                        continue;
                    }
                }

                case "print_descending" -> {
                    System.out.println("Вывод элементов коллекции в порядке убывания");
                    printQueue(queue, true, false);
                }

                case "print_field_descending_type" -> {
                    System.out.println("Вывод значений поля type всех элементов в порядке убывания");
                    printQueue(queue, true, true);
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

    public void getInfo() {  // ← Уберите static
        System.out.println("В коллекции " + queue.size() + " элемента(ов)");
    }

    public static List<String> parseAddCommand(String input) {
        List<String> parts = new ArrayList<>();

        String content = input.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
        }

        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '{') {
                depth++;
                current.append(c);
            } else if (c == '}') {
                depth--;
                current.append(c);
            } else if (c == ';' && depth == 0) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }

        return parts;
    }

    public static void printQueue(PriorityQueue<? extends Collectible<?>> queue,
                                  boolean reverse,
                                  boolean type_only) {  // ← Измените параметр
        if (queue == null || queue.isEmpty()) {
            System.out.println("📭 Очередь пуста.");
            return;
        }

        PriorityQueue<Collectible<?>> tempQueue;
        if(!reverse) {
            tempQueue = new PriorityQueue<>(queue);
        } else {
            tempQueue = new PriorityQueue<>(
                    queue.size(),
                    Comparator.comparingLong((Collectible<?> item) -> item.getId()).reversed()
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
                Organization org = (Organization) tempQueue.poll();  // ← Приведение для Organization
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
        } else {
            System.out.println("\n" + "=".repeat(40));
            System.out.printf("📋 Содержимое очереди (%d элементов) - в порядке приоритета:%n", queue.size());
            System.out.println("=".repeat(40));
            System.out.printf("%-5s %-30s%n", "ID", "Тип");
            System.out.println("-".repeat(40));

            while (!tempQueue.isEmpty()) {
                Organization org = (Organization) tempQueue.poll();
                String readableType = org.getType().name();

                System.out.printf("%-5d %-30s%n", org.getId(), readableType);
            }
            System.out.println("=".repeat(40) + "\n");
        }
    }
}