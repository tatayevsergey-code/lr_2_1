import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Organization {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private long id;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private float annualTurnover;
    private OrganizationType type;
    private Address officialAddress;

    boolean valid = true;

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public float getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return Float.compare(annualTurnover, that.annualTurnover) == 0 && valid == that.valid && Objects.equals(name, that.name) && Objects.equals(coordinates, that.coordinates) && Objects.equals(creationDate, that.creationDate) && type == that.type && Objects.equals(officialAddress, that.officialAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, coordinates, creationDate, annualTurnover, type, officialAddress, valid);
    }

    public Address getOfficialAddress() {
        return officialAddress;
    }

    public void setOfficialAddress(Address officialAddress) {
        this.officialAddress = officialAddress;
    }

    /**
     * Сравнивает организации по бизнес-свойствам (исключая авто-генерируемый ID).
     */
    public boolean hasSamePropertiesAs(Organization other) {
        if (other == null) return false;
        return Objects.equals(this.name, other.name) &&
                Objects.equals(this.coordinates, other.coordinates) &&
                Float.compare(this.annualTurnover, other.annualTurnover) == 0 &&
                this.type == other.type &&
                Objects.equals(this.officialAddress, other.officialAddress);
    }

    // Конструктор для загрузки из XML
    public Organization(/*long id,*/ String name, Coordinates coordinates, LocalDate creationDate,
                        float annualTurnover, OrganizationType type, Address officialAddress) {
        //this.id = id;
        this.id = ID_COUNTER.getAndIncrement(); // Уникальный ID
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.annualTurnover = annualTurnover;
        this.type = type;
        this.officialAddress = officialAddress;
    }

    public Organization(String name, String coordinates, float annualTurnover, String type, String officialAddress) {


        // === Валидация входных данных ===
        if (name == null || name.trim().isEmpty()) {
            valid = false;
            throw new IllegalArgumentException("Название организации не может быть пустым");
        }
        if (coordinates == null) {
            valid = false;
            throw new IllegalArgumentException("Координаты не могут быть null");
        }
        if (annualTurnover <= 0) {
            valid = false;
            throw new IllegalArgumentException("Годовой оборот должен быть положительным числом");
        }
        if (type == null) {
            valid = false;
            throw new IllegalArgumentException("Тип организации не может быть null");
        }
        if (officialAddress == null) {
            valid = false;
            throw new IllegalArgumentException("Официальный адрес не может быть null");
        }

        // === Генерация служебных полей ===
        this.id = ID_COUNTER.getAndIncrement(); // Уникальный ID
        this.creationDate = LocalDate.now();    // Текущая дата создания
        //название организации
        this.name = name;
        //координаты
        String[] xy = coordinates.split(";");
        long x = Long.parseLong(xy[0]);
        long y = Long.parseLong(xy[1]);
        this.coordinates = new Coordinates(x, y);
        //годовой оборот
        this.annualTurnover = annualTurnover;

        //Тип организации
        // Нормализация типа: удаляем всё кроме букв и пробелов, заменяем множественные пробелы на один,
        // приводим к формату enum (UPPER_SNAKE_CASE)
        String normalized_type = type
                .replaceAll("[^а-яА-Я\\s]", "")          // Удаляем знаки препинания, цифры, подчёркивания
                .replaceAll("\\s+", "_")                 // Пробелы → подчёркивания
                .toUpperCase();                          // В верхний регистр
        try {
            //this.type = OrganizationType.valueOf(normalized_type);
            this.type = OrganizationType.fromReadableName(normalized_type);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Некорректный тип: '" + type + "'");
            System.out.println("✅ Доступные варианты: " +
                    Arrays.stream(OrganizationType.values())
                            .map(OrganizationType::getReadableName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));

            valid = false;
        }

        //Адрес
        String[] adr = officialAddress.split(";");
        this.officialAddress = new Address(adr[0], adr[1]);
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", annualTurnover=" + annualTurnover +
                ", type=" + type +
                ", officialAddress=" + officialAddress +
                '}';
    }
}
