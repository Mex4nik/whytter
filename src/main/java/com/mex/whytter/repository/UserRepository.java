package com.mex.whytter.repository;

import com.mex.whytter.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
