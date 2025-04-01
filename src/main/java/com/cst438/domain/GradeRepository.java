package com.cst438.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends CrudRepository<Grade, Integer> {

    // TODO uncomment the following lines as needed

    @Query("select g from Grade g where g.assignment.assignmentId=:assignmentId and g.enrollment.enrollmentId=:enrollmentId")
    Grade findByEnrollmentIdAndAssignmentId(int enrollmentId, int assignmentId);

    @Query("SELECT g FROM Grade g WHERE g.assignment.assignmentId = :assignmentId AND g.enrollment.student.id = :studentId")
    Optional<Grade> findGradeByAssignmentAndStudent(int assignmentId, int studentId);

    @Query("select g from Grade g where g.assignment.assignmentId=:assignmentId")
    List<Grade> findByAssignmentId(int assignmentId);
}
