import java.util.Comparator;
import java.util.PriorityQueue;

class Patient {

    final String name;
    final int severity;
    final long arrival;

    Patient(String name, int severity, long arrival) {

        this.name = name;
        this.severity = severity;
        this.arrival = arrival;
    }

    @Override
    public String toString() {
        return name + "(S" + severity + ")";
    }
}

class Triage {

    private long counter = 0;

    // TODO 1: Create PriorityQueue with Comparator
    private PriorityQueue<Patient> queue =
            new PriorityQueue<>(
                Comparator.comparingInt(
                    (Patient p) -> p.severity
                ).thenComparingLong(
                    p -> p.arrival
                )
            );

    // Admit a patient
    void admit(String name, int severity) {

        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException(
                "Severity must be between 1 and 5"
            );
        }

        queue.add(
            new Patient(name, severity, counter++)
        );
    }

    // Remove the highest-priority patient
    Patient next() {

        return queue.poll();
    }

    // Display patients in correct treatment order
    void displayWaitingList() {

        PriorityQueue<Patient> copy =
                new PriorityQueue<>(queue);

        while (!copy.isEmpty()) {

            System.out.println(copy.poll());
        }
    }
}

public class Main {

    public static void main(String[] args) {

        Triage er = new Triage();

        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);

        StringBuilder order = new StringBuilder();

        Patient p;

        while ((p = er.next()) != null) {

            order.append(p).append(' ');
        }

        System.out.println(order.toString().trim());
    }
}
