package com.tajutechgh.student.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

	@Query("SELECT rt FROM RefreshToken rt WHERE rt.user.username = ?1")
	public List<RefreshToken> findByUsername(String username);
	
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiryTime <= CURRENT_TIME")
	@Modifying
	public int deleteByExpiryTime();
}