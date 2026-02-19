import java.util.Arrays;

public enum OrganizationType {
    TRUST("Траст"),
    PRIVATE_LIMITED_COMPANY("ООО"),
    OPEN_JOINT_STOCK_COMPANY("АО");

    private final String readableName;

    OrganizationType(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return readableName;
    }

    // 🔑 Ключевой метод: поиск по человекочитаемому имени
    public static OrganizationType fromReadableName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя типа организации не может быть пустым");
        }
        String normalized = name.trim().replaceAll("\\s+", " "); // убрать лишние пробелы
        for (OrganizationType type : values()) {
            if (type.readableName.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип: '" + name + "'");
    }

    // Статический метод для отладки/логгирования
    public static String getAllReadableNames() {
        return Arrays.stream(values())
                .map(OrganizationType::getReadableName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
