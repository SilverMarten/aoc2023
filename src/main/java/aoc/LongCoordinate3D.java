package aoc;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A coordinate of row, column, and height.
 */
public final class LongCoordinate3D implements Comparable<LongCoordinate3D> {
    private final long row;
    private final long column;
    private final long height;
    private final int hashCode;

    public LongCoordinate3D(long row, long column, long height) {
        this.row = row;
        this.column = column;
        this.height = height;
        this.hashCode = computeHashCode();
    }

    public long getRow() {
        return row;
    }

    public long getColumn() {
        return column;
    }

    public long getHeight() {
        return height;
    }

    public static LongCoordinate3D of(long row, long column, long height) {
        return new LongCoordinate3D(row, column, height);
    }

    /**
     * @return The set of adjacent coordinates to this coordinate.
     */
    public Set<LongCoordinate3D> findAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> !(x == 0 && y == 0))
                                                .mapToObj(y -> IntStream.rangeClosed(-1, 1)
                                                                        .filter(z -> !(x == 0 && y == 0 && z == 0))
                                                                        .mapToObj(z -> LongCoordinate3D.of(this.row + y,
                                                                                                           this.column + x,
                                                                                                           this.height + z))))
                        .flatMap(s -> s)
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    /**
     * @return The set of orthogonally adjacent coordinates to this coordinate.
     */
    public Set<LongCoordinate3D> findOrthogonalAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> x == 0 ^ y == 0)
                                                .mapToObj(y -> IntStream.rangeClosed(-1, 1)
                                                                        .filter(z -> (x == 0 ^ z == 0)
                                                                                     ^ (y == 0 ^ z == 0))
                                                                        .mapToObj(z -> LongCoordinate3D.of(this.row + y,
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
        LongCoordinate3D other = (LongCoordinate3D) obj;
        return column == other.column && height == other.height && row == other.row;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", row, column, height);
    }

    @Override
    public int compareTo(LongCoordinate3D o) {
        int result = Long.compare(this.row, o.row);
        result = result == 0 ? Long.compare(this.column, o.column) : result;
        return result == 0 ? Long.compare(this.height, o.height) : result;
    }

}
