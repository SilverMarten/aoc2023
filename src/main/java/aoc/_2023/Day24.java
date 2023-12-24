package aoc._2023;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.Range;
import org.slf4j.LoggerFactory;

import aoc.Coordinate3D;
import aoc.FileUtils;
import aoc.LongCoordinate3D;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/24
 * 
 * @author Paul Cormier
 *
 */
public class Day24 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day24.class);

    private static final String INPUT_TXT = "input/Day24.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day24.txt";

    private static final class Hailstone {
        final LongCoordinate3D position;
        final Coordinate3D velocity;

        public Hailstone(LongCoordinate3D position, Coordinate3D velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        @Override
        public String toString() {
            return String.format("%d, %d, %d @ %d, %d, %d",
                                 position.getRow(), position.getColumn(), position.getHeight(),
                                 velocity.getRow(), velocity.getColumn(), velocity.getHeight());
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 2;
        int part1TestResult = part1(testLines, Range.of(7L, 27L));
        log.info("{} pairs of hailstone which will cross within the test area. (should be {})",
                 part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("{} pairs of hailstone which will cross within the test area.",
                 part1(lines, Range.of(200_000_000_000_000L, 400_000_000_000_000L)));

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
     * Considering only the X and Y axes, check all pairs of hailstones' future paths for intersections. How many of
     * these intersections occur within the test area?
     * 
     * @param lines The lines describing each hail stone's position and velocity.
     * @param rangeToCheck The range of x and y values within which to look for collisions.
     * @return The number of pairs of hailstone which will cross within the test area.
     */
    private static int part1(final List<String> lines, Range<Long> rangeToCheck) {

        // Parse the positions and velocities
        List<Hailstone> hailstones = lines.stream().map(l -> {
            String[] position = l.split(" @ *")[0].split(", *");
            String[] velocity = l.split(" @ *")[1].split(", *");
            return new Hailstone(LongCoordinate3D.of(Long.parseLong(position[0]),
                                                     Long.parseLong(position[1]),
                                                     Long.parseLong(position[2])),
                                 Coordinate3D.of(Integer.parseInt(velocity[0]),
                                                 Integer.parseInt(velocity[1]),
                                                 Integer.parseInt(velocity[2])));
        })
                                          .collect(Collectors.toList());

        log.debug("Hailstones:\n{}", hailstones.stream().map(Hailstone::toString).collect(Collectors.joining("\n")));

        // Find all pairs
        Set<Set<Hailstone>> combinations = hailstones.stream()
                                                     .flatMap(h -> hailstones.stream()
                                                                             .filter(h2 -> h2 != h)
                                                                             .map(h2 -> Set.of(h, h2)))
                                                     .collect(Collectors.toSet());

        return (int) IterableUtils.countMatches(combinations,
                                                pair -> instersectsInRange2D(pair, rangeToCheck));
    }

    /**
     * Determine if two hailstones will intersect, in 2D, within the given range.
     * <br>
     * See: <a href=
     * "https://www.baeldung.com/java-intersection-of-two-lines#java-impl">https://www.baeldung.com/java-intersection-of-two-lines#java-impl</a>
     * 
     * @param pair The pair of hailstones to check.
     * @param rangeToCheck The acceptable range for the x and y (row and column) coordinates.
     * @return {@code true} if the hailstone will intersect, {@code false} otherwise.
     * 
     */
    private static boolean instersectsInRange2D(Set<Hailstone> pair, Range<Long> rangeToCheck) {

        Hailstone stone1 = IterableUtils.get(pair, 0);
        Hailstone stone2 = IterableUtils.get(pair, 1);
        log.debug("Hailstone A: {}", stone1);
        log.debug("Hailstone B: {}", stone2);

        // Slope 1
        double m1 = (double) stone1.velocity.getColumn() / stone1.velocity.getRow();
        // Y-intercept 1
        double b1 = stone1.position.getColumn() - m1 * stone1.position.getRow();
        //Slope 2
        double m2 = (double) stone2.velocity.getColumn() / stone2.velocity.getRow();
        // Y-intercept 2
        double b2 = stone2.position.getColumn() - m2 * stone2.position.getRow();

        if (m1 == m2) {
            log.debug("Hailstones' paths are parallel; they never intersect.");
            return false;
        }

        double x = (b2 - b1) / (m1 - m2);
        double y = m1 * x + b1;

        boolean stone1Past = (stone1.position.getRow() - x) * stone1.velocity.getRow() >= 0 &&
                             (stone1.position.getColumn() - y) * stone1.velocity.getColumn() >= 0;
        boolean stone2Past = (stone2.position.getRow() - x) * stone2.velocity.getRow() >= 0 &&
                             (stone2.position.getColumn() - y) * stone2.velocity.getColumn() >= 0;

        if (stone1Past && !stone2Past)
            log.debug("Hailstones' paths crossed in the past for hailstone A.");
        else if (!stone1Past && stone2Past)
            log.debug("Hailstones' paths crossed in the past for hailstone B.");
        else if (stone1Past && stone2Past)
            log.debug("Hailstones' paths crossed in the past for both hailstones.");

        if (stone1Past || stone2Past)
            return false;

        boolean intersect = x >= rangeToCheck.getMinimum() && x <= rangeToCheck.getMaximum()
                            && y >= rangeToCheck.getMinimum() && y <= rangeToCheck.getMaximum();

        log.debug(String.format("Hailstones' paths will cross %s the test area (at x=%.3f, y=%.3f)",
                                intersect ? "inside" : "outside",
                                x, y));

        return intersect;

    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}