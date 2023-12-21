package aoc._2023;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/21
 * 
 * @author Paul Cormier
 *
 */
public class Day21 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day21.class);

    private static final String INPUT_TXT = "input/Day21.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day21.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 16;
        int steps = 6;
        int part1TestResult = part1(testLines, steps);
        log.info("The Elf can reach {} garden plots in {} steps. (should be {})", part1TestResult, steps, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The Elf can reach {} garden plots in 64 steps.", part1(lines, 64));

        // PART 2
        log.info("Part 2:");
        //        log.setLevel(Level.DEBUG);

        int[] steps2 = { 6, 10, 50, 100, 500, 1000, 5000 };
        long[] expectedTestResults = { 16, 50, 1594, 6536, 167004, 668697, 16722044 };
        for (int i = 0; i < steps2.length; i++) {

            int part2TestResult = part2(testLines, steps2[i]);
            log.info("The Elf can reach {} garden plots in {} steps. (should be {})", part2TestResult, steps2[i], expectedTestResults[i]);

            if (part2TestResult != expectedTestResults[i])
                log.error("The test result doesn't match the expected value.");
        }

        log.setLevel(Level.INFO);

        log.info("The Elf can reach {} garden plots in 26501365 steps.", part2(lines, 26501365));
    }

    /**
     * Starting from the garden plot marked S on your map, how many garden plots
     * could the Elf reach in exactly n steps?
     * 
     * @param lines
     *            The lines describing the garden plots and rocks.
     * @param steps
     *            The number of steps the Elf will take.
     * @return The number of garden plots that can be reached.
     */
    private static int part1(final List<String> lines, int steps) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map
        Set<Coordinate> rocks = Coordinate.findCoordinates(lines, '#');
        Set<Coordinate> gardenPlots = Coordinate.findCoordinates(lines, '.');
        Coordinate start = Coordinate.findCoordinates(lines, 'S').stream().findAny().get();
        gardenPlots.add(start);

        // Start at start's neighbours, and go n steps.
        Queue<Coordinate> plotsToVisit = new ArrayDeque<>(start.findOrthogonalAdjacent()
                                                               .stream()
                                                               .filter(gardenPlots::contains)
                                                               .collect(Collectors.toSet()));
        Queue<Coordinate> plotsToVisitNext = new ArrayDeque<>();

        IntStream.range(1, steps).forEach(i -> {
            //            log.debug("Step {}:\n{}", i, plotsToVisit);
            log.atDebug()
               .setMessage("Step {}:\n{}\n{}")
               .addArgument(i)
               .addArgument(plotsToVisit)
               .addArgument(() -> Coordinate.printMap(rows, columns, rocks, '#', Set.copyOf(plotsToVisit), 'O'))
               .log();

            while (!plotsToVisit.isEmpty()) {
                Coordinate plot = plotsToVisit.poll();
                plotsToVisitNext.addAll(plot.findOrthogonalAdjacent()
                                            .stream()
                                            .filter(c -> gardenPlots.contains(c) &&
                                                         !plotsToVisitNext.contains(c))
                                            .collect(Collectors.toSet()));
            }
            plotsToVisit.addAll(plotsToVisitNext);
            plotsToVisitNext.clear();

        });

        return plotsToVisit.size();
    }

    /**
     * Starting from the garden plot marked S on your infinite map, how many
     * garden plots could the Elf reach in exactly 26501365 steps?
     * 
     * @param lines
     *            The lines describing the garden plots and rocks.
     * @param steps
     * @param steps
     *            The number of steps the Elf will take.
     * @return The number of garden plots that can be reached.
     */
    private static int part2(final List<String> lines, int steps) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map
        Set<Coordinate> rocks = Coordinate.findCoordinates(lines, '#');
        Set<Coordinate> gardenPlots = Coordinate.findCoordinates(lines, '.');
        Coordinate start = Coordinate.findCoordinates(lines, 'S').stream().findAny().get();
        gardenPlots.add(start);

        // Start at start's neighbours, and go n steps.
        Queue<Coordinate> plotsToVisit = new ArrayDeque<>(start.findOrthogonalAdjacent()
                                                               .stream()
                                                               .filter(gardenPlots::contains)
                                                               .collect(Collectors.toSet()));
        Queue<Coordinate> plotsToVisitNext = new ArrayDeque<>();

        IntStream.range(1, steps).forEach(i -> {
            //            log.debug("Step {}:\n{}", i, plotsToVisit);
            log.atDebug()
               .setMessage("Step {}:\n{}\n{}")
               .addArgument(i)
               .addArgument(plotsToVisit)
               .addArgument(() -> Coordinate.printMap(rows, columns, rocks, '#', Set.copyOf(plotsToVisit), 'O'))
               .log();

            while (!plotsToVisit.isEmpty()) {
                Coordinate plot = plotsToVisit.poll();
                plotsToVisitNext.addAll(plot.findOrthogonalAdjacent()
                                            .stream()
                                            .filter(c -> gardenPlots.contains(c) &&
                                                         !plotsToVisitNext.contains(c))
                                            .collect(Collectors.toSet()));
            }
            plotsToVisit.addAll(plotsToVisitNext);
            plotsToVisitNext.clear();

        });

        return plotsToVisit.size();
    }

}