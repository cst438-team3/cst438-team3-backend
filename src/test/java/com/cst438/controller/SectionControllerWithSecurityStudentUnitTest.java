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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionControllerWithSecurityStudentUnitTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @Autowired
    private TestRestTemplate template;

    @Test
    public void studentTest() throws Exception {
        ResponseEntity<String> result = template.withBasicAuth("user@csumb.edu", "user")
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        LoginDTO loginDTO = fromJsonString(result.getBody(), LoginDTO.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginDTO.jwt());

        int sectionNo = 11;
        int studentId = 5;

        HttpEntity<Void> enrollEntity = new HttpEntity<>(headers);
        result = template.exchange("/enrollments/sections/" + sectionNo + "?studentId=" + studentId,
                HttpMethod.POST, enrollEntity, String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        EnrollmentDTO enrollment = fromJsonString(result.getBody(), EnrollmentDTO.class);
        assertEquals(studentId, enrollment.studentId());
        assertEquals(sectionNo, enrollment.sectionNo());

        result = template.exchange("/enrollments?year=" + enrollment.year() + "&semester=" + enrollment.semester() + "&studentId=" + studentId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        EnrollmentDTO[] enrollments = fromJsonString(result.getBody(), EnrollmentDTO[].class);
        assertTrue(enrollments.length > 0);
        boolean found = Arrays.stream(enrollments).anyMatch(e -> e.sectionNo() == sectionNo);
        assertTrue(found);

        result = template.exchange("/transcripts?studentId=" + studentId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        EnrollmentDTO[] transcript = fromJsonString(result.getBody(), EnrollmentDTO[].class);
        assertTrue(transcript.length > 0);
        found = Arrays.stream(transcript).anyMatch(e -> e.sectionNo() == sectionNo);
        assertTrue(found);

        result = template.exchange("/enrollments/" + enrollment.enrollmentId(),
                HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
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
