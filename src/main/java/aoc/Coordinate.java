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

    /**
     * Create a printout of the map, using '#' as the marker and '.' for empty
     * spaces.
     * 
     * @param coordinates
     *            The set of coordinates to display.
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @return A string representation of the map.
     */
    public static String printMap(Set<Coordinate> coordinates, int rows, int columns) {
        return printMap(coordinates, rows, columns, '#');
    }

    /**
     * Create a printout of the map.
     * 
     * @param coordinates
     *            The set of coordinates to display.
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param presentMarker
     *            The character to print at the given coordinates.
     * @return A string representation of the map.
     */
    public static String printMap(Set<Coordinate> coordinates, int rows, int columns, char presentMarker) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append(coordinates.contains(new Coordinate(location / columns, location % columns + 1)) ? presentMarker
                                                                                                             : ".");

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

}