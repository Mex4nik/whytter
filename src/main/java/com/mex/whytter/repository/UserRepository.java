package com.mex.whytter.repository;

import com.mex.whytter.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @EntityGraph(attributePaths = { "subscriptions", "subscribers" })
    Optional<User> findById(String s);
}
