package com.tajutechgh.student.api.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.tajutechgh.student.api.repository.User;
import com.tajutechgh.student.api.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter.class);
	
	@Autowired 
	private JwtUtility jwtUtil;
	
	@Autowired 
	@Qualifier("handlerExceptionResolver")	
	HandlerExceptionResolver exceptionResolver;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// this line is for debug purpose, should be removed later
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (!hasAuthorizationBearer(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = getBearerToken(request);
		
		LOGGER.info("Token: " + token);
		
		try {
			Claims claims = jwtUtil.validateAccessToken(token);
			UserDetails userDetails = getUserDetails(claims);
			
			setAuthenticationContext(userDetails, request);
			
			filterChain.doFilter(request, response);
			
			clearAuthenticationContext();
			
		} catch (JwtValidationException e) {
			LOGGER.error(e.getMessage(), e);
			
			exceptionResolver.resolveException(request, response, null, e);
		}
	}

	private void clearAuthenticationContext() {
		SecurityContextHolder.clearContext();
	}

	private void setAuthenticationContext(UserDetails userDetails, HttpServletRequest request) {
		var authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
		
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private UserDetails getUserDetails(Claims claims) {
		String subject = (String) claims.get(Claims.SUBJECT);
		String[] array = subject.split(",");
		
		Integer userId = Integer.valueOf(array[0]);
		String username = array[1];
		
		User user = new User();
		user.setId(userId);
		user.setUsername(username);
		
		String role = (String) claims.get("role");
		user.setRole(role);
		
		LOGGER.info("User parsed from JWT: " + user.getId() + ", "
				+ user.getUsername() + ", " + user.getRole());
		
		return new CustomUserDetails(user);
	}

	private boolean hasAuthorizationBearer(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		
		LOGGER.info("Authorization Header: " + header);
		
		if (ObjectUtils.isEmpty(header) || !header.startsWith("Bearer")) {
			return false;
		}
		return true;
	}
	
	private String getBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		
		String[] array = header.split(" ");
		if (array.length == 2) return array[1];
		
		return null;
	}
	
	

}