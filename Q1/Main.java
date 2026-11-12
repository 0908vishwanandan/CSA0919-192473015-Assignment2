import java.util.ArrayList;
import java.util.List;

public class Main {

    interface TicketBuilder {
        String build(String[] fields);
    }

    static volatile long resultHolder;

    // Method 1: Using String
    static String usingString(String[] fields) {

        String ticket = "";

        for (int i = 0; i < fields.length; i++) {
            ticket = ticket + (i == 0 ? "" : " | ") + fields[i];
        }

        return ticket;
    }

    // Method 2: Using StringBuilder
    static String usingStringBuilder(String[] fields) {

        StringBuilder ticket = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {

            if (i > 0) {
                ticket.append(" | ");
            }

            ticket.append(fields[i]);
        }

        return ticket.toString();
    }

    // Method 3: Using StringBuffer
    static String usingStringBuffer(String[] fields) {

        StringBuffer ticket = new StringBuffer();

        for (int i = 0; i < fields.length; i++) {

            if (i > 0) {
                ticket.append(" | ");
            }

            ticket.append(fields[i]);
        }

        return ticket.toString();
    }

    // Generate test data
    static String[][] createWorkload(int tickets, int fieldCount) {

        String[][] data = new String[tickets][fieldCount];

        for (int i = 0; i < tickets; i++) {

            data[i][0] = "PNR:" + i;

            for (int j = 1; j < fieldCount; j++) {
                data[i][j] = "FIELD" + j + ":VALUE";
            }
        }

        return data;
    }

    // Measure execution time
    static long measureTime(
            String[][] data, TicketBuilder builder) {

        long checksum = 0;

        long start = System.nanoTime();

        for (String[] fields : data) {

            String ticket = builder.build(fields);

            checksum += ticket.length();
        }

        long end = System.nanoTime();

        resultHolder += checksum;

        return end - start;
    }

    // Calculate used heap memory
    static long usedMemory() {

        Runtime runtime = Runtime.getRuntime();

        return runtime.totalMemory() - runtime.freeMemory();
    }

    // Measure average execution time
    static double averageTime(
            String[][] data, TicketBuilder builder) {

        for (int i = 0; i < 2; i++) {
            measureTime(data, builder);
        }

        int repetitions = 5;

        long total = 0;

        for (int i = 0; i < repetitions; i++) {
            total += measureTime(data, builder);
        }

        return total / (double) repetitions / 1_000_000.0;
    }

    // Measure retained output memory approximately
    static long measureMemory(
            String[][] data, TicketBuilder builder)
            throws InterruptedException {

        System.gc();
        Thread.sleep(150);

        long before = usedMemory();

        List<String> results = new ArrayList<>();

        for (String[] fields : data) {
            results.add(builder.build(fields));
        }

        long after = usedMemory();

        if (results.size() != data.length) {
            throw new IllegalStateException("Invalid result count");
        }

        return Math.max(0, after - before);
    }

    // Run all three approaches
    static void testWorkload(
            String name, int tickets, int fields)
            throws InterruptedException {

        String[][] data = createWorkload(tickets, fields);

        System.out.println("\n" + name);
        System.out.println("Tickets: " + tickets);
        System.out.println("Fields per ticket: " + fields);

        double stringTime =
                averageTime(data, Main::usingString);

        double builderTime =
                averageTime(data, Main::usingStringBuilder);

        double bufferTime =
                averageTime(data, Main::usingStringBuffer);

        System.out.println("\nAverage Execution Time:");

        System.out.println("String: " + stringTime + " ms");

        System.out.println("StringBuilder: " + builderTime + " ms");

        System.out.println("StringBuffer: " + bufferTime + " ms");

        System.out.println("\nApproximate Retained Memory:");

        System.out.println(
                "String: " +
                measureMemory(data, Main::usingString) / 1024 +
                " KB"
        );

        System.out.println(
                "StringBuilder: " +
                measureMemory(data, Main::usingStringBuilder) / 1024 +
                " KB"
        );

        System.out.println(
                "StringBuffer: " +
                measureMemory(data, Main::usingStringBuffer) / 1024 +
                " KB"
        );
    }

    public static void main(String[] args)
            throws InterruptedException {

        testWorkload("WORKLOAD 1 - SMALL TICKET", 1, 6);

        testWorkload("WORKLOAD 2 - LARGE TICKET GENERATION",
                10000, 50);

        testWorkload("WORKLOAD 3 - MANY SHORT TICKETS",
                50000, 10);
    }
}
