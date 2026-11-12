import java.util.HashSet;
import java.util.Set;

class MutableStudent {

    String regNo;

    MutableStudent(String regNo) {
        this.regNo = regNo;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof MutableStudent)) {
            return false;
        }

        return regNo.equals(((MutableStudent) o).regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class Main {

    public static void main(String[] args) {

        Set<MutableStudent> students = new HashSet<>();

        MutableStudent s =
                new MutableStudent("A");

        students.add(s);

        System.out.println("Before change: " + students.contains(s));

        // Change registration number
        s.regNo = "B";

        System.out.println("After change: " + students.contains(s));

        System.out.println("Set size: " + students.size());
    }
}
