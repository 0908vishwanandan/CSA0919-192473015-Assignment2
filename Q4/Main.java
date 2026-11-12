import java.util.*;
import java.util.function.Supplier;

class Medicine {

    private final String code;
    private final String name;
    private final int stock;

    Medicine(String code, String name, int stock) {
        this.code = code;
        this.name = name;
        this.stock = stock;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {
        return code + " | " + name + " | Stock: " + stock;
    }
}

public class Main {

    static final int LOOKUPS = 100000;
    static final int REPETITIONS = 5;

    static volatile long resultHolder;

    // Stores a map and its original insertion order.
    static class Inventory {

        Map<String, Medicine> map;

        List<String> insertionOrder;

        Inventory(Map<String, Medicine> map) {
            this.map = map;
            insertionOrder = new ArrayList<>();
        }
    }

    // Generate medicine codes.
    static String createCode(int id) {
        return String.format(Locale.ROOT, "MED%06d", id);
    }

    // Create inventory with different Map implementations.
    static Inventory createInventory(
            int n, Supplier<Map<String, Medicine>> factory) {

        Map<String, Medicine> map = factory.get();

        Inventory inventory = new Inventory(map);

        // Insert in descending order to distinguish
        // code order from insertion order.
        for (int i = n; i >= 1; i--) {

            String code = createCode(i);

            Medicine medicine = new Medicine(
                    code,
                    "Medicine_" + i,
                    100 + (i % 500)
            );

            map.put(code, medicine);

            inventory.insertionOrder.add(code);
        }

        return inventory;
    }

    // Build medicine report.
    static String buildCodeReport(Inventory inventory) {

        Map<String, Medicine> map = inventory.map;

        StringBuilder report = new StringBuilder();

        // TreeMap already maintains sorted key order.
        if (map instanceof SortedMap) {

            for (Medicine medicine : map.values()) {
                report.append(medicine).append('\n');
            }
        }

        // HashMap and LinkedHashMap need sorting.
        else {

            List<String> keys =
                    new ArrayList<>(map.keySet());

            Collections.sort(keys);

            for (String code : keys) {
                report.append(map.get(code)).append('\n');
            }
        }

        return report.toString();
    }

    // Build insertion-order report.
    static String buildInsertionReport(Inventory inventory) {

        Map<String, Medicine> map = inventory.map;

        StringBuilder report = new StringBuilder();

        // LinkedHashMap already maintains insertion order.
        if (map instanceof LinkedHashMap) {

            for (Medicine medicine : map.values()) {
                report.append(medicine).append('\n');
            }
        }

        // Other maps require the original insertion list.
        else {

            for (String code : inventory.insertionOrder) {

                report.append(map.get(code)).append('\n');
            }
        }

        return report.toString();
    }

    // Generate lookup queries.
    static String[] createQueries(int n) {

        String[] queries = new String[LOOKUPS];

        for (int i = 0; i < LOOKUPS; i++) {

            int id = (int) ((i * 7919L) % n) + 1;

            queries[i] = createCode(id);
        }

        return queries;
    }

    // Perform 100,000 lookups.
    static long performLookups(
            Map<String, Medicine> map, String[] queries) {

        long checksum = 0;

        long start = System.nanoTime();

        for (String code : queries) {

            Medicine medicine = map.get(code);

            if (medicine != null) {
                checksum += medicine.getStock();
            }
        }

        long end = System.nanoTime();

        resultHolder += checksum;

        return end - start;
    }

    // Calculate average lookup time.
    static double averageLookupTime(
            Map<String, Medicine> map, String[] queries) {

        // Warm-up
        for (int i = 0; i < 2; i++) {
            performLookups(map, queries);
        }

        long total = 0;

        for (int i = 0; i < REPETITIONS; i++) {
            total += performLookups(map, queries);
        }

        return total / (double) REPETITIONS / 1_000_000.0;
    }

