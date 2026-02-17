import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Collection {
    public static void main(String[] args) {

        // Сортировка по годовому обороту (от меньшего к большему)
        PriorityQueue<Organization> queue = new PriorityQueue<>(
                Comparator.comparingLong(Organization::getId)
        );

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введи команду, собака:");
        while (true) {
            String cmd = scanner.nextLine();
            String cmd_args[] = cmd.split(" ");
            System.out.println("Текущая введенная команда - " + cmd);
            switch (cmd) {
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
                        exit - завершение программы (без сохранения в файл)
                        """);
                case "info" -> {
                }
                case "show" -> {
                    System.out.println("Вывод всех элементов коллекции");
                    printQueue(queue);
                }
                case "add" -> {
                    System.out.println("Добавление элемента в коллекцию");

                    String elm = cmd_args[1].substring(1, args[1].length() - 1);   //убрал скобки
                    System.out.println(elm);
                    String[] elmArr = elm.split(";");
                    System.out.println(Arrays.toString(elmArr));

                    Organization org = null;

                    if (elmArr.length != 5) {
                            System.out.println("Неверный формат. Формат аргумента {наименование;координаты;годовой_оборот;тип;адрес}");
                            continue;
                    }
                            //throw new IllegalArgumentException("Неверный формат. Формат аргумента {наименование;координаты;годовой_оборот;тип;адрес}");
                    try {
                        org = new Organization(elmArr[0],       //имя
                                elmArr[1],                      //координаты
                                Float.parseFloat(elmArr[2]),    //годовой оборот
                                elmArr[3],                      //тип
                                elmArr[4]);                     //адрес
                    } catch (IllegalArgumentException e) {
                        System.err.println("Ошибка: " + e.getMessage() + ". Повторите ввод.");
                    } /*finally*/ {
                        if (org != null) {

                            System.out.println(org);
                            queue.offer(org); // или queue.add(org) — выбросит исключение при ошибке
                            printQueue(queue);
                        }
                    }
                }
                case "update" -> {
                }
                case "remove_by_id" -> {
                }
                case "clear" -> {
                }
                case "save" -> {
                }
                case "execute_script" -> {
                }
                case "exit" -> {
                    return;
                }
            }
        }
    }

    public static void printQueue(PriorityQueue<Organization> queue) {
        if (queue == null || queue.isEmpty()) {
            System.out.println("📭 Очередь пуста.");
            return;
        }

        // Создаём временную копию для безопасного извлечения
        PriorityQueue<Organization> tempQueue = new PriorityQueue<>(queue);

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("📋 Содержимое очереди (%d элементов) - в порядке приоритета:%n", queue.size());
        System.out.println("=".repeat(80));
        System.out.printf("%-4s %-10s %-25s %-30s %-12s %-12s%n",
                "№", "ID", "Название", "Тип", "Оборот", "Дата");
        System.out.println("-".repeat(80));

        int index = 1;
        while (!tempQueue.isEmpty()) {
            Organization org = tempQueue.poll();
            // Форматируем тип: заменяем подчёркивания на пробелы и делаем читаемым
            String readableType = org.getType().name();

            System.out.printf("%-4d %-10d %-25s %-30s %,12.2f %-12s%n",
                    index++,
                    org.getId(),
                    org.getName(),
                    readableType,
                    org.getAnnualTurnover(),
                    org.getCreationDate());
        }
        System.out.println("=".repeat(80) + "\n");
    }

}