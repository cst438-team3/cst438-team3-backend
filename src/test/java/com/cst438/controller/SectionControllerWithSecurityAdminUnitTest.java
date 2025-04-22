package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.LoginDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionControllerWithSecurityAdminUnitTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void createNewUser() throws Exception {
        UserDTO user = new UserDTO(
                100,
                "newUser",
                "newUser@csumb.edu",
                "STUDENT"
        );

        ResponseEntity<String> result = template.withBasicAuth("admin@csumb.edu", "admin")
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        LoginDTO dto = fromJsonString(result.getBody(), LoginDTO.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dto.jwt());
        HttpEntity<UserDTO> entity = new HttpEntity<>(user, headers);

        result = template.exchange("/users", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        UserDTO resultUser = fromJsonString(result.getBody(), UserDTO.class);
        assertNotEquals(0, resultUser.id());
        assertEquals("newUser", resultUser.name());

        User u = userRepository.findById(resultUser.id()).orElse(null);
        assertNotNull(u);
        assertEquals("newUser", u.getName());
        userRepository.deleteById(resultUser.id());
    }

    @Test
    public void createNewCourse() throws Exception {
        CourseDTO course = new CourseDTO(
                "cst420",
                "newCourse",
                3
        );

        ResponseEntity<String> result = template.withBasicAuth("admin@csumb.edu", "admin")
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        LoginDTO dto = fromJsonString(result.getBody(), LoginDTO.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dto.jwt());
        HttpEntity<CourseDTO> entity = new HttpEntity<>(course, headers);

        result = template.exchange("/courses", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        CourseDTO resultCourse = fromJsonString(result.getBody(), CourseDTO.class);
        assertNotEquals(0, resultCourse.courseId());
        assertEquals("cst420", resultCourse.courseId());

        Course c = courseRepository.findById(resultCourse.courseId()).orElse(null);
        assertNotNull(c);
        assertEquals("cst420", c.getCourseId());
        courseRepository.deleteById(resultCourse.courseId());
    }

    @Test
    public void createNewSection() throws Exception {
        SectionDTO section = new SectionDTO(
                100,
                2024,
                "Spring",
                "cst499",
                "",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        ResponseEntity<String> result = template.withBasicAuth("admin@csumb.edu", "admin")
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        LoginDTO dto = fromJsonString(result.getBody(), LoginDTO.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dto.jwt());
        HttpEntity<SectionDTO> entity = new HttpEntity<>(section, headers);

        result = template.exchange("/sections", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        SectionDTO resultSection = fromJsonString(result.getBody(), SectionDTO.class);
        assertNotEquals(0, resultSection.secNo());
        assertEquals("cst499", resultSection.courseId());

        Section s = sectionRepository.findById(resultSection.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());
        sectionRepository.deleteById(resultSection.secNo());
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
