import java.util.ArrayDeque;

public class Main {

    public static void main(String[] args) {

        ArrayDeque<String> upNext = new ArrayDeque<>();

        // Add songs at the end
        upNext.addLast("Song A");
        upNext.addLast("Song B");

        // Add a song at the beginning
        upNext.addFirst("Song X");

        System.out.println(upNext);

        // Remove the first song
        System.out.println("Playing: " + upNext.removeFirst());

        System.out.println("Remaining Songs: " + upNext);
    }
}
