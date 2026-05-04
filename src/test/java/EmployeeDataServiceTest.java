import org.example.EmployeeDataReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EmployeeDataServiceTest {


    private EmployeeDataReportService service;

    @BeforeEach
    void setUp() {
	service = new EmployeeDataReportService();
    }

    @Test
    void employeeReportGeneration_doesNotThrow() {
	assertDoesNotThrow(() -> service.employeeReportGeneration());
    }
}
