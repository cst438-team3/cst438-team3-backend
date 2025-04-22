package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionControllerWithSecurityInstructorUnitTest {

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void createAndGradeAssignment() throws Exception {
        UserDTO instructor = createUser(100, "instructor", "instructor@csumb.edu", "INSTRUCTOR");
        UserDTO student = createUser(200, "student", "student@csumb.edu", "STUDENT");

        HttpHeaders instructorHeaders = loginAsUser("instructor@csumb.edu", "instructor2024");

        AssignmentDTO assignment = new AssignmentDTO(
                6000,
                "newAssignment",
                "2024-02-01",
                "cst363",
                1,
                8
        );

        ResponseEntity<String> result = template.exchange(
                "/assignments",
                HttpMethod.POST,
                new HttpEntity<>(assignment, instructorHeaders),
                String.class
        );
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AssignmentDTO resultAssignment = fromJsonString(result.getBody(), AssignmentDTO.class);

        Section section = findFirstSection("cst363", 2024, "Spring");
        Enrollment enrollment = enrollStudent(userRepository.findByEmail("student@csumb.edu"), section);

        Grade grade = createInitialGrade(resultAssignment.id(), enrollment);

        GradeDTO[] grades = getGradesForAssignment(resultAssignment.id(), instructorHeaders);
        assertTrue(grades.length > 0);

        GradeDTO existingGrade = grades[0];
        updateGrade(existingGrade, 95, instructorHeaders);

        Grade updatedGradeEntity = gradeRepository.findById(existingGrade.gradeId()).get();
        assertEquals(Integer.valueOf(95), updatedGradeEntity.getScore());

        cleanupTestData(resultAssignment.id(), enrollment.getEnrollmentId(), student.id(), instructor.id());
    }

    private UserDTO createUser(int id, String name, String email, String role) throws Exception {
        UserDTO user = new UserDTO(id, name, email, role);
        HttpHeaders adminHeaders = loginAsUser("admin@csumb.edu", "admin");

        ResponseEntity<String> response = template.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user, adminHeaders),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return user;
    }

    private HttpHeaders loginAsUser(String username, String password) throws Exception {
        ResponseEntity<String> response = template.withBasicAuth(username, password)
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        LoginDTO loginDTO = fromJsonString(response.getBody(), LoginDTO.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginDTO.jwt());
        return headers;
    }

    private Section findFirstSection(String courseId, int year, String semester) {
        List<Section> sections = sectionRepository.findByLikeCourseIdAndYearAndSemester(courseId, year, semester);
        assertFalse(sections.isEmpty());
        return sections.get(0);
    }

    private Enrollment enrollStudent(User student, Section section) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        return enrollmentRepository.save(enrollment);
    }

    private Grade createInitialGrade(int assignmentId, Enrollment enrollment) {
        Grade grade = new Grade();
        grade.setScore(null);
        grade.setAssignment(assignmentRepository.findById(assignmentId).get());
        grade.setEnrollment(enrollment);
        return gradeRepository.save(grade);
    }

    private GradeDTO[] getGradesForAssignment(int assignmentId, HttpHeaders headers) throws Exception {
        ResponseEntity<String> response = template.exchange(
                "/assignments/" + assignmentId + "/grades",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return fromJsonString(response.getBody(), GradeDTO[].class);
    }

    private void updateGrade(GradeDTO grade, int score, HttpHeaders headers) {
        GradeDTO updatedGrade = new GradeDTO(
                grade.gradeId(),
                grade.studentName(),
                grade.studentEmail(),
                grade.assignmentTitle(),
                grade.courseId(),
                grade.sectionId(),
                score
        );

        ResponseEntity<Void> response = template.exchange(
                "/grades",
                HttpMethod.PUT,
                new HttpEntity<>(List.of(updatedGrade), headers),
                Void.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void cleanupTestData(int assignmentId, int enrollmentId, int studentId, int instructorId) {
        List<Grade> allGrades = StreamSupport
                .stream(gradeRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        for (Grade g : allGrades) {
            if (g.getAssignment().getAssignmentId() == assignmentId) {
                gradeRepository.delete(g);
            }
        }

        assignmentRepository.deleteById(assignmentId);
        enrollmentRepository.deleteById(enrollmentId);
        userRepository.deleteById(studentId);
        userRepository.deleteById(instructorId);
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
/*
// create grade for assignment
        GradeDTO grade = new GradeDTO(
                100,
                "user",
                "user@csumb.edu",
                "Software Engineering",
                "cst438",
                10,
                59
        );
        HttpEntity<GradeDTO> gradeEntity = new HttpEntity<>(grade, headers);
        result = template.exchange("/grades", HttpMethod.PUT, gradeEntity, String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        GradeDTO resultGrade = fromJsonString(result.getBody(), GradeDTO.class);
        assertNotEquals(0, resultGrade.gradeId());
        assertEquals(100, resultGrade.gradeId());
 */