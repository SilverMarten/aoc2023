package aoc._2023;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/2
 * 
 * @author Paul Cormier
 *
 */
public class Day2 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day2.class);

    private static final String INPUT_TXT = "input/Day2.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day2.txt";

    private enum CubeColour {
        RED, GREEN, BLUE
    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("The sum of the possible game ids is: {} (should be 8)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the possible game ids is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the products of the number of cubes needed is: {} (should be 2286)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The sum of the products of the number of cubes needed is: {}", part2(lines));
    }

    /**
     * The Elf would first like to know which games would have been possible if the
     * bag contained only 12 red cubes, 13 green cubes, and 14 blue cubes?
     * 
     * @param lines
     * @return The sum of the game ids which are possible for that many cubes
     */
    private static int part1(final List<String> lines) {
        final int maxRed = 12;
        final int maxGreen = 13;
        final int maxBlue = 14;

        int sumOfGameIds = 0;

        List<Map<CubeColour, Integer>> result;
        int gameId;
        for (String gameLine : lines) {
            log.debug("Game input: {}", gameLine);
            result = countCubes(gameLine.split(":")[1]);

            boolean possible = result.stream().noneMatch(m -> m.getOrDefault(CubeColour.RED, 0) > maxRed
                                                              || m.getOrDefault(CubeColour.GREEN, 0) > maxGreen
                                                              || m.getOrDefault(CubeColour.BLUE, 0) > maxBlue);
            if (possible) {
                gameId = Integer.parseInt(StringUtils.getDigits(gameLine.split(":")[0]));
                log.debug("Game {} is possible", gameId);
                sumOfGameIds += gameId;
            }
        }

        return sumOfGameIds;
    }

    /**
     * Given a line of the format:
     * 
     * <pre>
     * Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
     * </pre>
     * 
     * parse the number of cubes for each colour for every round.
     * 
     * @param game
     * @return A list of maps of the {@link CubeColour} and counts.
     */
    private static List<Map<CubeColour, Integer>> countCubes(String game) {

        List<Map<CubeColour, Integer>> rounds = new ArrayList<>();
        for (String round : game.split(";")) {
            rounds.add(Stream.of(round.split(","))
                             .collect(Collectors.toMap(cc -> CubeColour.valueOf(cc.trim().split(" ")[1].toUpperCase()),
                                                       cc -> Integer.valueOf(cc.trim().split(" ")[0]))));

        }
        log.debug("Mapped result: {}", rounds);
        return rounds;
    }

    /**
     * in each game you played, what is the fewest number of cubes of each color
     * that could have been in the bag to make the game possible?
     * 
     * @param lines
     * @return The sum of the product of the number of cubes required for each game
     *     to be possible.
     */
    private static int part2(final List<String> lines) {

        int sumOfProducts = 0;

        for (String gameLine : lines) {
            int maxRed = 0;
            int maxGreen = 0;
            int maxBlue = 0;

            log.debug("Game input: {}", gameLine);

            for (Map<CubeColour, Integer> game : countCubes(gameLine.split(":")[1])) {
                maxRed = Integer.max(maxRed, game.getOrDefault(CubeColour.RED, 0));
                maxGreen = Integer.max(maxGreen, game.getOrDefault(CubeColour.GREEN, 0));
                maxBlue = Integer.max(maxBlue, game.getOrDefault(CubeColour.BLUE, 0));
            }
            sumOfProducts += maxRed * maxGreen * maxBlue;
        }

        return sumOfProducts;
    }

}