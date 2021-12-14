package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Employee extends Model {

    private Long employeeId;
    private Long reportsTo;
    private String firstName;
    private String lastName;
    private String email;
    private String title;

    public Employee() {
        // new employee for insert
        this.firstName = "";
        this.lastName = "";
        this.title = "";
        this.email = "";
        this.reportsTo = (long)(0);
    }

    private Employee(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        email = results.getString("Email");
        employeeId = results.getLong("EmployeeId");
        reportsTo = results.getLong("ReportsTo");
        title = results.getString("Title");
    }



    // method to verify the data given is valid
    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (firstName == null || "".equals(firstName)) {
            addError("FirstName can't be null or blank!");
        }
        if (lastName == null || "".equals(lastName)) {
            addError("LastName can't be null!");
        }
        if(!email.contains("@")){
            addError("Not a valid email");
        }
        return !hasErrors();
    }

    @Override
    public boolean update() { // method that updates an employees inforation based on the Id
        if (verify()) { // using the verify method
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement( // SQL statement for the update
                         "UPDATE employees SET FirstName=?, LastName=?, Email=? WHERE EmployeeId=?")) {
                stmt.setString(1, this.getFirstName()); // filling in the ? marks in the statement
                stmt.setString(2, this.getLastName());
                stmt.setString(3, this.getEmail());
                stmt.setLong(4, this.getEmployeeId());
                stmt.executeUpdate(); // executing the statement
                return true; // then I return true if it was a success and false otherwise
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean create() { // method that creates a new employee
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement( // SQL statement for inserting a new employee in
                         "INSERT INTO employees (FirstName, LastName, Email, Title, ReportsTo) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setString(1, this.firstName);
                stmt.setString(2, this.lastName);
                stmt.setString(3, this.email); // setting the values for the ? in the statement
                stmt.setString(4, this.title);
                stmt.setLong(5, this.reportsTo);


                stmt.executeUpdate(); // executing the statment
                employeeId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public void delete() { // delete method that will delete an employee
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM employees WHERE EmployeeID=?")) { // SQL statment
            stmt.setLong(1, this.employeeId);
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public List<Customer> getCustomers() {
        return Customer.forEmployee(employeeId);
    }

    public Long getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(Long reportsTo) {
        this.reportsTo = reportsTo;
    }


    // this is a get method that gets all the employees based on how they report to and returns
    // them as a List of employees
    public List<Employee> getReports() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees WHERE ReportsTo=?"
             )) {
            stmt.setLong(1, this.getEmployeeId());
            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // get method that returns the boss of the employee
    public Employee getBoss() {
        //TODO implement
        return Employee.find(this.reportsTo);
    }

    public static List<Employee> all() {
        return all(0, Integer.MAX_VALUE);
    }

    // this is where paging is implemented
    public static List<Employee> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * page - count);
            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Employee> getAll(){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees"
             )) {
            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // this method finds an employee based on their email
    public static Employee findByEmail(String newEmailAddress) {
        try (Connection conn = DB.connect();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE Email=?")) {
            stmt.setString(1, newEmailAddress);
            ResultSet results = stmt.executeQuery();
            return new Employee(results);
        }catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        // throw new UnsupportedOperationException("Implement me");
    }

    public static Employee find(long employeeId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE EmployeeId=?")) {
            stmt.setLong(1, employeeId);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Employee(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public void setTitle(String programmer) {
        title = programmer;
    }

    // this method is a set method for the employee reports
    public void setReportsTo(Employee employee) {
        // TODO implement
        this.reportsTo = employee.getEmployeeId();
    }

    public static class SalesSummary {
        private String firstName;
        private String lastName;
        private String email;
        private Long salesCount;
        private BigDecimal salesTotals;
        private SalesSummary(ResultSet results) throws SQLException {
            firstName = results.getString("FirstName");
            lastName = results.getString("LastName");
            email = results.getString("Email");
            salesCount = results.getLong("SalesCount");
            salesTotals = results.getBigDecimal("SalesTotal");
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public Long getSalesCount() {
            return salesCount;
        }

        public BigDecimal getSalesTotals() {
            return salesTotals;
        }
    }

    // this method gets the name of the employee and the total sales that he has done
    // the totals are then stored in the sales summary class
    public static List<Employee.SalesSummary> getSalesSummaries() {
        //TODO - a GROUP BY query to determine the sales (look at the invoices table), using the SalesSummary class
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT employees.FirstName as firstName, employees.LastName as lastName," +
                             "employees.Email as email, COUNT(invoices.CustomerId) as salesCount," +
                             "SUM(invoices.Total) as SalesTotal FROM invoices " +
                             "JOIN customers on customers.customerId = invoices.CustomerId " +
                             "JOIN employees on employees.EmployeeId = customers.SupportRepId " +
                             "GROUP BY employees.Email"
             )) {

            ResultSet results = stmt.executeQuery();
            List<SalesSummary> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new SalesSummary(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }
}
