package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmployeeHelper {
    static List<Employee> allEmployees = Employee.getAll();
    private static int number;

    public static String makeEmployeeTree() {
        // TODO, change this to use a single query operation to get all employees

        Employee employee = allEmployees.get(0);

        Map<Long, List<Employee>> employeeMap = new HashMap<>();

        for(int i=0; i < allEmployees.size(); i++){
            Employee curBoss = allEmployees.get(i);
            List<Employee> reportList = new LinkedList<>();
            for(int j=i+1; j<allEmployees.size(); j++ ){
                if(curBoss.getEmployeeId().equals(allEmployees.get(j).getReportsTo())){
                    reportList.add(allEmployees.get(j));
                }
            }
            employeeMap.put(curBoss.getEmployeeId(), reportList);
        }
        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just uses the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        StringBuilder list = new StringBuilder("<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>");
        List<Employee> reports = employeeMap.get(employee.getEmployeeId());
        if (reports != null) {
            for (Employee report : reports) {
                list.append(makeTree(report, employeeMap));
            }
        } else{
            number += 1;
            makeTree(allEmployees.get(number), employeeMap);
        }
        return list + "</ul></li>";
    }
}
