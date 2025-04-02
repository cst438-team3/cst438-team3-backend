package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentScheduleControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    public void testStudentEnrollsSuccessfully() throws Exception {
        int studentId = 5; 
        int sectionNo = 6; 

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.post("/enrollments/sections/" + sectionNo)
                        .param("studentId", String.valueOf(studentId))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);
        assertEquals(studentId, result.studentId());
        assertEquals(sectionNo, result.sectionNo());

        Enrollment enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        assertNotNull(enrollment);

        enrollmentRepository.delete(enrollment);

        enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        assertNull(enrollment);
    }


   @Test
    public void testStudentAlreadyEnrolled() throws Exception {
        int studentId = 3;
        int sectionNo = 8;

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.post("/enrollments/sections/" + sectionNo)
                        .param("studentId", String.valueOf(studentId))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        String errorMessage = response.getErrorMessage();
        assertEquals("Student is already enrolled.", errorMessage);
    }


    @Test
    public void testEnrollWithInvalidSectionNumber() throws Exception {
        int studentId = 5;
        int sectionNo = 999;

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.post("/enrollments/sections/" + sectionNo)
                        .param("studentId", String.valueOf(studentId))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertEquals(404, response.getStatus());

        String errorMessage = response.getErrorMessage();
        assertEquals("Section not found: " + sectionNo, errorMessage);

        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(999, 5);
        assertNull(e);
    }


    @Test
    public void testEnrollPastAddDeadline() throws Exception {
        int studentId = 5; 
        int sectionNo = 1; 

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.post("/enrollments/sections/" + sectionNo)
                        .param("studentId", String.valueOf(studentId))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        String errorMessage = response.getErrorMessage();
        assertEquals("Cannot enroll outside of add/drop period.", errorMessage);

        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(1, 5);
        assertNull(e);
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
