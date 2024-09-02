package com.tajutechgh.student.api.security.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import com.tajutechgh.student.api.repository.RefreshToken;
import com.tajutechgh.student.api.repository.RefreshTokenRepository;

import jakarta.persistence.Query;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class RefreshTokenRepositoryTests {

	@Autowired 
	private RefreshTokenRepository repo;
	
	@Autowired 
	TestEntityManager testEntityManager;
	
	@Test
	public void testFindByUsernameNotFound() {
		
		String usernameNotExist = "abcdefgh";
		
		List<RefreshToken> findResult = repo.findByUsername(usernameNotExist);
		
		assertThat(findResult).isEmpty();
	}
	
	@Test
	public void testFindByUsernameFound() {
		
		String usernameExist = "Samuel";
		
		List<RefreshToken> findResult = repo.findByUsername(usernameExist);
		
		assertThat(findResult).isNotEmpty();
	}	
	
	@Test
	public void testDeleteByExpiryTime() {
		
		String jpql = "SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiryTime <= CURRENT_TIME";
		Query query = testEntityManager.getEntityManager().createQuery(jpql);
		
		Long numberOfExpiredRefreshTokens = (Long) query.getSingleResult();
		
		int rowsDeleted = repo.deleteByExpiryTime();
		
		assertEquals(numberOfExpiredRefreshTokens, rowsDeleted);
	}
	
	
}