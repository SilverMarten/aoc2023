package aoc._2023;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/12
 * 
 * @author Paul Cormier
 *
 */
public class Day12 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day12.class);

    private static final String INPUT_TXT = "input/Day12.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day12.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);
        log.setLevel(Level.TRACE);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the different arrangements of the springs is: {} (should be 21)", part1(testLines));

        log.setLevel(Level.INFO);
//        log.setLevel(Level.DEBUG);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the different arrangements of the springs is: {} (should be less than 6,142,112)",
                 part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the different arrangements of the springs is: {} (should be 525,152)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The sum of the different arrangements of the springs is: {} (should be less than 2,059,101,273,163)",
                 part2(lines));
    }

    /**
     * For each row, count all of the different arrangements of operational and
     * broken springs that meet the given criteria. What is the sum of those
     * counts?
     * 
     * @param lines
     *     The lines containing information about the springs
     * @return The sum of the different arrangements of the springs
     */
    private static int part1(final List<String> lines) {

        Map<String, List<Integer>> cache = new HashMap<>();

        return lines.stream()
                    .mapToInt(l -> countCombinations(l.split(" ")[0],
                                                     Stream.of(l.split(" ")[1].split(",")).map(Integer::parseInt)
                                                           .collect(Collectors.toList()),
                                                     cache))
                    .sum();
    }

    /**
     * Given a description of a line of springs, and the known groups of damaged
     * springs, find how many configurations there could be.
     * 
     * @param springString
     *     The String describing the line of springs
     * @param groups
     *     The sizes of each contiguous group of damaged springs
     * @return The number of combinations which would satisfy the known
     *     information in the line of springs as well as the list of groups
     *     of damaged springs.
     */
    private static int countCombinations(String springString, List<Integer> groups, Map<String, List<Integer>> cache) {

        // Add an implicit . at the beginning and end
        // springString = StringUtils.wrap(springString, '.');

        log.trace("{} {}", springString, groups);
        String[] springs = springString.split("");

        // The known number of broken springs
        int brokenSprings = groups.stream().mapToInt(Integer::intValue).sum();

        int visbleBrokenSprings = ArrayUtils.indexesOf(springs, "#").cardinality();

        int operationalSprings = springs.length - brokenSprings;

        int visibleOperationalSprings = ArrayUtils.indexesOf(springs, ".").cardinality();

        int unknownSprings = ArrayUtils.indexesOf(springs, "?").cardinality();

        int totalCombinations = (int) Math.pow(2, unknownSprings);
        log.trace("Broken: {}/{}\tOperational: {}/{}\tUnknown: {}\tTotal springs: {}\tCombinations: {}",
                  visbleBrokenSprings, brokenSprings,
                  visibleOperationalSprings, operationalSprings,
                  unknownSprings, springs.length, totalCombinations);

        // Try each possibility, count the working ones
        AtomicInteger possibleCombinations = new AtomicInteger(0);
        IntStream.range(0, totalCombinations).forEach(i -> {
            // Replace each ? with . or #, sequentially
            String combination = springString;
            while (combination.contains("?")) {
                combination = combination.replaceFirst("\\?", i % 2 == 0 ? "." : "#");
                i = i / 2;
            }

            // Check if it matches
            String combinationToTest = combination;
            List<Integer> groupsToTest = cache.computeIfAbsent(combinationToTest.replaceAll("\\.+", "."),
                                                               k -> Stream.of(combinationToTest.split("\\.+"))
                                                                          .map(String::length)
                                                                          .filter(n -> n > 0)
                                                                          .collect(Collectors.toList()));
            if (groups.equals(groupsToTest)) {
                log.trace("{} {} works", combination, groupsToTest);
                possibleCombinations.incrementAndGet();
            } else {
                log.trace("{} {} invalid", combination, groupsToTest);
            }
        });

        log.debug("{} {} - {} arrangement{}", springString, groups, possibleCombinations,
                  possibleCombinations.get() > 1 ? "s" : "");

        return possibleCombinations.get();
    }

    /**
     * For each row, count all of the different arrangements of operational and
     * broken springs that meet the given criteria. What is the sum of those
     * counts?
     * 
     * @param lines
     *     The lines containing information about the springs, to be duplicated 5
     *     times.
     * @return The sum of the different arrangements of the springs
     */
    private static long part2(final List<String> lines) {

        Map<String, List<Integer>> cache = new HashMap<>();
        return lines.stream()
                    .mapToLong(l -> countCombinations(Collections.nCopies(5, l.split(" ")[0])
                                                                 .stream()
                                                                 .collect(Collectors.joining("?")),
                                                      Stream.of(Collections.nCopies(5, l.split(" ")[1])
                                                                           .stream()
                                                                           .collect(Collectors.joining(","))
                                                                           .split(","))
                                                            .map(Integer::parseInt)
                                                            .collect(Collectors.toList()),
                                                      cache))
                    .sum();
    }

}