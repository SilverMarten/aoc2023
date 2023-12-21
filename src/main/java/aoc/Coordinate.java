package aoc;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

/**
 * A coordinate of row and column.
 */
public final class Coordinate implements Comparable<Coordinate> {
    private final int row;
    private final int column;
    private final int hashCode;

    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
        this.hashCode = computeHashCode();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public static Coordinate of(int row, int column) {
        return new Coordinate(row, column);
    }

    /**
     * @return The set of adjacent coordinates to this coordinate.
     */
    public Set<Coordinate> findAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> !(x == 0 && y == 0))
                                                .mapToObj(y -> Coordinate.of(this.row + y, this.column + x)))
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    /**
     * @return The set of orthogonally adjacent coordinates to this coordinate.
     */
    public Set<Coordinate> findOrthogonalAdjacent() {
        return IntStream.rangeClosed(-1, 1)
                        .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                .filter(y -> x == 0 ^ y == 0)
                                                .mapToObj(y -> Coordinate.of(this.row + y, this.column + x)))
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet());
    }

    /**
     * Return the pre-generated hashCode for this Coordinate.
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Compute the hashCode for this Coordinate so it can be cached by the
     * constructor.
     * 
     * @return the computed hashCode for this Coordinate.
     */
    private int computeHashCode() {
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
     * Create a map of the coordinates of digits.
     * 
     * @param lines
     *            The lines to find and map the locations of digits.
     * @return A map of coordinates to the digit found at those coordinates.
     */
    public static Map<Coordinate, Integer> mapDigits(List<String> lines) {

        AtomicInteger row = new AtomicInteger(1);

        Map<Coordinate, Integer> coordinates = new HashMap<>();
        lines.stream().forEachOrdered(line -> {
            IntStream.range(0, line.length())
                     .forEach(i -> {
                         char digit = line.charAt(i);
                         if (Character.isDigit(digit))
                             coordinates.put(new Coordinate(row.get(), i + 1), digit - '0');
                     });

            row.getAndIncrement();
        });

        return coordinates;
    }

    /**
     * Create a map of the coordinates of non-blank characters. The default
     * blank character is a period '.'.
     * 
     * @param lines
     *            The lines to find and map the locations of non-blank
     *            characters.
     * @return A map of coordinates to the character found at those coordinates.
     */
    public static Map<Coordinate, Character> mapCoordinates(List<String> lines) {
        return mapCoordinates(lines, '.');
    }

    /**
     * Create a map of the coordinates of non-blank characters.
     * 
     * @param lines
     *            The lines to find and map the locations of non-blank
     *            characters.
     * @param blankSpace
     *            The character to be treated as a blank space.
     * @return A map of coordinates to the character found at those coordinates.
     */
    public static Map<Coordinate, Character> mapCoordinates(List<String> lines, char blankSpace) {

        AtomicInteger row = new AtomicInteger(1);

        Map<Coordinate, Character> coordinates = new HashMap<>();
        lines.stream().forEachOrdered(line -> {
            BitSet chars = ArrayUtils.indexesOf(line.toCharArray(), blankSpace);
            // Use the length of the line, since the length of the BitSet is only the last
            // set bit, which might not be the end of the line.
            chars.flip(0, line.length());
            chars.stream()
                 .forEach(c -> coordinates.put(new Coordinate(row.get(), c + 1), line.charAt(c)));

            row.getAndIncrement();
        });

        return coordinates;
    }

    /**
     * Map a list of strings into a set of coordinates of the locations of # in
     * the strings.
     * 
     * @param lines
     *            The lines to find and map the locations of #s.
     * @return The set of coordinates of the locations of #s.
     */
    public static Set<Coordinate> findCoordinates(List<String> lines) {
        return findCoordinates(lines, '#');
    }

    /**
     * Map a list of strings into a set of coordinates of the locations of a
     * given character in the strings.
     * 
     * @param lines
     *            The lines to find and map the locations of the given
     *            character.
     * @param charToFind
     *            The character to find in the strings and return the
     *            coordinates of.
     * @return The set of coordinates of the locations of the given character.
     */
    public static Set<Coordinate> findCoordinates(List<String> lines, char charToFind) {
        AtomicInteger row = new AtomicInteger(1);

        Set<Coordinate> coordinates = new HashSet<>();
        for (String line : lines) {
            coordinates.addAll(ArrayUtils.indexesOf(line.toCharArray(), charToFind)
                                         .stream()
                                         .mapToObj(c -> new Coordinate(row.get(), c + 1))
                                         .collect(Collectors.toSet()));
            row.getAndIncrement();
        }

        return coordinates;
    }

    /**
     * Create a printout of the digit map, using '.' for empty spaces.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param coordinates
     *            The map of coordinates to display the corresponding digit.
     * 
     * @return A string representation of the map.
     */
    public static String printDigitMap(int rows, int columns, Map<Coordinate, Integer> coordinates) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append((char) (coordinates.getOrDefault(new Coordinate(location / columns, location % columns + 1),
                                                             '.' - '0') +
                                    '0'));

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * Create a printout of the map, translating the value to a character with
     * the given function, using '.' for empty spaces.
     * 
     * @param <V>
     *            The type of the value in the given map.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param coordinates
     *            The map of coordinates to display the corresponding character.
     * @param The
     *            function to map the given value type to a character.
     * 
     * @return A string representation of the map.
     */
    public static <V> String printMap(int rows, int columns, Map<Coordinate, V> coordinates,
                                      Function<V, Character> characterMapper) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            Optional<V> charToPrint = Optional.ofNullable(coordinates.get(new Coordinate(location / columns,
                                                                                         location % columns + 1)));
            printout.append(charToPrint.map(characterMapper).orElse('.'));

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * Create a printout of the map, using '.' for empty spaces.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param coordinates
     *            The map of coordinates to display the corresponding character.
     * 
     * @return A string representation of the map.
     */
    public static String printMap(int rows, int columns, Map<Coordinate, Character> coordinates) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append(coordinates.getOrDefault(new Coordinate(location / columns, location % columns + 1), '.'));

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * Create a printout of the map, using '#' as the marker and '.' for empty
     * spaces.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param coordinates
     *            The set of coordinates to display.
     * 
     * @return A string representation of the map.
     */
    public static String printMap(int rows, int columns, Set<Coordinate> coordinates) {
        return printMap(rows, columns, coordinates, '#');
    }

    /**
     * Create a printout of the map, using '#' as the marker and '.' for empty
     * spaces.
     * 
     * @param maxRow
     *            The number of rows in the map.
     * @param maxColumn
     *            The number of columns in the map.
     * @param coordinates
     *            The set of coordinates to display.
     * 
     * @return A string representation of the map.
     */
    public static String printMap(int minRow, int minColumn, int maxRow, int maxColumn, Set<Coordinate> coordinates) {
        return printMap(minRow, minColumn, maxRow, maxColumn, coordinates, '#');
    }

    /**
     * Create a printout of the map.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param coordinates
     *            The set of coordinates to display.
     * @param presentMarker
     *            The character to print at the given coordinates.
     * 
     * @return A string representation of the map.
     */
    public static String printMap(int rows, int columns, Set<Coordinate> coordinates, char presentMarker) {
        return printMap(1, 1, rows, columns, coordinates, presentMarker);
    }

    /**
     * Create a printout of the map.
     * 
     * @param maxRow
     *            The number of rows in the map.
     * @param maxColumn
     *            The number of columns in the map.
     * @param coordinates
     *            The set of coordinates to display.
     * @param presentMarker
     *            The character to print at the given coordinates.
     * 
     * @return A string representation of the map.
     */
    public static String printMap(int minRow, int minColumn, int maxRow, int maxColumn,
                                  Set<Coordinate> coordinates, char presentMarker) {

        StringBuilder printout = new StringBuilder();

        IntStream.rangeClosed(minRow, maxRow).forEach(r -> {
            IntStream.rangeClosed(minColumn, maxColumn).forEach(c -> {
                Coordinate location = Coordinate.of(r, c);
                printout.append(coordinates.contains(location) ? presentMarker : ".");
            });
            printout.append('\n');
        });

        return printout.toString();
    }

    /**
     * Create a printout of the map.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param firstCoordinates
     *            The first set of coordinates to display.
     * @param firstMarker
     *            The character to print at the given coordinates in the first
     *            set.
     * @param secondCoordinates
     *            The second set of coordinates to display.
     * @param secondMarker
     *            The character to print at the given coordinates in the second
     *            set.
     * @return A string representation of the map.
     */
    public static String printMap(int rows, int columns,
                                  Set<Coordinate> firstCoordinates, char firstMarker,
                                  Set<Coordinate> secondCoordinates, char secondMarker) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            Coordinate coordinate = new Coordinate(location / columns, location % columns + 1);
            char marker = '.';
            if (firstCoordinates.contains(coordinate))
                marker = firstMarker;
            else if (secondCoordinates.contains(coordinate))
                marker = secondMarker;
            printout.append(marker);

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }
}