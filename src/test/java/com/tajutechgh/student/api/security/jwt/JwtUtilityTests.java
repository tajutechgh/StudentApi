package com.tajutechgh.student.api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.tajutechgh.student.api.repository.User;

public class JwtUtilityTests {
	
	private static JwtUtility jwtUtil;
	
	@BeforeAll
	static void setup() {
		
		jwtUtil = new JwtUtility();
		
		jwtUtil.setIssuerName("My Company");
		jwtUtil.setAccessTokenExpiration(2);
		jwtUtil.setSecretKey("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuv+9-$!*&%");
	}
	
	@Test
	public void testGenerateFail() {
		
		assertThrows(IllegalArgumentException.class, new Executable() {
			
			@Override
			public void execute() throws Throwable {
				
				User user = null;
				
				jwtUtil.generateAccessToken(user);
			}
		});
	}
	 
	@Test
	public void testGenerateSuccess() {
		
		User user = new User();
		
		user.setId(3);
		user.setUsername("johndoe");
		user.setRole("read");
		
		String token = jwtUtil.generateAccessToken(user);
		
		assertThat(token).isNotNull();
		
		System.out.println(token);
	}	
	
	@Test
	public void testValidateFail() {
		
		assertThrows(JwtValidationException.class, () -> {
			
			jwtUtil.validateAccessToken("a.b.c");
		});
	}
	
	@Test
	public void testValidateSuccess() {
		
		User user = new User();
		
		user.setId(3);
		user.setUsername("johndoe");
		user.setRole("read");
		
		String token = jwtUtil.generateAccessToken(user);
		
		assertThat(token).isNotNull();
		
		assertDoesNotThrow(() -> {
			
			jwtUtil.validateAccessToken(token);
			
		});
	}	
}