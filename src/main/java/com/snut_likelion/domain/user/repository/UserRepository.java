package com.snut_likelion.domain.user.repository;

import com.snut_likelion.domain.user.dto.res.MemberSearchResponse;
import com.snut_likelion.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH LionInfo li on u.id = li.user.id " +
            "WHERE u.email = :email")
    Optional<User> findWithLionInfoByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailOrUsernameOrPhoneNumber(String email, String username, String phoneNumber);

    @Query("select u from User u join fetch u.lionInfos " +
            "where u.id = :userId")
    Optional<User> findWithLionUserById(Long userId);

    @Query("select u from User u " +
            "left join fetch u.portfolioLinks " +
            "where u.id = :userId")
    Optional<User> findUserDetailsByUserId(@Param("userId") Long userId);

    @Query("select new com.snut_likelion.domain.user.dto.res.MemberSearchResponse(" +
            "u.id, u.username, li.part, li.generation, u.profileImageUrl) " +
            "from User u join LionInfo li on u.id = li.user.id " +
            "where lower(u.username) like lower(concat('%', :keyword, '%'))"
    )
    List<MemberSearchResponse> searchUserByKeyword(String keyword);

    List<User> findAllBySayingIsNotNull();
}
