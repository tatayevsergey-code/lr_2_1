import java.util.Objects;

public class Address {
    private String street;
    private String zipCode;

    public Address(String street, String zipCode) {
        this.street = street;
        this.zipCode = zipCode;
    }

    public String getStreet() { return street; }
    public String getZipCode() { return zipCode; }

    @Override
    public String toString() {
        return "{" +   zipCode + ',' + street + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) && Objects.equals(zipCode, address.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, zipCode);
    }
}
