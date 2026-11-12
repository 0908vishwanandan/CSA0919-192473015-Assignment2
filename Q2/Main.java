import java.util.*;
import java.util.function.Supplier;

class Song {

    int id;
    String title;

    Song(int id) {
        this.id = id;
        this.title = "Song" + id;
    }
}

public class Main {

    static volatile long result;

    // Create an ArrayList with n songs
    static List<Song> createArrayList(int n) {

        List<Song> list = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            list.add(new Song(i));
        }

        return list;
    }

    // Create a LinkedList with n songs
    static List<Song> createLinkedList(int n) {

        List<Song> list = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            list.add(new Song(i));
        }

        return list;
    }

    // Generate additional songs
    static Song[] createExtraSongs(int n) {

        Song[] songs = new Song[n];

        for (int i = 0; i < n; i++) {
            songs[i] = new Song(100000 + i);
        }

        return songs;
    }

    // Measure adding songs at the end
    static long addEnd(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        Song[] extra = createExtraSongs(n);

        long start = System.nanoTime();

        for (Song song : extra) {
            list.add(song);
        }

        return System.nanoTime() - start;
    }

    // Measure insertion at the front
    static long insertFront(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        int operations = Math.min(n, 500);

        Song[] extra = createExtraSongs(operations);

        long start = System.nanoTime();

        for (Song song : extra) {
            list.add(0, song);
        }

        return System.nanoTime() - start;
    }

    // Measure insertion in the middle
    static long insertMiddle(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        int operations = Math.min(n, 500);

        Song[] extra = createExtraSongs(operations);

        long start = System.nanoTime();

        for (Song song : extra) {

            int middle = list.size() / 2;

            list.add(middle, song);
        }

        return System.nanoTime() - start;
    }

    // Measure removal from the front
    static long removeFront(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        int operations = Math.min(n, 500);

        long start = System.nanoTime();

        for (int i = 0; i < operations; i++) {
            list.remove(0);
        }

        return System.nanoTime() - start;
    }

    // Measure removal from the middle
    static long removeMiddle(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        int operations = Math.min(n, 500);

        long start = System.nanoTime();

        for (int i = 0; i < operations; i++) {

            int middle = list.size() / 2;

            list.remove(middle);
        }

        return System.nanoTime() - start;
    }

    // Measure accessing the k-th song
    static long accessK(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        long start = System.nanoTime();

        for (int i = 0; i < 1000; i++) {

            Song song = list.get(n / 2);

            result += song.id;
        }

        return System.nanoTime() - start;
    }

    // Measure iteration through entire playlist
    static long iterate(
            Supplier<List<Song>> factory, int n) {

        List<Song> list = factory.get();

        long start = System.nanoTime();

        for (int r = 0; r < 5; r++) {

            for (Song song : list) {
                result += song.id;
            }
        }

        return System.nanoTime() - start;
    }

    // Print both timings
    static void compare(
            String operation,
            long arrayTime,
            long linkedTime) {

        System.out.println("\n" + operation);

        System.out.printf(
                "ArrayList: %.3f ms%n",
                arrayTime / 1_000_000.0
        );

        System.out.printf(
                "LinkedList: %.3f ms%n",
                linkedTime / 1_000_000.0
        );
    }

    // Run all experiments
    static void test(int n) {

        System.out.println("\n================================");
        System.out.println("PLAYLIST SIZE: " + n);
        System.out.println("================================");

        compare("Add at End",
                addEnd(() -> createArrayList(n), n),
                addEnd(() -> createLinkedList(n), n));

        compare("Insert at Front",
                insertFront(() -> createArrayList(n), n),
                insertFront(() -> createLinkedList(n), n));

        compare("Insert in Middle",
                insertMiddle(() -> createArrayList(n), n),
                insertMiddle(() -> createLinkedList(n), n));

        compare("Remove from Front",
                removeFront(() -> createArrayList(n), n),
                removeFront(() -> createLinkedList(n), n));

        compare("Remove from Middle",
                removeMiddle(() -> createArrayList(n), n),
                removeMiddle(() -> createLinkedList(n), n));

        compare("Access k-th Song",
                accessK(() -> createArrayList(n), n),
                accessK(() -> createLinkedList(n), n));

        compare("Iterate Entire Playlist",
                iterate(() -> createArrayList(n), n),
                iterate(() -> createLinkedList(n), n));
    }

    public static void main(String[] args) {

        // Three different playlist sizes
        test(1000);

        test(10000);

        test(50000);

        System.out.println("\nAll experiments completed.");
    }
}
