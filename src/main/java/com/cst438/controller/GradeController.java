package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class GradeController {
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     instructor lists the grades for an assignment for all enrolled students
     returns the list of grades (ordered by student name) for the assignment
     if there is no grade entity for an enrolled student, a grade entity with null grade is created
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId, Principal principal) {

        User instructor = userRepository.findByEmail(principal.getName());

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "assignment not found");
        }
        int sectionNo = assignment.getSection().getSectionNo();

        // Check if the instructor is associated with the section
        if (!assignment.getSection().getInstructorEmail().equals(instructor.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned instructor can view the grades for this section");
        }

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        List<GradeDTO> gradeDTOList = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignmentId);
            if (grade == null) { // CHANGED: Check if grade exists
                grade = new Grade(); // CHANGED: Instantiate new Grade
                grade.setEnrollment(enrollment); // CHANGED: Associate the enrollment
                grade.setAssignment(assignment);  // CHANGED: Associate the assignment
                grade.setScore(null);             // CHANGED: Set score to null
                grade = gradeRepository.save(grade); // CHANGED: Save the new grade entity
            }
            GradeDTO gradeDTO = new GradeDTO(
                    grade.getGradeId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    assignment.getTitle(),
                    assignment.getSection().getCourse().getCourseId(),
                    assignment.getSection().getSecId(),
                    grade.getScore()
            );
            gradeDTOList.add(gradeDTO);
        }
        return gradeDTOList;
    }
    /**
     instructor updates one or more assignment grades
     only the score attribute of grade entity can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/grades")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateGrades(@RequestBody List<GradeDTO> dlist, Principal principal) {

        User instructor = userRepository.findByEmail(principal.getName());

        for (GradeDTO grade : dlist) {
            Grade g = gradeRepository.findById(grade.gradeId()).orElseThrow(() -> new RuntimeException("Grade not found for id: " + grade.gradeId()));

            Assignment assignment = g.getAssignment();

            if(!assignment.getSection().getInstructorEmail().equalsIgnoreCase(instructor.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned instructor can update the grades for this section");
            }

            g.setScore(grade.score());
            gradeRepository.save(g);
        }
    }
}
