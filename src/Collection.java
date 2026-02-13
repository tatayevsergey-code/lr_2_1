import java.util.Scanner;

public class Collection {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Невозможен запуск без аргументов. Для просмотра справки используйте аргумент help ");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        // технологический вывод
        for (int i = 0; i < args.length; i++) {
            System.out.println("Аргумент " + i + ": " + args[i]);
        }

        if (args[0].equals("help")) {
            System.out.println("""
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
        }
        else if (args[0].equals("info")) {

        }
        else if (args[0].equals("show")) {

        }
        else if (args[0].equals("add")) {
            System.out.println("Добавление элемента в коллекцию");
            System.out.print("Введите название организации: ");
            String name = scanner.nextLine();  // читает всю строку до перевода строки
            System.out.print("Введите координаты: ");
            Organization org = new Organization();
        }
        else if (args[0].equals("update")) {

        }else if (args[0].equals("remove_by_id")) {

        }
        else if (args[0].equals("clear")) {

        }
        else if (args[0].equals("save")) {

        }
        else if (args[0].equals("execute_script")) {

        }
        else if (args[0].equals("exit")) {
            return;
        }
    }
}