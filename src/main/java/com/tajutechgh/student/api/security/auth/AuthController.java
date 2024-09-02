package com.tajutechgh.student.api.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tajutechgh.student.api.security.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/oauth")
public class AuthController {

	@Autowired 
	private AuthenticationManager authenticationManager;
	
	@Autowired 
	private TokenService tokenService;
	
	@PostMapping("/token")
	public ResponseEntity<?> getAccessToken(@RequestBody @Valid AuthRequest request) {
		
		String username = request.getUsername();
		String password = request.getPassword();
		
		try {
			
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			AuthResponse response = tokenService.generateTokens(userDetails.getUser());
			
			return ResponseEntity.ok(response);
			
		} catch (BadCredentialsException ex) {
			
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
	
	@PostMapping("/token/refresh")
	public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
		
		try {
			
			AuthResponse response = tokenService.refreshTokens(request);
			
			return ResponseEntity.ok(response);
			
		} catch (RefreshTokenNotFoundException | RefreshTokenExpiredException e) {
			
			e.printStackTrace();
			
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
	}
}