package aoc._2023;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/20
 * 
 * @author Paul Cormier
 *
 */
public class Day20 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day20.class);

    private static final String INPUT_TXT = "input/Day20.txt";

    private static final String TEST_INPUT_TXT_1 = "testInput/Day20_1.txt";
    private static final String TEST_INPUT_TXT_2 = "testInput/Day20_2.txt";

    private static class Module {
        Operation operation;
        String name;
        private final List<Module> destinationModules;

        public Module(String name) {
            this.operation = (f, i) -> Optional.empty();
            this.name = name;
            this.destinationModules = new ArrayList<>();
        }

        public void addDestination(Module destination) {
            this.destinationModules.add(destination);
            // Signal it low to initialize
            destination.handleInput(this, false);
        }

        public List<Pulse> handleInput(Module fromModule, boolean input) {
            // Process the operation and generate a list of outputs
            Optional<Boolean> output = operation.handleInput(fromModule, input);

            return output.map(out -> this.destinationModules.stream()
                                                            .peek(m -> log.debug("{} -{}-> {}",
                                                                                 this.name,
                                                                                 Boolean.TRUE.equals(out) ? "high" : "low",
                                                                                 m.name))
                                                            .map(dest -> Pulse.of(this, dest, out))
                                                            .collect(Collectors.toList()))
                         .orElse(Collections.emptyList());
        }

        @Override
        public String toString() {
            return String.format("%s%s -> %s", ObjectUtils.defaultIfNull(operation, ""), name,
                                 destinationModules.stream().map(m -> m.name).collect(Collectors.joining(", ")));
        }
    }

    @FunctionalInterface
    private interface Operation {
        /**
         * Handle the processing of state and inputs
         * 
         * @param state
         * @param inputs
         * @return
         */
        public Optional<Boolean> handleInput(Module fromModule, boolean input);
    }

    private static class FlipFlop implements Operation {

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            // TODO Auto-generated method stub
            return Optional.of(false);
        }

        @Override
        public String toString() {
            return "%";
        }

    }

    private static class Conjunction implements Operation {

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            // TODO Auto-generated method stub
            return Optional.of(false);
        }

        @Override
        public String toString() {
            return "&";
        }

    }

    private static final class Pulse {
        final Module from;
        final Module to;
        final boolean signal;

        private Pulse(Module from, Module to, boolean signal) {
            this.from = from;
            this.to = to;
            this.signal = signal;
        }

        public static Pulse of(Module from, Module to, boolean signal) {
            return new Pulse(from, to, signal);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test files
        List<String> testLines1 = FileUtils.readFile(TEST_INPUT_TXT_1);

        int expectedTestResult = 32000000;
        long part1TestResult = part1(testLines1);
        log.info("The product of the total high and low pulses is: {} (should be {})", part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        List<String> testLines2 = FileUtils.readFile(TEST_INPUT_TXT_2);

        expectedTestResult = 11687500;
        part1TestResult = part1(testLines2);
        log.info("The product of the total high and low pulses is: {} (should be {})", part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The product of the total high and low pulses is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        expectedTestResult = 1_234_567_890;
        long part2TestResult = part2(testLines1);
        log.info("{} (should be {})", part2TestResult, expectedTestResult);

        if (part2TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Consult your module configuration; determine the number of low pulses and
     * high pulses that would be sent after pushing the button 1000 times,
     * waiting for all pulses to be fully handled after each push of the button.
     * What do you get if you multiply the total number of low pulses sent by
     * the total number of high pulses sent?
     * 
     * @param lines
     *            The lines describing each module.
     * @return The product of the total high and low pulses.
     */
    private static long part1(final List<String> lines) {

        // Parse the modules
        Map<String, Module> moduleMap = parseModules(lines);

        // I'm sure this isn't going to work for part 2, but...
        AtomicLong totalHighs = new AtomicLong();
        AtomicLong totalLows = new AtomicLong();
        Queue<Pulse> inputsToProcess = new ArrayDeque<>();

        Module button = new Module("button");
        button.operation = (f, i) -> Optional.of(false);
        button.addDestination(moduleMap.get("broadcaster"));

        IntStream.range(0, 1).forEach(i -> {
            // Push the button
            inputsToProcess.addAll(button.handleInput(button, false));

            // Process the queue
            while (!inputsToProcess.isEmpty()) {
                Pulse processing = inputsToProcess.poll();

                // Count highs and lows
                (processing.signal ? totalHighs : totalLows).getAndIncrement();

                // Add any new pulses
                inputsToProcess.addAll(processing.to.handleInput(processing.from, processing.signal));

            }

        });

        log.debug("Total low pulses: {}\t Total high pulses: {}", totalLows.get(), totalHighs.get());

        return totalLows.get() * totalHighs.get();
    }

    private static Map<String, Module> parseModules(final List<String> lines) {
        Map<String, Module> moduleMap = new HashMap<>();
        Map<Character, Supplier<Operation>> operationCreatorMap = Map.of('%', FlipFlop::new, '&', Conjunction::new);
        lines.forEach(line -> {

            // Get/create the source Module
            Module sourceModule = moduleMap.computeIfAbsent(line.substring(0, line.indexOf(' ')).replaceAll("\\W", ""), Module::new);
            // Set its operation
            char operation = line.charAt(0);
            if (operationCreatorMap.containsKey(operation))
                sourceModule.operation = operationCreatorMap.get(operation).get();
            // Add its destinations
            Stream.of(line.substring(line.indexOf('>') + 1).split(","))
                  .map(name -> moduleMap.computeIfAbsent(name.trim(), Module::new))
                  .forEach(sourceModule::addDestination);
        });

        log.atDebug()
           .setMessage("Modules:\n{}")
           .addArgument(moduleMap.values().stream().map(Module::toString).collect(Collectors.joining("\n")))
           .log();
        return moduleMap;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}