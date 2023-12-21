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
        log.setLevel(Level.DEBUG);

        expectedTestResult = 1_234_567_890;
        int part2TestResult = part2(testLines);
        log.info("{} (should be {})", part2TestResult, expectedTestResult);

        if (part2TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
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
        //        Set<Coordinate> visitedPlots = new HashSet<>();

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
                //                visitedPlots.add(plot);
                plotsToVisitNext.addAll(plot.findOrthogonalAdjacent()
                                            .stream()
                                            .filter(c -> gardenPlots.contains(c) &&
                                                         //                                                         !visitedPlots.contains(c) &&
                                                         !plotsToVisitNext.contains(c))
                                            .collect(Collectors.toSet()));
            }
            plotsToVisit.addAll(plotsToVisitNext);
            plotsToVisitNext.clear();

        });

        return plotsToVisit.size();
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}