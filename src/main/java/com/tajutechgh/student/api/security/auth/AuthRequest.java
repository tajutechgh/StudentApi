package com.tajutechgh.student.api.security.auth;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

public class AuthRequest {
	
	@NotNull 
	@Length(min = 5, max = 20)
	private String username;
	
	@NotNull 
	@Length(min = 5, max = 10)
	private String password;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}	
	
}