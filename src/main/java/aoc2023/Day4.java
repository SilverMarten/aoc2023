package aoc2023;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/4
 * 
 * @author Paul Cormier
 *
 */
public class Day4 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day4.class);

    private static final String INPUT_TXT = "input/Day4.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day4.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("The cards are worth {} points. (should be 13)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The cards are worth {} points. (should be higher than 842)", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("You end up with {} total scratch cards. (should be 30)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("You end up with {} total scratch cards.", part2(lines));
    }

    /**
     * Given a list of scratch cards, tally the points of each. How many points
     * are they worth in total? Each winning number matched doubles the point
     * value; the first is 1 point.
     * 
     * @param lines
     *            The lines representing the scratch cards.
     * @return The sum of their point values.
     */
    private static int part1(final List<String> lines) {

        return lines.stream().mapToInt(Day4::countWinningNumbers)
                    .map(n -> (int) Math.pow(2, n - 1.0))
                    .sum();

    }

    /**
     * Count the number of winning numbers on this card.
     * 
     * @param cardLine
     *            The line representing the card.
     * @return The number of winning numbers on this card.
     */
    private static int countWinningNumbers(String cardLine) {
        int winningNumbersStart = cardLine.indexOf(':') + 2;
        int winningNumbersEnd = cardLine.indexOf("|") - 1;
        String[] winningNumbersArray = cardLine.substring(winningNumbersStart, winningNumbersEnd).split(" ");
        log.debug("Winning numbers: {}", Arrays.toString(winningNumbersArray));
        Set<Integer> winningNumbers = Stream.of(winningNumbersArray)
                                            .filter(NumberUtils::isParsable)
                                            .map(Integer::valueOf)
                                            .collect(Collectors.toSet());

        int cardNumbersStart = winningNumbersEnd + 3;
        String[] cardNumbersArray = cardLine.substring(cardNumbersStart).split(" ");
        log.debug("Card numbers: {}", Arrays.toString(cardNumbersArray));
        Set<Integer> cardNumbers = Stream.of(cardNumbersArray)
                                         .filter(NumberUtils::isParsable)
                                         .map(Integer::valueOf)
                                         .collect(Collectors.toSet());

        winningNumbers.retainAll(cardNumbers);
        return winningNumbers.size();
    }

    /**
     * Winning matches result in copies of subsequent cards. How many total
     * scratch cards do you end up with?
     * 
     * @param lines
     *            The lines representing scratch cards.
     * @return The total number of scratch cards.
     */
    private static int part2(final List<String> lines) {

        Map<Integer, AtomicInteger> cards = new HashMap<>();

        // Initialise the cards map with the number of cards at the start
        int totalCards = lines.size();
        IntStream.rangeClosed(1, totalCards)
                 .forEach(i -> cards.put(i, new AtomicInteger(1)));

        int cardNumber = 1;
        for (String line : lines) {
            // Count the matches
            int matches = countWinningNumbers(line);

            int numberOfCards = cards.get(cardNumber).get();
            // Add that many, times the number of those cards, to the map
            IntStream.rangeClosed(cardNumber + 1, cardNumber + matches)
                     .filter(i -> i <= totalCards)
                     .forEach(i -> cards.get(i).accumulateAndGet(numberOfCards, Math::addExact));

            cardNumber++;
        }

        return cards.values().stream().mapToInt(AtomicInteger::get).sum();
    }

}