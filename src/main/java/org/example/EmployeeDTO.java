package org.example;

public class EmployeeDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private Long salary;
    private Long managerId;

    public EmployeeDTO(Long id, String firstName, String lastName, Long salary, Long managerId) {
	this.id = id;
	this.firstName = firstName;
	this.lastName = lastName;
	this.salary = salary;
	this.managerId = managerId;
    }

    public Long getId() {
	return id;
    }

    public Long getManagerId() {
	return managerId;
    }

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public Long getSalary() {
	return salary;
    }

}
