import java.util.Objects;

public class Coordinates {
    private long x;
    private long y;

    public Coordinates(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() { return x; }
    public long getY() { return y; }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
