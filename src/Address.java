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
}
