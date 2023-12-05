package aoc2023;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/5
 * 
 * @author Paul Cormier
 *
 */
public class Day5 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day5.class);

    private static final String INPUT_TXT = "input/Day5.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day5.txt";

    private static final class AlmanacMap {

        Range<Long> idRange;
        long translationValue;

        public AlmanacMap(long rangeStart, long rangeEnd, long translationValue) {
            this.idRange = Range.between(rangeStart, rangeEnd);
            this.translationValue = translationValue;
        }

        /**
         * Using this map, convert the given id to the new id, if possible.
         * 
         * @param id
         *            The input id to be translated.
         * @return The result of the translation, or the original value if
         */
        Optional<Long> mapId(Long id) {
            return idRange.contains(id) ? Optional.of(id + translationValue) : Optional.empty();
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The lowest location number is {} (should be 35)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The lowest location number is {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * What is the lowest location number that corresponds to any of the initial
     * seed numbers?
     * 
     * @param lines
     *            The lines from the almanac
     * @return The lowest location number that corresponds to any of the initial
     *         seed numbers.
     */
    private static long part1(final List<String> lines) {

        // Seeds: the first line is seeds
        List<Long> seeds = Stream.of(lines.get(0).substring(7).split(" "))
                                 .map(Long::valueOf)
                                 .collect(Collectors.toList());
        log.debug("Seeds: {}", seeds);

        // Parse lines into almanac list
        List<List<AlmanacMap>> almanacMaps = parseAlmanac(lines.subList(2, lines.size()));

        // Translate all seeds, and find lowest value
        return seeds.stream().mapToLong(s -> {
            AtomicLong seedValue = new AtomicLong(s);

            for (List<AlmanacMap> almanacMap : almanacMaps) {
                almanacMap.stream()
                          .map(m -> m.mapId(seedValue.get()))
                          .filter(Optional::isPresent)
                          .map(Optional::get)
                          .findAny()
                          .ifPresent(seedValue::set);
            }
            return seedValue.get();
        })
                    .min()
                    .getAsLong();

    }

    private static List<List<AlmanacMap>> parseAlmanac(List<String> lines) {
        List<List<AlmanacMap>> almanac = new ArrayList<>();

        List<AlmanacMap> currentMap = null;
        for (String line : lines) {
            // Skip blank lines
            if (line.isBlank())
                continue;

            // Start new maps
            if (line.matches("^\\D.*")) {
                log.debug("Starting {}", line);
                currentMap = new ArrayList<>();
                almanac.add(currentMap);
                continue;
            }

            // Parse AlmanacMap
            String[] values = line.split(" ");
            long start = Long.parseLong(values[1]);
            long length = Long.parseLong(values[2]);
            long difference = Long.parseLong(values[0]) - start;
            currentMap.add(new AlmanacMap(start, start + length - 1, difference));
        }

        return almanac;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}