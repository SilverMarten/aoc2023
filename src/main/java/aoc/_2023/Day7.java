package aoc._2023;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/7
 * 
 * @author Paul Cormier
 *
 */
public class Day7 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day7.class);

    private static final String INPUT_TXT = "input/Day7.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day7.txt";

    private static class CardHand implements Comparable<CardHand> {

        protected static final String CARDS = "AKQJT98765432";
        protected static final String CARD_INDEX = "abcdefghijklm";
        protected static final List<String> RANKS = List.of("5", "41", "32", "311", "221", "2111", "11111");

        int type;
        int wager;
        String cards;
        protected String index;

        public CardHand(int wager, String cards) {
            this.wager = wager;
            this.cards = cards;
            // Create a lexicographical index of the cards to make ordering easier
            this.index = StringUtils.replaceChars(this.cards, CARDS, CARD_INDEX);
            this.type = findType(cards);
        }

        /**
         * Given a set of cards, figure out its type based on number of repeated
         * cards.
         * 
         * @param cards
         *            The String representing the cards
         * @return The type, 1 being the highest
         */
        protected static int findType(String cards) {

            String cardinality = CollectionUtils.getCardinalityMap(Arrays.asList(cards.split("")))
                                                .values().stream()
                                                .sorted(Comparator.reverseOrder())
                                                .map(Object::toString)
                                                .collect(Collectors.joining());
            log.trace("{} ({})", cards, cardinality);

            return RANKS.indexOf(cardinality) + 1;

        }

        public String toString() {
            return String.format("%s %3d %d", this.cards, this.wager, this.type);
        }

        @Override
        public int compareTo(CardHand otherHand) {
            return Comparator.<CardHand> comparingInt(h -> h.type)
                             .thenComparing(h -> h.index)
                             .reversed()
                             .compare(this, otherHand);
        }

    }

    private static class CardHandWithJokers extends CardHand {

        private static final String CARDS_WITH_JOKERS = "AKQT98765432J";

        public CardHandWithJokers(int wager, String cards) {
            super(wager, cards);
            // Create a lexicographical index of the cards to make ordering easier
            this.index = StringUtils.replaceChars(this.cards, CARDS_WITH_JOKERS, CARD_INDEX);
            this.type = findType(cards);
        }

        /**
         * Given a set of cards, with jokers, figure out its type based on
         * number of repeated cards.
         * 
         * @param cards
         *            The String representing the cards
         * @return The type, 1 being the highest
         */
        protected static int findType(String cards) {

            Map<String, Integer> cardinalityMap = CollectionUtils.getCardinalityMap(Arrays.asList(cards.split("")));

            // If there are jokers, replace them with the most useful card
            if (cardinalityMap.containsKey("J")) {
                cardinalityMap.remove("J");
                String newCard = cardinalityMap.entrySet()
                                               .stream()
                                               .sorted((o1, o2) -> Comparator.<Entry<String, Integer>> comparingInt(Entry::getValue)
                                                                             .reversed().compare(o1, o2))
                                               .findFirst()
                                               .map(Entry::getKey)
                                               .orElse("A");
                cards = cards.replaceAll("J", newCard);
                cardinalityMap = CollectionUtils.getCardinalityMap(Arrays.asList(cards.split("")));
            }

            String cardinality = cardinalityMap.values().stream()
                                               .sorted(Comparator.reverseOrder())
                                               .map(Object::toString)
                                               .collect(Collectors.joining());
            log.trace("{} ({})", cards, cardinality);

            return RANKS.indexOf(cardinality) + 1;

        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.TRACE);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The total winnings are: {} (should be 6440)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The total winnings are: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.TRACE);

        log.info("The new total winnings are: {} (should be 5905)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The new total winnings are: {} (should be higher than 241254630 and 243092105)", part2(lines));
    }

    /**
     * What are the total winnings?
     * 
     * @param lines
     *            The lines representing card hands
     * @return The total winnings for the card hands
     */
    private static int part1(final List<String> lines) {

        AtomicInteger ordinal = new AtomicInteger(1);
        AtomicInteger sum = new AtomicInteger(0);
        lines.stream()
             .map(l -> new CardHand(Integer.parseInt(l.substring(6)), l.substring(0, 5)))
             .sorted()
             .peek(h -> log.debug(h.toString()))
             .forEach(h -> sum.getAndAdd(h.wager * ordinal.getAndIncrement()));

        return sum.get();
    }

    /**
     * What are the new total winnings?
     * 
     * @param lines
     *            The lines representing card hands
     * @return The total winnings for the card hands
     */
    private static int part2(final List<String> lines) {

        AtomicInteger ordinal = new AtomicInteger(1);
        AtomicInteger sum = new AtomicInteger(0);
        lines.stream()
             .map(l -> new CardHandWithJokers(Integer.parseInt(l.substring(6)), l.substring(0, 5)))
             .sorted()
             .peek(h -> log.debug(h.toString()))
             .forEach(h -> sum.getAndAdd(h.wager * ordinal.getAndIncrement()));

        return sum.get();
    }

}
