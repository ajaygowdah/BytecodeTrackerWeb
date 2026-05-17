public class SampleTest {

    // Safe method
    public void safeMethod() {
        System.out.println("Safe method executed");
    }

    // Dangerous method (name contains 'danger', analyzer will detect)
    public void dangerousMethod() {
        // Simulate a dangerous operation
        System.out.println("This method is considered dangerous by analyzer.");
    }
}
