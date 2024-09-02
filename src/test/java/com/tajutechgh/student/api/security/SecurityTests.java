package com.tajutechgh.student.api.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tajutechgh.student.api.repository.Student;
import com.tajutechgh.student.api.security.auth.AuthRequest;
import com.tajutechgh.student.api.security.auth.AuthResponse;
import com.tajutechgh.student.api.security.auth.RefreshTokenRequest;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {

	private static final String GET_ACCESS_TOKEN_ENDPOINT = "/api/oauth/token";
	private static final String LIST_STUDENT_ENDPOINT = "/api/students?pageSize=10&pageNum=1";
	private static final String REFRESH_TOKEN_ENDPOINT = "/api/oauth/token/refresh";
	
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	
	@Test
	public void getBaseUriShouldReturn401() throws Exception {
		mockMvc.perform(get("/"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void testGetAccessTokenBadRequest() throws Exception {
		
		AuthRequest request = new AuthRequest();
		
		request.setUsername("g");
		request.setPassword("aaa");
		
		String requestBody = objectMapper.writeValueAsString(request);
		
		mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
				.contentType("application/json").content(requestBody))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testGetAccessTokenFail() throws Exception {
		
		AuthRequest request = new AuthRequest();
		
		request.setUsername("namhm");
		request.setPassword("aaaaaa");
		
		String requestBody = objectMapper.writeValueAsString(request);
		
		mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
				.contentType("application/json").content(requestBody))
			.andDo(print())
			.andExpect(status().isUnauthorized());		
	}	
	
	@Test
	public void testGetAccessTokenSuccess() throws Exception {
		
		AuthRequest request = new AuthRequest();
		
		request.setUsername("Samuel");
		request.setPassword("samuel");
		
		String requestBody = objectMapper.writeValueAsString(request);
		
		mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
				.contentType("application/json").content(requestBody))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty());
	}	
	
	@Test
	public void testListStudentFail() throws Exception {
		mockMvc.perform(get(LIST_STUDENT_ENDPOINT)
				.header("Authorization", "Bearer somethinginvalid"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.errors[0]").isNotEmpty());
	}
	
	@Test
	public void testListStudentSuccess() throws Exception {
		
		AuthRequest request = new AuthRequest();
		
		request.setUsername("Samuel");
		request.setPassword("samuel");
		
		String requestBody = objectMapper.writeValueAsString(request);
		
		MvcResult mvcResult = mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
							.contentType("application/json").content(requestBody))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.accessToken").isNotEmpty())
						.andExpect(jsonPath("$.refreshToken").isNotEmpty())
						.andReturn();
		
		String responseBody = mvcResult.getResponse().getContentAsString();
		
		AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);
		
		String bearerToken = "Bearer " + response.getAccessToken();
		
		mockMvc.perform(get(LIST_STUDENT_ENDPOINT)
				.header("Authorization", bearerToken))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].name").isString());		
	}
	
	@Test
	public void testAddStudent1() throws Exception {
		
		String apiEndpoint = "/api/students";
		
		Student student = new Student();
		
		student.setName("Mohammed Tajudeen");
		
		String requestBody = objectMapper.writeValueAsString(student);
		
		mockMvc.perform(post(apiEndpoint)
				.contentType("application/json")
				.content(requestBody)
				.with(jwt().authorities(new SimpleGrantedAuthority("write"))))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").isString());			
	}
	
	@Test
	public void testAddStudent2() throws Exception {
		
		String apiEndpoint = "/api/students";
		
		Student student = new Student();
		student.setName("Nam Ha Minh");
		
		String requestBody = objectMapper.writeValueAsString(student);
		
		// to make this test works, specify authority name in configuration class is 'SCOPE_write
		mockMvc.perform(post(apiEndpoint).contentType("application/json").content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "write"))))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").isString());			
	}	
	
	@Test
	public void testUpdateStudent1() throws Exception {
		
		String apiEndpoint = "/api/students";
		
		Student student = new Student();
		student.setId(6);
		student.setName("John Max");
		
		// to make this test works, specify authority name in configuration class is 'SCOPE_write
		var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("xxxx")
			.header("alg", "none")
			.issuer("My Company")
			.claim("scope", "write")
			.subject("1,namhm")
			.build();
		
		String requestBody = objectMapper.writeValueAsString(student);
		
		mockMvc.perform(put(apiEndpoint).contentType("application/json").content(requestBody)
				.with(jwt().jwt(jwt)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").isString());			
	}	
	
	@Test
	public void testUpdateStudent2() throws Exception {
		
		String apiEndpoint = "/api/students";
		
		Student student = new Student();
		student.setId(6);
		student.setName("Mary Smith");
		
		var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("xxxx")
			.header("alg", "none")
			.issuer("My Company")
			.subject("1,namhm")
			.build();
		
		String requestBody = objectMapper.writeValueAsString(student);
		
		// to make this test works, specify authority name in configuration class is 'SCOPE_write
		var authorities = AuthorityUtils.createAuthorityList("SCOPE_write");
		
		var token = new JwtAuthenticationToken(jwt, authorities);
		
		mockMvc.perform(put(apiEndpoint).contentType("application/json").content(requestBody)
				.with(authentication(token)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").isString());			
	}		
	
	@Test
	public void testDeleteStudent() throws Exception {
		
		Integer studentId = 4;
		String apiEndpoint = "/api/students/" + studentId;
		
		mockMvc.perform(delete(apiEndpoint)
				.with(jwt().authorities(new SimpleGrantedAuthority("write"))))
			.andDo(print())
			.andExpect(status().isNoContent());
	}
	
	@Test
	public void testRefreshTokenBadRequest() throws Exception {
		
		RefreshTokenRequest requestObject = new RefreshTokenRequest();
		requestObject.setUsername("abc");
		requestObject.setRefreshToken("hohhg");
		
		String requestBody = objectMapper.writeValueAsString(requestObject);
		
		mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
					.contentType("application/json").content(requestBody))
			.andDo(print())			
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testRefreshTokenFail() throws Exception {
		
		RefreshTokenRequest requestObject = new RefreshTokenRequest();
		
		requestObject.setUsername("namhm");
		requestObject.setRefreshToken("hohhgaagsagsgasgsgasgsgggg81818833aaas");
		
		String requestBody = objectMapper.writeValueAsString(requestObject);
		
		mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
					.contentType("application/json").content(requestBody))
			.andDo(print())			
			.andExpect(status().isUnauthorized());
	}	
	
	@Test
	public void testRefreshTokenSuccess() throws Exception {
		
		RefreshTokenRequest requestObject = new RefreshTokenRequest();
		
		requestObject.setUsername("namhm");
		requestObject.setRefreshToken("efe1d9c8-b222-4eec-9065-b69bc122cd2d");
		
		String requestBody = objectMapper.writeValueAsString(requestObject);
		
		mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
					.contentType("application/json").content(requestBody))
			.andDo(print())			
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty());		
	}	
}