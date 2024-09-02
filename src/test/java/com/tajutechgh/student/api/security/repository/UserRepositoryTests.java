package com.tajutechgh.student.api.security.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import com.tajutechgh.student.api.repository.User;
import com.tajutechgh.student.api.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {

	@Autowired 
	private UserRepository repo;
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@Test
	public void testAddFirstUser() {
		
		User user1 = new User();
		
		user1.setUsername("Aziz");
		user1.setRole("read");
		
		String rawPass = "aziz";
		String encodedPass = passwordEncoder.encode(rawPass);
		
		user1.setPassword(encodedPass);
		
		User savedUser = repo.save(user1);
		
		assertThat(savedUser).isNotNull();
	}
	
	@Test
	public void testAddSecondUser() {
		
		User user2 = new User();
		
		user2.setUsername("Samuel");
		user2.setRole("write");
		
		String rawPass = "samuel";
		String encodedPass = passwordEncoder.encode(rawPass);
		
		user2.setPassword(encodedPass);
		
		User savedUser = repo.save(user2);
		
		assertThat(savedUser).isNotNull();
		
	}	
	
	@Test
	public void testFindUserNotFound() {
		
		Optional<User> findByUsername = repo.findByUsername("xxxxx");
		
		assertThat(findByUsername).isNotPresent();
	}
	
	@Test
	public void testFindUserFound() {
		
		String username = "Aziz";
		
		Optional<User> findByUsername = repo.findByUsername(username);
		
		assertThat(findByUsername).isPresent();
		
		User user = findByUsername.get();
		
		assertThat(user.getUsername()).isEqualTo(username);
	}	
}