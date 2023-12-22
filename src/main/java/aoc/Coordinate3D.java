package aoc;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A coordinate of row and column.
 */
public final class Coordinate3D implements Comparable<Coordinate3D> {
    private final int row;
    private final int column;
    private final int height;
    private final int hashCode;

    public Coordinate3D(int row, int column, int height) {
        this.row = row;
        this.column = column;
        this.height = height;
        this.hashCode = computeHashCode();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getHeight() {
        return height;
    }

    public static Coordinate3D of(int row, int column, int height) {
        return new Coordinate3D(row, column, height);
    }

    /**
     * @return The set of adjacent coordinates to this coordinate.
     */
    public Set<Coordinate3D> findAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> !(x == 0 && y == 0))
                                                .mapToObj(y -> IntStream.rangeClosed(-1, 1)
                                                                        .filter(z -> !(x == 0 && y == 0 && z == 0))
                                                                        .mapToObj(z -> Coordinate3D.of(this.row + y,
                                                                                                       this.column + x,
                                                                                                       this.height + z))))
                        .flatMap(s -> s)
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    /**
     * @return The set of orthogonally adjacent coordinates to this coordinate.
     */
    public Set<Coordinate3D> findOrthogonalAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> x == 0 ^ y == 0)
                                                .mapToObj(y -> IntStream.rangeClosed(-1, 1)
                                                                        .filter(z -> (x == 0 ^ z == 0) ^ (y == 0 ^ z == 0))
                                                                        .mapToObj(z -> Coordinate3D.of(this.row + y,
                                                                                                       this.column + x,
                                                                                                       this.height + z))))
                        .flatMap(s -> s)
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Compute the hashCode for this Coordinate3D so it can be cached by the
     * constructor.
     * 
     * @return the computed hashCode for this Coordinate3D.
     */
    private int computeHashCode() {
        return Objects.hash(column, height, row);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coordinate3D other = (Coordinate3D) obj;
        return column == other.column && height == other.height && row == other.row;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", row, column, height);
    }

    @Override
    public int compareTo(Coordinate3D o) {
        int result = Integer.compare(this.row, o.row);
        result = result == 0 ? Integer.compare(this.column, o.column) : result;
        return result == 0 ? Integer.compare(this.height, o.height) : result;
    }

}
