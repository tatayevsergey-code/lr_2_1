// Новый файл: Collectible.java
public interface Collectible<T extends Collectible<T>> extends Comparable<T> {
    long getId();
    void setId(long id);
    boolean hasSamePropertiesAs(T other);
    String getName();
}
