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
            this.operation = new DefaultOperation();
            this.name = name;
            this.destinationModules = new ArrayList<>();
        }

        public void addDestination(Module destination) {
            this.destinationModules.add(destination);
        }

        public List<Pulse> handleInput(Module fromModule, boolean input) {
            // Process the operation and generate a list of outputs
            Optional<Boolean> output = operation.handleInput(fromModule, input);

            return output.map(out -> this.destinationModules.stream()
                                                            .peek(m -> log.trace("{} -{}-> {}",
                                                                                 this.name,
                                                                                 Boolean.TRUE.equals(out) ? "high"
                                                                                                          : "low",
                                                                                 m.name))
                                                            .map(dest -> Pulse.of(this, dest, out))
                                                            .collect(Collectors.toList()))
                         .orElse(Collections.emptyList());
        }

        public void initConnections() {
            this.destinationModules.forEach(d -> d.operation.init(this));
        }

        @Override
        public String toString() {
            return String.format("%s%s -> %s", ObjectUtils.defaultIfNull(operation, ""), name,
                                 destinationModules.stream().map(m -> m.name).collect(Collectors.joining(", ")));
        }
    }

    private interface Operation {
        /**
         * Handle the processing of state and inputs
         * 
         * @param fromModule
         * @param input
         * @return
         */
        public Optional<Boolean> handleInput(Module fromModule, boolean input);

        public void init(Module fromModule);
    }

    private static class DefaultOperation implements Operation {

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            return Optional.of(input);
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public void init(Module fromModule) {
        }

    }

    private static class Button implements Operation {

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            return Optional.of(false);
        }

        @Override
        public void init(Module fromModule) {
        }
    }

    /**
     * Flip-flop modules (prefix %) are either on or off; they are initially off. If
     * a flip-flop module receives a high pulse, it is ignored and nothing happens.
     * However, if a flip-flop module receives a low pulse, it flips between on and
     * off. If it was off, it turns on and sends a high pulse. If it was on, it
     * turns off and sends a low pulse.
     */
    private static class FlipFlop implements Operation {

        private boolean state = false;

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            if (input)
                return Optional.empty();

            state = !state;

            return Optional.of(state);
        }

        @Override
        public String toString() {
            return "%";
        }

        @Override
        public void init(Module fromModule) {
        }

    }

    /**
     * Conjunction modules (prefix &) remember the type of the most recent pulse
     * received from each of their connected input modules; they initially default
     * to remembering a low pulse for each input. When a pulse is received, the
     * conjunction module first updates its memory for that input. Then, if it
     * remembers high pulses for all inputs, it sends a low pulse; otherwise, it
     * sends a high pulse.
     */
    private static class Conjunction implements Operation {

        private Map<Module, Boolean> state = new HashMap<>();

        @Override
        public void init(Module fromModule) {
            state.put(fromModule, false);
        }

        @Override
        public Optional<Boolean> handleInput(Module fromModule, boolean input) {
            state.put(fromModule, input);
            return Optional.of(!state.values().stream().allMatch(b -> b));
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
        log.setLevel(Level.TRACE);

        // Read the test files
        List<String> testLines1 = FileUtils.readFile(TEST_INPUT_TXT_1);

        int expectedTestResult = 32000000;
        long part1TestResult = part1(testLines1);
        log.info("The product of the total high and low pulses is: {} (should be {})", part1TestResult,
                 expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        List<String> testLines2 = FileUtils.readFile(TEST_INPUT_TXT_2);

        expectedTestResult = 11687500;
        part1TestResult = part1(testLines2);
        log.info("The product of the total high and low pulses is: {} (should be {})", part1TestResult,
                 expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The product of the total high and low pulses is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");

        log.setLevel(Level.DEBUG);

        log.info("The fewest number of button pushes required to send a low pulse to module \"rx\" is: {}",
                 part2(lines));
    }

    /**
     * Consult your module configuration; determine the number of low pulses and
     * high pulses that would be sent after pushing the button 1000 times,
     * waiting for all pulses to be fully handled after each push of the button.
     * What do you get if you multiply the total number of low pulses sent by
     * the total number of high pulses sent?
     * 
     * @param lines
     *     The lines describing each module.
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
        button.operation = new Button();
        button.addDestination(moduleMap.get("broadcaster"));

        // Push the button once
        inputsToProcess.addAll(button.handleInput(button, false));

        // Process the queue
        while (!inputsToProcess.isEmpty()) {
            Pulse processing = inputsToProcess.poll();

            // Count highs and lows
            (processing.signal ? totalHighs : totalLows).getAndIncrement();

            // Add any new pulses
            inputsToProcess.addAll(processing.to.handleInput(processing.from, processing.signal));

        }

        log.setLevel(Level.INFO);

        // Now push it 999 times more
        IntStream.range(1, 1000).forEach(i -> {
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

    /**
     * Reset all modules to their default states. Waiting for all pulses to be fully
     * handled after each button press, what is the fewest number of button presses
     * required to deliver a single low pulse to the module named rx?
     * 
     * @param lines
     *     The lines describing each module.
     * @return The fewest number of button pushes required to send a low pulse to
     *     module "rx".
     */
    private static int part2(final List<String> lines) {

        // Parse the modules
        Map<String, Module> moduleMap = parseModules(lines);

        Queue<Pulse> inputsToProcess = new ArrayDeque<>();

        Module rx = moduleMap.get("rx");

        Module button = new Module("button");
        button.operation = new Button();
        button.addDestination(moduleMap.get("broadcaster"));

        // Now push it 999 times more
        int buttonPushes = 0;
        while (buttonPushes++ >= 0) {
            // Push the button
            inputsToProcess.addAll(button.handleInput(button, false));

            // Process the queue
            while (!inputsToProcess.isEmpty()) {
                Pulse processing = inputsToProcess.poll();

                // Watch for a low pulse going to "rx"
                if (processing.to == rx && !processing.signal)
                    break;

                // Add any new pulses
                inputsToProcess.addAll(processing.to.handleInput(processing.from, processing.signal));

            }

            // Periodically log the progress...
            if (Math.log10(buttonPushes) % 1 == 0)
                log.debug("{} button pushes.", buttonPushes);

        }

        return buttonPushes;
    }

    private static Map<String, Module> parseModules(final List<String> lines) {
        Map<String, Module> moduleMap = new HashMap<>();
        Map<Character, Supplier<Operation>> operationCreatorMap = Map.of('%', FlipFlop::new, '&', Conjunction::new);
        lines.forEach(line -> {

            // Get/create the source Module
            Module sourceModule = moduleMap.computeIfAbsent(line.substring(0, line.indexOf(' ')).replaceAll("\\W", ""),
                                                            Module::new);
            // Set its operation
            char operation = line.charAt(0);
            if (operationCreatorMap.containsKey(operation))
                sourceModule.operation = operationCreatorMap.get(operation).get();

            // Add its destinations
            Stream.of(line.substring(line.indexOf('>') + 1).split(","))
                  .map(name -> moduleMap.computeIfAbsent(name.trim(), Module::new))
                  .forEach(sourceModule::addDestination);

        });

        moduleMap.values().forEach(Module::initConnections);

        log.atDebug()
           .setMessage("Modules:\n{}")
           .addArgument(moduleMap.values().stream().map(Module::toString).collect(Collectors.joining("\n")))
           .log();
        return moduleMap;
    }

}