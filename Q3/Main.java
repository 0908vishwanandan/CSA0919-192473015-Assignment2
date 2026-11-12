import java.util.HashSet;
import java.util.Set;

class Student {

    String regNo;
    String name;

    Student(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Student)) {
            return false;
        }

        return regNo.equals(((Student) o).regNo);
    }

    // hashCode() is not overridden
}

public class Main {

    public static void main(String[] args) {

        Set<Student> present = new HashSet<>();

        present.add(new Student("192311001", "Arun"));

        present.add(new Student("192311002", "Divya"));

        present.add(new Student("192311001", "Arun"));

        System.out.println("Unique students: " + present.size());
    }
}
