import java.util.Arrays;

public enum OrganizationType {
    TRUST("Trust"),
    PRIVATE_LIMITED_COMPANY("Private Limited Company"),
    OPEN_JOINT_STOCK_COMPANY("Open Joint Stock Company");

    private final String readableName;

    OrganizationType(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return readableName;
    }

    // Статический метод для отладки/логгирования
    public static String getAllReadableNames() {
        return Arrays.stream(values())
                .map(OrganizationType::getReadableName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
