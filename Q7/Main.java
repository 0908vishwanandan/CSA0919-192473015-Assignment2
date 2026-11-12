import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Team {

    final String name;
    final int points;
    final int goalDiff;

    Team(String name, int points, int goalDiff) {

        this.name = name;
        this.points = points;
        this.goalDiff = goalDiff;
    }

    @Override
    public String toString() {

        return name + "(" + points + "," + goalDiff + ")";
    }
}

// ORIGINAL LEADERBOARD
class Leaderboard<T> {

    private final List<T> items = new ArrayList<>();

    private final Comparator<? super T> order;

    private int sortCount = 0;

    Leaderboard(Comparator<? super T> order) {

        this.order = order;
    }

    void add(T item) {

        items.add(item);
    }

    // Original method sorts every time
    List<T> ranking() {

        List<T> sortedItems = new ArrayList<>(items);

        sortedItems.sort(order);

        sortCount++;

        return sortedItems;
    }

    int getSortCount() {

        return sortCount;
    }
}

// REDESIGNED CACHED LEADERBOARD
class CachedLeaderboard<T> {

    private final List<T> items = new ArrayList<>();

    private final Comparator<? super T> order;

    private List<T> cachedRanking = null;

    private List<T> cachedView = null;

    private int sortCount = 0;

    CachedLeaderboard(Comparator<? super T> order) {

        this.order = order;
    }

    // Add team and invalidate the cache
    void add(T item) {

        items.add(item);

        cachedRanking = null;

        cachedView = null;
    }

    // Sort only when the cache is unavailable
    List<T> ranking() {

        if (cachedRanking == null) {

            cachedRanking = new ArrayList<>(items);

            cachedRanking.sort(order);

            cachedView = Collections.unmodifiableList(
                    cachedRanking
            );

            sortCount++;
        }

        return cachedView;
    }

    int getSortCount() {

        return sortCount;
    }
}

// Main class
public class Main {

    static volatile int result;

    // Generate teams for testing
    static List<Team> createTeams(int n) {

        List<Team> teams = new ArrayList<>();

        for (int i = 0; i < n; i++) {

            String name = "Team" + i;

            int points = (i * 37) % 101;

            int goalDiff = (i * 17) % 51;

            teams.add(
                new Team(name, points, goalDiff)
            );
        }

        return teams;
    }

    // Test correctness of both implementations
    static void correctnessTest(
            Comparator<Team> order) {

        Leaderboard<Team> original =
                new Leaderboard<>(order);

        CachedLeaderboard<Team> redesigned =
                new CachedLeaderboard<>(order);

        List<Team> teams = new ArrayList<>();

        teams.add(new Team("Panthers", 9, 4));

        teams.add(new Team("Eagles", 9, 6));

        teams.add(new Team("Cobras", 6, 1));

        teams.add(new Team("Bisons", 9, 4));

        for (Team team : teams) {

            original.add(team);

            redesigned.add(team);
        }

        System.out.println("CORRECTNESS TEST");

        System.out.println(
                "Original Ranking: " + original.ranking()
        );

        System.out.println(
                "Redesigned Ranking: " + redesigned.ranking()
        );
    }

    // Measure original solution
    static long measureOriginal(
            List<Team> teams,
            Comparator<Team> order,
            int requests) {

        Leaderboard<Team> board =
                new Leaderboard<>(order);

        for (Team team : teams) {
            board.add(team);
        }

        long start = System.nanoTime();

        for (int i = 0; i < requests; i++) {

            List<Team> ranking = board.ranking();

            result += ranking.get(0).points;
        }

        long end = System.nanoTime();

        System.out.println(
                "Original sorting operations: "
                + board.getSortCount()
        );

        return end - start;
    }

    // Measure redesigned solution
    static long measureRedesigned(
            List<Team> teams,
            Comparator<Team> order,
            int requests) {

        CachedLeaderboard<Team> board =
                new CachedLeaderboard<>(order);

        for (Team team : teams) {
            board.add(team);
        }

        long start = System.nanoTime();

        for (int i = 0; i < requests; i++) {

            List<Team> ranking = board.ranking();

            result += ranking.get(0).points;
        }

        long end = System.nanoTime();

        System.out.println(
                "Redesigned sorting operations: "
                + board.getSortCount()
        );

        return end - start;
    }

    // Compare original and redesigned systems
    static void test(int n, Comparator<Team> order) {

        int requests = 10;

        List<Team> teams = createTeams(n);

        System.out.println("\n================================");

        System.out.println("NUMBER OF TEAMS: " + n);

        System.out.println("RANKING REQUESTS: " + requests);

        System.out.println("================================");

        long originalTime =
                measureOriginal(teams, order, requests);

        long redesignedTime =
                measureRedesigned(teams, order, requests);

        System.out.printf(
                "Original Time: %.3f ms%n",
                originalTime / 1_000_000.0
        );

        System.out.printf(
                "Redesigned Time: %.3f ms%n",
                redesignedTime / 1_000_000.0
        );

        System.out.println(
                "Original copied references: "
                + (long) n * requests
        );

        System.out.println(
                "Redesigned copied references: "
                + n
        );
    }

    public static void main(String[] args) {

        Comparator<Team> byRank =
                Comparator.comparingInt(
                        (Team t) -> t.points
                ).reversed()

                .thenComparing(
                        Comparator.comparingInt(
                                (Team t) -> t.goalDiff
                        ).reversed()
                )

                .thenComparing(t -> t.name);

        correctnessTest(byRank);

        test(1000, byRank);

        test(10000, byRank);

        test(50000, byRank);

        System.out.println("\nRedesign testing completed.");
    }
}
