package com.tajutechgh.student.api.security.auth;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tajutechgh.student.api.repository.RefreshToken;
import com.tajutechgh.student.api.repository.RefreshTokenRepository;
import com.tajutechgh.student.api.repository.User;
import com.tajutechgh.student.api.security.jwt.JwtUtility;

@Service
public class TokenService {
	
	@Value("${app.security.jwt.refresh-token.expiration}")
	private int refreshTokenExpiration;
	
	@Autowired 
	private RefreshTokenRepository refreshTokenRepo;
	
	@Autowired 
	private JwtUtility jwtUtil;
	
	@Autowired PasswordEncoder passwordEncoder;
	
	public AuthResponse generateTokens(User user) {
		
		String accessToken = jwtUtil.generateAccessToken(user);
		
		AuthResponse response = new AuthResponse();
		response.setAccessToken(accessToken);
		
		String randomUUID = UUID.randomUUID().toString();
		
		response.setRefreshToken(randomUUID);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(passwordEncoder.encode(randomUUID));
		
		long refreshTokenExpirationInMillis = System.currentTimeMillis() + refreshTokenExpiration * 60000;
		refreshToken.setExpiryTime(new Date(refreshTokenExpirationInMillis));
		
		refreshTokenRepo.save(refreshToken);
		
		return response;
	}
	
	public AuthResponse refreshTokens(RefreshTokenRequest request) throws RefreshTokenNotFoundException, RefreshTokenExpiredException {
		
		String rawRefreshToken = request.getRefreshToken();
		
		List<RefreshToken> listRefreshTokens = refreshTokenRepo.findByUsername(request.getUsername());
		
		RefreshToken foundRefreshToken = null;
		
		for (RefreshToken token : listRefreshTokens) {
			if (passwordEncoder.matches(rawRefreshToken, token.getToken())) {
				foundRefreshToken = token;
			}
		}
		
		if (foundRefreshToken == null)
			throw new RefreshTokenNotFoundException();
		
		Date currentTime = new Date();
		
		if (foundRefreshToken.getExpiryTime().before(currentTime))
			throw new RefreshTokenExpiredException();
		
		AuthResponse response = generateTokens(foundRefreshToken.getUser());
		
		refreshTokenRepo.delete(foundRefreshToken);
		
		return response;
	}
}