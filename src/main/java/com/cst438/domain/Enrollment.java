package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Enrollment {
    @Id
    // @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;
	
	// TODO complete this class
    // add additional attribute for grade
    // create relationship between enrollment and user entities
    // create relationship between enrollment and section entities
    // add getter/setter methods
    
    private String grade;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "section_no")
    private Section section;

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getGrade() {
        return grade;
    }

    public User getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
