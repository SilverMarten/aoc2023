package aoc;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A coordinate of row and column.
 */
public final class Coordinate implements Comparable<Coordinate> {
    private final int row;
    private final int column;

    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * @return The set of adjacent coordinates to this coordinate.
     */
    public Set<Coordinate> findAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> !(x == 0 && y == 0))
                                                .mapToObj(y -> new Coordinate(this.row + y, this.column + x)))
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coordinate other = (Coordinate) obj;
        if (column != other.column)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", row, column);
    }

    @Override
    public int compareTo(Coordinate o) {
        int result = Integer.compare(this.row, o.row);

        return result == 0 ? Integer.compare(this.column, o.column) : result;
    }

}