    // Measure code-order report generation.
    static double measureCodeReport(Inventory inventory) {

        // Warm-up
        resultHolder += buildCodeReport(inventory).length();

        long total = 0;

        for (int i = 0; i < REPETITIONS; i++) {

            long start = System.nanoTime();

            String report = buildCodeReport(inventory);

            long end = System.nanoTime();

            resultHolder += report.length();

            total += end - start;
        }

        return total / (double) REPETITIONS / 1_000_000.0;
    }

    // Measure insertion-order report generation.
    static double measureInsertionReport(Inventory inventory) {

        // Warm-up
        resultHolder += buildInsertionReport(inventory).length();

        long total = 0;

        for (int i = 0; i < REPETITIONS; i++) {

            long start = System.nanoTime();

            String report = buildInsertionReport(inventory);

            long end = System.nanoTime();

            resultHolder += report.length();

            total += end - start;
        }

        return total / (double) REPETITIONS / 1_000_000.0;
    }

    // Check whether medicine codes appear in the required order.
    static boolean checkOrder(
            String report, String[] expectedCodes) {

        int previousPosition = -1;

        for (String code : expectedCodes) {

            int position = report.indexOf(code);

            if (position <= previousPosition) {
                return false;
            }

            previousPosition = position;
        }

        return true;
    }

    // Verify ordering using a small test inventory.
    static void verifyOrdering() {

        List<Supplier<Map<String, Medicine>>> factories =
                Arrays.asList(
                        HashMap::new,
                        TreeMap::new,
                        LinkedHashMap::new
                );

        String[] insertionCodes = {
                "MED003", "MED001", "MED002"
        };

        String[] sortedCodes = {
                "MED001", "MED002", "MED003"
        };

        System.out.println("ORDER VERIFICATION");

        for (Supplier<Map<String, Medicine>> factory : factories) {

            Inventory inventory = new Inventory(factory.get());

            for (String code : insertionCodes) {

                inventory.map.put(
                        code,
                        new Medicine(code, "TestMedicine", 50)
                );

                inventory.insertionOrder.add(code);
            }

            String codeReport = buildCodeReport(inventory);

            String insertionReport =
                    buildInsertionReport(inventory);

            System.out.println(
                    inventory.map.getClass().getSimpleName()
            );

            System.out.println(
                    "Code order correct: " +
                    checkOrder(codeReport, sortedCodes)
            );

            System.out.println(
                    "Insertion order correct: " +
                    checkOrder(insertionReport, insertionCodes)
            );
        }
    }

    // Run benchmark for one Map.
    static void benchmark(
            String name,
            Supplier<Map<String, Medicine>> factory,
            int n) {

        System.out.println("\n" + name);

        Inventory inventory = createInventory(n, factory);

        String[] queries = createQueries(n);

        double lookupTime =
                averageLookupTime(inventory.map, queries);

        double codeReportTime =
                measureCodeReport(inventory);

        double insertionReportTime =
                measureInsertionReport(inventory);

        System.out.printf(
                Locale.ROOT,
                "100,000 lookups: %.3f ms%n",
                lookupTime
        );

        System.out.printf(
                Locale.ROOT,
                "Average time per lookup: %.3f ns%n",
                lookupTime * 1_000_000.0 / LOOKUPS
        );

        System.out.printf(
                Locale.ROOT,
                "Code-order report: %.3f ms%n",
                codeReportTime
        );

        System.out.printf(
                Locale.ROOT,
                "Insertion-order report: %.3f ms%n",
                insertionReportTime
        );
    }

    // Test all three maps.
    static void testInventorySize(int n) {

        System.out.println("\n================================");
        System.out.println("INVENTORY SIZE: " + n);
        System.out.println("================================");

        benchmark(
                "HashMap",
                HashMap::new,
                n
        );

        benchmark(
                "TreeMap",
                TreeMap::new,
                n
        );

        benchmark(
                "LinkedHashMap",
                LinkedHashMap::new,
                n
        );
    }

    public static void main(String[] args) {

        verifyOrdering();

        testInventorySize(1000);

        testInventorySize(10000);

        testInventorySize(50000);

        System.out.println("\nAll experiments completed.");
    }
}
