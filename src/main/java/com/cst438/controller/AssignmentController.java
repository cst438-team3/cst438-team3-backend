package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.GradeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {

        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> assignmentDTO_list = new ArrayList<>();

        for (Assignment a: assignments){
            assignmentDTO_list.add(new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()));
        }
        return assignmentDTO_list;
    }

    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) throws ParseException {
        Section section = sectionRepository.findById(dto.secNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "section not found"));

        Date termEndDate = section.getTerm().getEndDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dueDate = dateFormat.parse(dto.dueDate());
        if (dueDate.after(termEndDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Due date is past term end date");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(dto.title());
        assignment.setDueDate(dto.dueDate()); // Keep as string for storage
        assignment.setSection(section);
        assignmentRepository.save(assignment);

        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate(),
                dto.courseId(),
                dto.secId(),
                dto.secNo()
        );
    }

    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.id()).orElse(null);
        if (assignment == null){
            throw new ResponseStatusException( HttpStatus.NOT_FOUND, "assignment not found");
        }
        assignment.setTitle(dto.title());
        assignment.setDueDate(dto.dueDate());

        assignmentRepository.save(assignment);
        return new AssignmentDTO(assignment.getAssignmentId(), assignment.getTitle(), assignment.getDueDate(), dto.courseId(), dto.secId(), dto.secNo());

    }

    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if(assignment!=null){
            assignmentRepository.delete(assignment);
        }
    }

}
