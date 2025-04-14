package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentScheduleController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    GradebookServiceProxy gradebookServiceProxy;

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {
        List<EnrollmentDTO> transcript = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
            for (Enrollment e : enrollments) {
                transcript.add(new EnrollmentDTO(
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
        return transcript;
    }


    /**
     students enrolls into a section of a course
     returns the enrollment data including primary key
     logged in user must be the student (assignment 7)
     */
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId ) {
                Section section = sectionRepository.findById(sectionNo).orElse(null);
                if (section == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found: " + sectionNo);
                }
                Term term = section.getTerm();
                LocalDate addDate = term.getAddDate().toLocalDate();
                LocalDate addDeadline = term.getAddDeadline().toLocalDate();
                LocalDate today = LocalDate.now();
            
                if (today.isBefore(addDate) || today.isAfter(addDeadline)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll outside of add/drop period.");
                }
                if (enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId) != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already enrolled.");
                }

                User student = userRepository.findById(studentId).orElse(null);
                if (student == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId);
                }

                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setSection(section);
                enrollment.setGrade(null);
                enrollmentRepository.save(enrollment);

                 EnrollmentDTO enrollmentDTO =  new EnrollmentDTO(enrollment.getEnrollmentId(), null, studentId, student.getName(), student.getEmail(),
                section.getCourse().getCourseId(), section.getCourse().getTitle(), section.getSecId(), sectionNo,
                section.getBuilding(), section.getRoom(), section.getTimes(), section.getCourse().getCredits(),
                term.getYear(), term.getSemester());

                 gradebookServiceProxy.addEnrollment(enrollmentDTO);

                 return enrollmentDTO;
            }


    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found: " + enrollmentId);
        }
        Term term = enrollment.getSection().getTerm();
        LocalDate addDate = term.getAddDate().toLocalDate();
        LocalDate dropDeadline = term.getDropDeadline().toLocalDate();
        LocalDate today = LocalDate.now();
        if (today.isAfter(dropDeadline)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot drop course after drop deadline.");
        }
        if (today.isBefore(addDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add course before add date.");
        }
        enrollmentRepository.delete(enrollment);
        gradebookServiceProxy.deleteEnrollment(enrollmentId);
    }
}
