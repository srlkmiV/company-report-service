package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmployeeDataReportService {
    public void employeeReportGeneration() {
	//Can have up to 1000 employees - done
	//employee details in csv file - done
	//Manager earns >= 20%, <=50% of the average salary of direct suboordinates - done
	//max employee + 4 levels = ceo
	//which managers earns less than they should and by how much - done
	//which managers earns more than they should and by how much - done
	//which managers have reporting line more than 4 and by how much

	Path employeeDataFile = Path.of("C:/Users/sajitha/Documents/Test/src/main/resources/testfile.csv");
	Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap = new HashMap<>();
	Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap = fetchEmployeeDataFromCSVFile(employeeDataFile,
		employeeIdEmployeeDTOMap);
	if (!managerReportingEmployeeMap.isEmpty()) {
	    managerSalaryCheck(managerReportingEmployeeMap, employeeIdEmployeeDTOMap);
	}

    }

    private void managerSalaryCheck(Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	for (Map.Entry<Long, List<EmployeeDTO>> entry : managerReportingEmployeeMap.entrySet()) {
	    Long managerId = entry.getKey();
	    if (managerId == null)
		continue;
	    checkTheReportingLineMoreThanFour(entry, managerReportingEmployeeMap,employeeIdEmployeeDTOMap);
	    checkSalaryRangeOfManagers(managerId, entry.getValue(), employeeIdEmployeeDTOMap);

	}

    }

    private void checkTheReportingLineMoreThanFour(Map.Entry<Long, List<EmployeeDTO>> entry,
	    Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap, Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	Map<Long, Integer> employeeReportingLineCount = new HashMap<>();
	Set<Long> employeeCheck = new HashSet<>();
	int depth = reportingLineCountCheck(entry.getKey(), managerReportingEmployeeMap, employeeReportingLineCount, employeeCheck);
	EmployeeDTO manager = employeeIdEmployeeDTOMap.get(entry.getKey());
	if (depth > 4) {
	    int excess = depth - 4;
	    System.out.printf(
		    "Manager %s %s has reporting depth %d (>4 by %d)%n",
		     manager.getFirstName(), manager.getLastName(), depth, excess
	    );
	}

    }


    private int reportingLineCountCheck(Long managerId,
	    Map<Long, List<EmployeeDTO>> directsByManager,
	    Map<Long, Integer> employeeReportingLineCount,
	    Set<Long> employeeCheck) {

	if (employeeReportingLineCount.containsKey(managerId)) {
	    return employeeReportingLineCount.get(managerId);
	}

	if (!employeeCheck.add(managerId)) {
	    // Cycle detected (bad data): treat as 0 to avoid infinite loop
	    return 0;
	}

	List<EmployeeDTO> directs = directsByManager.getOrDefault(managerId, List.of());
	int maxChildDepth = 0;

	for (EmployeeDTO e : directs) {
	    int childDepth = reportingLineCountCheck(e.getId(), directsByManager, employeeReportingLineCount, employeeCheck);
	    maxChildDepth = Math.max(maxChildDepth, childDepth);
	}

	employeeCheck.remove(managerId);

	int result = directs.isEmpty() ? 0 : (1 + maxChildDepth);
	employeeReportingLineCount.put(managerId, result);
	return result;
    }

    private void checkSalaryRangeOfManagers(Long managerId, List<EmployeeDTO> employeeDTOList,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	Double averageSalary = averageSalaryOfDirectSuboordinates(employeeDTOList);
	Double managerSalary = Double.valueOf(employeeIdEmployeeDTOMap.get(managerId).getSalary());
	Double percentageDiff = fetchSalaryPercentageDifference(managerSalary, averageSalary);
	String managerFirstName = employeeIdEmployeeDTOMap.get(managerId).getFirstName();
	String managerLastName = employeeIdEmployeeDTOMap.get(managerId).getLastName();
	if (percentageDiff < 20) {
	    System.out.printf("Manager %s %s earns less than they should by %.2f %n.", managerFirstName,
		    managerLastName, averageSalary - managerSalary);
	} else if (percentageDiff > 50) {
	    System.out.printf("Manager %s %s earns less than they should by %.2f %n.", managerFirstName,
		    managerLastName, managerSalary - averageSalary);
	}
    }

    private Double fetchSalaryPercentageDifference(Double managerSalary, Double averageSalary) {
	Double value = Math.max(managerSalary, averageSalary) - Math.min(managerSalary, averageSalary);
	return (value / averageSalary) * 100;
    }

    private Double averageSalaryOfDirectSuboordinates(List<EmployeeDTO> employeeDTOList) {

	return employeeDTOList.stream().mapToDouble(EmployeeDTO::getSalary).average().orElse(0.0);

    }

    private Map<Long, List<EmployeeDTO>> fetchEmployeeDataFromCSVFile(Path employeeDataFile,
	    Map<Long, EmployeeDTO> employeeIdEmployeeDTOMap) {
	Map<Long, List<EmployeeDTO>> managerReportingEmployeeMap = new HashMap<>();
	try (var rows = Files.lines(employeeDataFile)) {
	    rows.skip(1).map(row -> row.split(",")).forEach(col -> {
		List<EmployeeDTO> reportingEmpIds = new ArrayList<>();
		EmployeeDTO employeeDTO = new EmployeeDTO(Long.valueOf(col[0]), col[1], col[2], Long.valueOf(col[3]),
			(col.length > 4) ? Long.parseLong(col[4]) : null);
		employeeIdEmployeeDTOMap.put(employeeDTO.getId(), employeeDTO);
		if (managerReportingEmployeeMap.containsKey(employeeDTO.getManagerId())) {
		    reportingEmpIds = managerReportingEmployeeMap.get(employeeDTO.getManagerId());
		    reportingEmpIds.add(employeeDTO);
		    managerReportingEmployeeMap.put(employeeDTO.getManagerId(), reportingEmpIds);
		} else {
		    reportingEmpIds.add(employeeDTO);
		    managerReportingEmployeeMap.put(employeeDTO.getManagerId(), reportingEmpIds);
		}
		System.out.println(employeeDTO.getId() + " -> " + employeeDTO.getFirstName());
	    });

	} catch (Exception e) {
	    //	    e.printStackTrace();
	    System.out.println("Failed to read the file,{}" + e.getMessage());
	}
	return managerReportingEmployeeMap;
    }
}
