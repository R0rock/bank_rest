package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);
    List<Card> findByUser(User user);
    Optional<Card> findByIdAndUser(Long id, User user);

    @Query("SELECT c FROM Card c WHERE c.user = :user AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUser(@Param("user") User user);

    Long countByUser(User user);
}