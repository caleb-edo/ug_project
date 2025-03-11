package com.example.healthsystem.repositories;

import com.example.healthsystem.model.Message;
import com.example.healthsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId OR m.sender.id = :userId ORDER BY m.timestamp DESC")
    List<Message> findAllMessagesByUser(@Param("userId") Long userId);

    List<Message> findByReceiverOrderByTimestampDesc(User receiver);

    List<Message> findBySenderOrderByTimestampDesc(User sender);
}