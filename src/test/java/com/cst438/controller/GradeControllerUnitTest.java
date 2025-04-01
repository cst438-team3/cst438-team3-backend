package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GradeControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @Test
    public void gradeAssignmentTest() throws Exception {
        MockHttpServletResponse response;
        int assignmentId = 1;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + assignmentId + "/grades")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        //Response OK for getting grades
        assertEquals(200, response.getStatus());

        //Response gets converted to array of gradedtos
        GradeDTO[] gradeDTOs = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        assertTrue(gradeDTOs.length > 0, "Should have at least one grade for assignment");

        List<GradeDTO> updatedGrades = new ArrayList<>();

        // Update each grade with a score of 100
        for (GradeDTO grade : gradeDTOs) {
            GradeDTO updatedGrade = new GradeDTO(
                    grade.gradeId(),
                    grade.studentName(),
                    grade.studentEmail(),
                    grade.assignmentTitle(),
                    grade.courseId(),
                    grade.sectionId(),
                    100
            );
            updatedGrades.add(updatedGrade);
        }

        // PUT request to update the grades
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(updatedGrades))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        //200 response for putting grades
        assertEquals(200, response.getStatus());
        //Checks first grade to see if it was updated to 100
        assertEquals(100, gradeRepository.findById(gradeDTOs[0].gradeId()).orElseThrow(() -> new RuntimeException("Grade not found")).getScore());
    }

    @Test
    public void gradeInvalidAssignmentTest() throws Exception {
        MockHttpServletResponse response;
        //invalid id
        int invalidAssignmentId = 9999;

        //Checks if Assingmyt doesnt exist
        Assignment assignment = assignmentRepository.findById(invalidAssignmentId).orElse(null);
        assertNull(assignment);

        //Get request for invalid assignment id should give 404
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + invalidAssignmentId + "/grades")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        assertEquals(404, response.getStatus());
    }

    @Test
    public void updateEnrollmentGradeTest() throws Exception {
        MockHttpServletResponse response;
        int sectionNo = 8;
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/sections/" + sectionNo + "/enrollments")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        //Check that you get an okay response
        assertEquals(200, response.getStatus(), "Should get 200 OK when retrieving enrollments");

        //Response turns into an array of DTOs
        EnrollmentDTO[] enrollmentDTOs = fromJsonString(response.getContentAsString(), EnrollmentDTO[].class);

        //Array should have at least one enrollment
        assertTrue(enrollmentDTOs.length > 0, "Should have at least one enrollment for the section");

        int enrollmentId = enrollmentDTOs[0].enrollmentId();

        List<EnrollmentDTO> updatedEnrollments = new ArrayList<>();

        //Give each enrollment grade of A
        for (EnrollmentDTO enrollment : enrollmentDTOs) {
            EnrollmentDTO updatedEnrollment = new EnrollmentDTO(
                    enrollment.enrollmentId(),
                    "A",
                    enrollment.studentId(),
                    enrollment.name(),
                    enrollment.email(),
                    enrollment.courseId(),
                    enrollment.title(),
                    enrollment.sectionId(),
                    enrollment.sectionNo(),
                    enrollment.building(),
                    enrollment.room(),
                    enrollment.times(),
                    enrollment.credits(),
                    enrollment.year(),
                    enrollment.semester()
            );
            updatedEnrollments.add(updatedEnrollment);
        }

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(updatedEnrollments))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus(), "Should get 200 OK when updating enrollments");

        // Verify that the first enrollment was updated in the database
        Enrollment updatedEnrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        assertNotNull(updatedEnrollment, "Should find the enrollment in the database");
        assertEquals("A", updatedEnrollment.getGrade(), "Grade should be updated to A");
    }

    // Helper method to convert Java object to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to convert JSON string to Java object
    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}