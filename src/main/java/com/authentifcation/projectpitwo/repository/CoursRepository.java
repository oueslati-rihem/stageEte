package com.authentifcation.projectpitwo.repository;

import com.authentifcation.projectpitwo.entities.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByUser_id(Integer userId);
    Cours findByIdC(Long id);
    @Query("SELECT c.title AS coursName, COUNT(q) AS quizCount FROM Cours c LEFT JOIN c.Quizs q GROUP BY c")
    List<Object[]> findCoursWithQuizCount();
    @Query("SELECT c, AVG(c.averageRating) AS averageRating, COUNT(c) AS totalRatings FROM Cours c LEFT JOIN c.averageRating r GROUP BY c")
    List<Object[]> findAllWithAverageRatingAndTotalRatings();
}
