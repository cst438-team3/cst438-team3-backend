package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;;

    @Test
    public void addSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the section is inserted.
        SectionDTO section = new SectionDTO(
                0,
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

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        SectionDTO result = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.secNo());
        // check other fields of the DTO for expected values
        assertEquals("cst499", result.courseId());

        // check the database
        Section s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNull(s);  // section should not be found after delete
    }

    @Test
    public void addSectionFailsBadCourse( ) throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                "", 
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue the POST request
        response = mvc.perform(
                        post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // response should be 404, the course cst599 is not found
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("course not found cst599", message);

    }


    @Test
    public void addAssignment() throws Exception {
        MockHttpServletResponse response;
        AssignmentDTO assignment = new AssignmentDTO(
                4,
                "test assignment",
                "2024-12-16",
                "cst338",
                1,
                1
        );

        response = mvc.perform(
                        post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);
        assertNotEquals(0, result.id());
        assertEquals("test assignment", result.title());
        assertEquals("cst338", result.courseId()); // Check against actual course ID

        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("cst338", a.getSection().getCourse().getCourseId());

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/" + result.id()))
                .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());

        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);
    }

    @Test
    public void addAssignmentFailsBadSection() throws Exception {
        MockHttpServletResponse response;

        AssignmentDTO assignment = new AssignmentDTO(
                4,
                "invalid assignment",
                "2025-05-01",
                "cst338",
                1,
                999
        );

        response = mvc.perform(
                        post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();
        assertEquals(404, response.getStatus());
        assertEquals("section not found", response.getErrorMessage());
    }

    @Test
    public void addAssignmentWithInvalidDueDate() throws Exception {
        Section section = sectionRepository.findById(1).orElseThrow();
        Date termEndDate = section.getTerm().getEndDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(termEndDate);
        calendar.add(Calendar.DATE, 1);
        Date invalidDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String invalidDueDate = dateFormat.format(invalidDate);

        AssignmentDTO assignment = new AssignmentDTO(
                4,
                "late assignment",
                invalidDueDate,
                "cst338",
                1,
                1
        );

        MockHttpServletResponse response = mvc.perform(
                        post("/assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        assertEquals("Due date is past term end date", response.getErrorMessage());

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
