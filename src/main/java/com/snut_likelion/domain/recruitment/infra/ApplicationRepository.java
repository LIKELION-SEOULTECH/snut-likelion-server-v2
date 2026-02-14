package com.snut_likelion.domain.recruitment.infra;

import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query(
            "SELECT a FROM Application a " +
                    "JOIN FETCH a.user u " +
                    "LEFT JOIN FETCH a.answers ans " +
                    "LEFT JOIN FETCH ans.question q " +
                    "WHERE a.user.id = :userId " +
                    "AND a.recruitment.generation = :currentGeneration"
    )
    List<Application> findMyApplication(
            @Param("userId") Long userId,
            @Param("currentGeneration") int currentGeneration
    );

    @Query(
            "SELECT a FROM Application a " +
                    "JOIN FETCH a.user u " +
                    "LEFT JOIN FETCH a.answers ans " +
                    "LEFT JOIN FETCH ans.question q " +
                    "WHERE a.id = :id " +
                    "AND a.recruitment.generation = :currentGeneration"
    )
    Optional<Application> findWithDetailsById(
            @Param("id") Long id,
            @Param("currentGeneration") int currentGeneration
    );

    @Query("SELECT a FROM Application a JOIN FETCH a.user u WHERE a.id = :id")
    Optional<Application> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT a FROM Application a JOIN FETCH a.user u " +
            "WHERE a.recruitment.generation = :currentGeneration " +
            "AND a.status = :status")
    List<Application> findAllByStatus(
            @Param("status") ApplicationStatus status,
            @Param("currentGeneration") int currentGeneration
    );
}
