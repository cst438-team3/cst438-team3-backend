package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    /**
     students lists there enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/enrollments")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           @RequestParam("studentId") int studentId) {

       //check if studentId exists
       User student = userRepository.findById(studentId)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + studentId));

       //check if year & semester are valid
       if(year < 1900 || year > 2100) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 1900 and 2100.");
       }

       if(!("spring".equalsIgnoreCase(semester) || "fall".equalsIgnoreCase(semester))) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester must be Spring or Fall.");
       }

       List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);
       List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

       for(Enrollment e : enrollments) {
           enrollmentDTOs.add(new EnrollmentDTO(
                   e.getEnrollmentId(),
                   e.getGrade(),
                   e.getStudent().getId(),
                   e.getStudent().getName(),
                   e.getStudent().getEmail(),
                   e.getSection().getCourse().getCourseId(),
                   e.getSection().getCourse().getTitle(),
                   e.getSection().getSecId(),
                   e.getSection().getSectionNo(),
                   e.getSection().getBuilding(),
                   e.getSection().getRoom(),
                   e.getSection().getTimes(),
                   e.getSection().getCourse().getCredits(),
                   e.getSection().getTerm().getYear(),
                   e.getSection().getTerm().getSemester()
           ));
       }

       return enrollmentDTOs;
   }

    /**
     students lists there assignments given year and semester value
     returns list of assignments may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/assignments")
   public List<AssignmentStudentDTO> getStudentAssignments(
       @RequestParam("studentId") int studentId,
       @RequestParam("year") int year,
       @RequestParam("semester") String semester) {

       // return a list of assignments and (if they exist) the assignment grade
       //  for all sections that the student is enrolled for the given year and semester
       //  hint: use the assignment repository method findByStudentIdAndYearAndSemesterOrderByDueDate

       //check if studentId exists
       User student = userRepository.findById(studentId)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + studentId));

       //check if year & semester are valid
       if(year < 1900 || year > 2100) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 1900 and 2100.");
       }

       if(!("spring".equalsIgnoreCase(semester) || "fall".equalsIgnoreCase(semester))) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester must be Spring or Fall.");
       }

       List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
       List<AssignmentStudentDTO> assignmentDTOS = new ArrayList<>();

       for (Assignment a : assignments) {
           Optional<Grade> grade = gradeRepository.findGradeByAssignmentAndStudent(a.getAssignmentId(), studentId);
           Integer score = null;
           if (grade.isPresent()) {
               score = grade.get().getScore();
           }

           assignmentDTOS.add(new AssignmentStudentDTO(
                   a.getAssignmentId(),
                   a.getTitle(),
                   a.getDueDate(),
                   a.getSection().getCourse().getCourseId(),
                   a.getSection().getSecId(),
                   score
           ));
       }

       return assignmentDTOS;
   }

}