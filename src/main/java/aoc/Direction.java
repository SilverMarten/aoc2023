package aoc;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Direction {
    RIGHT('>', 'R', Coordinate.of(0, 1)),
    DOWN('v', 'D', Coordinate.of(1, 0)),
    LEFT('<', 'L', Coordinate.of(0, -1)),
    UP('^', 'U', Coordinate.of(-1, 0));

    private final char symbol;
    private final char letter;
    private final Coordinate translation;

    private static final Map<Character, Direction> symbolMap = Collections.unmodifiableMap(Stream.of(Direction.values())
                                                                                                 .collect(Collectors.toMap(d -> d.symbol,
                                                                                                                           d -> d)));
    private static final Map<Character, Direction> letterMap = Collections.unmodifiableMap(Stream.of(Direction.values())
                                                                                                 .collect(Collectors.toMap(d -> d.letter,
                                                                                                                           d -> d)));

    private Direction(char symbol, char letter, Coordinate translation) {
        this.symbol = symbol;
        this.letter = letter;
        this.translation = translation;
    }

    public static Direction withSymbol(char symbol) {
        return symbolMap.get(symbol);
    }

    public static Direction withLetter(char letter) {
        return letterMap.get(letter);
    }

    public static Direction oppositeOf(Direction direction) {
        switch (direction) {
            case UP:
                return DOWN;
            case RIGHT:
                return LEFT;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Direction opposite() {
        return oppositeOf(this);
    }

    public char getSymbol() {
        return symbol;
    }

    public char getLetter() {
        return letter;
    }

    public Coordinate getTranslation() {
        return translation;
    }

}
