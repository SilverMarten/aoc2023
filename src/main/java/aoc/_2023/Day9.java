package aoc._2023;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/9
 * 
 * @author Paul Cormier
 *
 */
public class Day9 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day9.class);

    private static final String INPUT_TXT = "input/Day9.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day9.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the extrapolated values is: {} (should be 114)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the extrapolated values is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the extrapolated values is: {} (should be 2)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The sum of the extrapolated values is: {}", part2(lines));
    }

    /**
     * What is the sum of these extrapolated values?
     * 
     * @param lines The sequences of values for which the next digits need to be
     *     estimated.
     * @return The sum of the extrapolated values for each sequence.
     */
    private static int part1(final List<String> lines) {
        return lines.stream()
                    .map(l -> Arrays.asList(l.split(" "))
                                    .stream()
                                    .mapToInt(Integer::valueOf).toArray())
                    .mapToInt(Day9::findNextValue)
                    .sum();
    }

    /**
     * Find the next number in the given sequence of numbers.
     * 
     * @param sequence The sequence of numbers.
     * @return The next value in the sequence.
     */
    private static int findNextValue(int[] sequence) {
        log.debug("{}", Arrays.toString(sequence));

        int nextValue = 0;
        boolean allZero = false;
        while (!allZero) {
            allZero = true;
            // Accumulate the last value of the sequence
            nextValue += sequence[sequence.length - 1];

            // Find the differences
            int[] nextSequence = new int[sequence.length - 1];
            for (int i = 0; i < nextSequence.length; i++) {
                nextSequence[i] = sequence[i + 1] - sequence[i];
                allZero &= nextSequence[i] == 0;
            }

            sequence = nextSequence;
            log.debug("{}", Arrays.toString(sequence));
        }

        return nextValue;
    }

    /**
     * What is the sum of these extrapolated values?
     * 
     * @param lines The sequences of values for which the previous digits need to be
     *     estimated.
     * @return The sum of the extrapolated values for each sequence.
     */
    private static int part2(final List<String> lines) {

        return lines.stream()
                    .map(l -> Arrays.asList(l.split(" "))
                                    .stream()
                                    .mapToInt(Integer::valueOf).toArray())
                    .map(a -> {
                        ArrayUtils.reverse(a);
                        return a;
                    })
                    .mapToInt(Day9::findNextValue)
                    .sum();
    }

}