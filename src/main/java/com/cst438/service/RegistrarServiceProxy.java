package com.cst438.service;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.cst438.dto.AssignmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    TermRepository termRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            switch (action) {
                case "addCourse":
                    CourseDTO addDTO = fromJsonString(parts[1], CourseDTO.class);
                    Course newCourse = new Course();
                    newCourse.setCourseId(addDTO.courseId());
                    newCourse.setTitle(addDTO.title());
                    newCourse.setCredits(addDTO.credits());
                    courseRepository.save(newCourse);
                    break;

                case "updateCourse":
                    CourseDTO updateDTO = fromJsonString(parts[1], CourseDTO.class);
                    Course courseToUpdate = courseRepository.findById(updateDTO.courseId()).orElse(null);
                    if (courseToUpdate != null) {
                        courseToUpdate.setTitle(updateDTO.title());
                        courseToUpdate.setCredits(updateDTO.credits());
                        courseRepository.save(courseToUpdate);
                    }
                    break;

                case "deleteCourse":
                    String courseIdToDelete = parts[1];
                    courseRepository.deleteById(courseIdToDelete);
                    break;
                
                case "addUser":
                    UserDTO userDTO = fromJsonString(parts[1], UserDTO.class);
                    User newUser = new User();
                    newUser.setId(userDTO.id());
                    newUser.setName(userDTO.name());
                    newUser.setEmail(userDTO.email());
                    newUser.setPassword("");
                    newUser.setType(userDTO.type());
                    userRepository.save(newUser);
                    break;
                
                case "updateUser":
                    UserDTO updateUserDTO = fromJsonString(parts[1], UserDTO.class);
                    User userToUpdate = userRepository.findById(updateUserDTO.id()).orElse(null);
                    if (userToUpdate != null) {
                        userToUpdate.setName(updateUserDTO.name());
                        userToUpdate.setEmail(updateUserDTO.email());
                        userToUpdate.setType(updateUserDTO.type());
                        userRepository.save(userToUpdate);
                    }
                    break;
                
                case "deleteUser":
                    int userIdToDelete = Integer.parseInt(parts[1]);
                    userRepository.deleteById(userIdToDelete);
                    break;
                
                case "addSection":
                    SectionDTO sectionDTO = fromJsonString(parts[1], SectionDTO.class);

                    Course course = courseRepository.findById(sectionDTO.courseId()).orElse(null);
                    Term term = termRepository.findByYearAndSemester(sectionDTO.year(), sectionDTO.semester());

                    if (course != null && term != null) {
                        Section section = new Section();
                        section.setSectionNo(sectionDTO.secNo());
                        section.setCourse(course);
                        section.setSecId(sectionDTO.secId());
                        section.setTerm(term);
                        section.setBuilding(sectionDTO.building());
                        section.setRoom(sectionDTO.room());
                        section.setTimes(sectionDTO.times());
                        section.setInstructor_email(sectionDTO.instructorEmail());
                        sectionRepository.save(section);
                    }
                    break;

                case "updateSection":
                    SectionDTO updateSectionDTO = fromJsonString(parts[1], SectionDTO.class);
                    Section existingSection = sectionRepository.findById(updateSectionDTO.secNo()).orElse(null);

                    Course updateCourse = courseRepository.findById(updateSectionDTO.courseId()).orElse(null);
                    Term updateTerm = termRepository.findByYearAndSemester(updateSectionDTO.year(), updateSectionDTO.semester());

                    if (existingSection != null && updateCourse != null && updateTerm != null) {
                        existingSection.setCourse(updateCourse);
                        existingSection.setTerm(updateTerm);
                        existingSection.setSecId(updateSectionDTO.secId());
                        existingSection.setBuilding(updateSectionDTO.building());
                        existingSection.setRoom(updateSectionDTO.room());
                        existingSection.setTimes(updateSectionDTO.times());
                        existingSection.setInstructor_email(updateSectionDTO.instructorEmail());
                        sectionRepository.save(existingSection);
                    }
                    break;

                case "deleteSection":
                    int sectionNoToDelete = Integer.parseInt(parts[1]);
                    sectionRepository.deleteById(sectionNoToDelete);
                    break;
                
                case "addEnrollment":
                    EnrollmentDTO enrollDTO = fromJsonString(parts[1], EnrollmentDTO.class);
                
                    User student = userRepository.findById(enrollDTO.studentId()).orElse(null);
                    Section section = sectionRepository.findById(enrollDTO.sectionNo()).orElse(null);
                    Enrollment existing = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(enrollDTO.sectionNo(), enrollDTO.studentId());
                    if (student != null && section != null && existing == null) {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setEnrollmentId(enrollDTO.enrollmentId());
                        enrollment.setGrade(enrollDTO.grade());
                        enrollment.setStudent(student);
                        enrollment.setSection(section);
                        enrollmentRepository.save(enrollment);
                    }
                    break;
                
                case "updateEnrollment":
                    EnrollmentDTO updateEnrollDTO = fromJsonString(parts[1], EnrollmentDTO.class);
                    Enrollment existingEnrollment = enrollmentRepository.findById(updateEnrollDTO.enrollmentId()).orElse(null);
                
                    if (existingEnrollment != null) {
                        existingEnrollment.setGrade(updateEnrollDTO.grade());
                        enrollmentRepository.save(existingEnrollment);
                    }
                    break;
                
                case "deleteEnrollment":
                    int enrollmentIdToDelete = Integer.parseInt(parts[1]);
                    enrollmentRepository.deleteById(enrollmentIdToDelete);
                    break;

                case "addAssignment":
                    AssignmentDTO addAssignmentDTO = fromJsonString(parts[1], AssignmentDTO.class);
                    Section sectionFind = sectionRepository.findById(addAssignmentDTO.secNo()).orElse(null);
                    if (sectionFind != null) {
                        Assignment newAssignment = new Assignment();
                        newAssignment.setAssignmentId(addAssignmentDTO.id());
                        newAssignment.setTitle(addAssignmentDTO.title());
                        newAssignment.setDueDate(addAssignmentDTO.dueDate());
                        newAssignment.setSection(sectionFind);
                        assignmentRepository.save(newAssignment);
                    }
                    break;
                
                case "updateAssignment":
                    AssignmentDTO updateAssignmentDTO = fromJsonString(parts[1], AssignmentDTO.class);
                    Assignment assignmentToUpdate = assignmentRepository.findById(updateAssignmentDTO.id()).orElse(null);
                    if (assignmentToUpdate != null) {
                        assignmentToUpdate.setTitle(updateAssignmentDTO.title());
                        assignmentToUpdate.setDueDate(updateAssignmentDTO.dueDate());
                        assignmentRepository.save(assignmentToUpdate);
                    }
                    break;
                
                case "deleteAssignment":
                    int assignmentIdToDelete = Integer.parseInt(parts[1]);
                    assignmentRepository.deleteById(assignmentIdToDelete);
                    break;

                default:
                    System.out.println("Unknown action: " + action);
            }

        } catch (Exception e) {
            System.err.println("Error processing message from Registrar: " + e.getMessage());
        }
    } 

    public void sendFinalGradeUpdate(EnrollmentDTO dto) {
        try {
            String msg = "updateEnrollment " + asJsonString(dto);
            System.out.println("Sending final grade update to Registrar: " + msg);
            rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), msg);
        } catch (Exception e) {
            System.err.println("Failed to send final grade update: " + e.getMessage());
        }
    }

    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}