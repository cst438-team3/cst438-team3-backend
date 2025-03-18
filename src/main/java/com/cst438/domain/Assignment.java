package com.cst438.domain;

import jakarta.persistence.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="assignment_id")
    private int assignmentId;

    private String title;
    private String dueDate;

    @ManyToOne
    @JoinColumn(name = "section_no")
    private Section section;

    @OneToMany(mappedBy = "assignment")
    private List<Grade> grades;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public Section getSection() {
        return section;
    }

    public List<Grade> getGrades() {
        return grades;
    }
}
