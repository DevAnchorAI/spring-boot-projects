package com.sks.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sks.user.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

	Optional<Users> findById(Long userId);

}
