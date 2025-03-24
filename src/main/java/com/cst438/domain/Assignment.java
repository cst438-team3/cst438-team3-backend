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
    @Column(name="due_date")
    private String dueDate;
    @ManyToOne
    @JoinColumn(name = "section_no", nullable = false)
    private Section section;

    @OneToMany(mappedBy = "assignment")
    private List<Grade> grades;

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDueDateAsString() {
        if (this.dueDate!=null) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            return f.format(this.dueDate);
        } else {
            return null;
        }
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<Grade> getGrades() {
        return grades;
    }
}

