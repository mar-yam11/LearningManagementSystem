package com.lms.lms.repository;

import com.lms.lms.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Integer> {
    Optional<Instructor> findById(int id);

    Optional<Instructor> findByEmail(String email);
}