package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.model.Attendance;
import org.example.proyecto_baseee.model.Employee;
import org.example.proyecto_baseee.repository.AttendanceRepository;
import org.example.proyecto_baseee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HrService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final org.example.proyecto_baseee.repository.EmployeeDocumentRepository documentRepository;

    public HrService(EmployeeRepository employeeRepository, 
                     AttendanceRepository attendanceRepository,
                     org.example.proyecto_baseee.repository.EmployeeDocumentRepository documentRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.documentRepository = documentRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee addEmployee(Employee employee) {
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }
        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee employee = getEmployeeById(id);
        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setPosition(updatedEmployee.getPosition());
        employee.setSalary(updatedEmployee.getSalary());
        employee.setUsername(updatedEmployee.getUsername());
        return employeeRepository.save(employee);
    }

    public Employee changeEmployeeStatus(Long id, String status) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(status.toUpperCase());
        return employeeRepository.save(employee);
    }

    public Attendance registerAttendance(Long employeeId, String status, String registeredBy) {
        if (attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Ya se registró una asistencia, retardo o falta para este empleado el día de hoy."
            );
        }
        
        Employee employee = getEmployeeById(employeeId);
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.now());
        attendance.setStatus(status.toUpperCase());
        attendance.setRegisteredBy(registeredBy);
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getEmployeeAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public org.example.proyecto_baseee.model.EmployeeDocument saveDocument(Long employeeId, String docType, String fileName, String contentType, byte[] data) {
        Employee employee = getEmployeeById(employeeId);
        org.example.proyecto_baseee.model.EmployeeDocument doc = documentRepository.findByEmployeeIdAndDocumentType(employeeId, docType)
                .orElse(new org.example.proyecto_baseee.model.EmployeeDocument());
        
        doc.setEmployee(employee);
        doc.setDocumentType(docType);
        doc.setFileName(fileName);
        doc.setContentType(contentType);
        doc.setData(data);
        return documentRepository.save(doc);
    }

    public List<org.example.proyecto_baseee.model.EmployeeDocument> getEmployeeDocuments(Long employeeId) {
        return documentRepository.findByEmployeeId(employeeId);
    }

    public org.example.proyecto_baseee.model.EmployeeDocument getDocument(Long employeeId, String documentType) {
        return documentRepository.findByEmployeeIdAndDocumentType(employeeId, documentType)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
}
