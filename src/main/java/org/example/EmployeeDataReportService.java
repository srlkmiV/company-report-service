package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmployeeDataReportService {
    public void employeeReportGeneration() {

	try {
	    Path employeeDataFile = Path.of(
		    Objects.requireNonNull(getClass().getClassLoader().getResource("testfile.csv")).toURI());
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap = new HashMap<>();
	    Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap = fetchEmployeeDataFromCSVFile(employeeDataFile,
		    employeeIdEmployeeDTOMap);
	    if (!managerReportingEmployeeMap.isEmpty()) {
		employeeReportGeneration(managerReportingEmployeeMap, employeeIdEmployeeDTOMap);
	    }
	} catch (Exception e) {
	    System.out.println("Failed to read the file,{}" + e.getMessage());
	}

    }

    private void employeeReportGeneration(Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	for (Map.Entry<Long, List<EmployeeDTO>> entry : managerReportingEmployeeMap.entrySet()) {
	    Long managerId = entry.getKey();
	    if (managerId == null)
		continue;
	    checkSalaryRangeOfManagers(managerId, entry.getValue(), employeeIdEmployeeDTOMap);
	    checkTheReportingLineMoreThanFour(entry, managerReportingEmployeeMap, employeeIdEmployeeDTOMap);
	}

    }

    private void checkTheReportingLineMoreThanFour(Map.Entry<Long, List<EmployeeDTO>> entry,
	    Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap, Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	Map<Long, Integer> employeeReportingLineCount = new HashMap<>();
	Set<Long> employeeCheck = new HashSet<>();
	int depth = reportingLineCountCheck(entry.getKey(), managerReportingEmployeeMap, employeeReportingLineCount,
		employeeCheck);
	EmployeeDTO manager = employeeIdEmployeeDTOMap.get(entry.getKey());
	if (depth > 4) {
	    int excess = depth - 4;
	    System.out.printf("Manager %s %s has reporting depth %d (>4 by %d)%n", manager.getFirstName(),
		    manager.getLastName(), depth, excess);
	}

    }

    private int reportingLineCountCheck(Long managerId, Map<Long, List<EmployeeDTO>> directsByManager,
	    Map<Long, Integer> employeeReportingLineCount, Set<Long> employeeCheck) {

	if (employeeReportingLineCount.containsKey(managerId)) {
	    return employeeReportingLineCount.get(managerId);
	}

	if (!employeeCheck.add(managerId)) {
	    return 0;
	}

	List<EmployeeDTO> directs = directsByManager.getOrDefault(managerId, List.of());
	int maxChildDepth = 0;

	for (EmployeeDTO e : directs) {
	    int childDepth = reportingLineCountCheck(e.getId(), directsByManager, employeeReportingLineCount,
		    employeeCheck);
	    maxChildDepth = Math.max(maxChildDepth, childDepth);
	}

	employeeCheck.remove(managerId);

	int result = directs.isEmpty() ? 0 : (1 + maxChildDepth);
	employeeReportingLineCount.put(managerId, result);
	return result;
    }

    private void checkSalaryRangeOfManagers(Long managerId, List<EmployeeDTO> employeeDTOList,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	String managerFirstName = employeeIdEmployeeDTOMap.get(managerId).getFirstName();
	String managerLastName = employeeIdEmployeeDTOMap.get(managerId).getLastName();

	Double averageSalary = averageSalaryOfDirectSuboordinates(employeeDTOList);
	Double managerSalary = Double.valueOf(employeeIdEmployeeDTOMap.get(managerId).getSalary());
	Double percentageDiff = computePercentageDifference(managerSalary, averageSalary);
	Double salaryMargin = 0.0;
	if (percentageDiff < 20) {
	    salaryMargin = averageSalary * 20 / 100;
	    System.out.printf("Manager %s %s earns less than they should by %.2f. %n", managerFirstName,
		    managerLastName, Math.max(managerSalary, averageSalary + salaryMargin) - Math.min(managerSalary,
			    managerSalary + salaryMargin));
	} else if (percentageDiff > 50) {
	    salaryMargin = averageSalary * 50 / 100;
	    System.out.printf("Manager %s %s earns more than they should by %.2f. %n", managerFirstName,
		    managerLastName,
		    Math.max(salaryMargin + averageSalary, managerSalary) - Math.min(salaryMargin + averageSalary,
			    managerSalary));
	}
    }

    private Double computePercentageDifference(Double managerSalary, Double averageSalary) {
	Double salaryDiff = Math.max(managerSalary, averageSalary) - Math.min(managerSalary, averageSalary);
	return (salaryDiff / averageSalary) * 100;
    }

    private Double averageSalaryOfDirectSuboordinates(List<EmployeeDTO> employeeDTOList) {

	return employeeDTOList.stream().mapToDouble(EmployeeDTO::getSalary).average().orElse(0.0);

    }

    private Map<Long, List<EmployeeDTO>> fetchEmployeeDataFromCSVFile(Path employeeDataFile,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap = new HashMap<>();
	//Reading excel file and creating employeeDTO and manager reporting employee map
	try (var rows = Files.lines(employeeDataFile)) {
	    rows.skip(1).map(row -> row.split(",")).forEach(col -> {

		EmployeeDTO employeeDTO = new EmployeeDTO(Long.valueOf(col[0]), col[1], col[2], Long.valueOf(col[3]),
			(col.length > 4) ? Long.parseLong(col[4]) : null);

		employeeIdEmployeeDTOMap.put(employeeDTO.getId(), employeeDTO);

		List<EmployeeDTO> reportingEmpIds = new ArrayList<>();
		if (managerReportingEmployeeMap.containsKey(employeeDTO.getManagerId())) {
		    reportingEmpIds = managerReportingEmployeeMap.get(employeeDTO.getManagerId());
		    reportingEmpIds.add(employeeDTO);
		    managerReportingEmployeeMap.put(employeeDTO.getManagerId(), reportingEmpIds);
		} else {
		    reportingEmpIds.add(employeeDTO);
		    managerReportingEmployeeMap.put(employeeDTO.getManagerId(), reportingEmpIds);
		}
	    });

	} catch (Exception e) {
	    System.out.println("Failed to read the file,{}" + e.getMessage());
	}
	return managerReportingEmployeeMap;
    }
}
