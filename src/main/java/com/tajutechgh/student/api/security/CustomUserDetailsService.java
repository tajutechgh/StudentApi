package com.tajutechgh.student.api.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tajutechgh.student.api.repository.User;
import com.tajutechgh.student.api.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired 
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Optional<User> findByUsername = userRepo.findByUsername(username);
		
		if (!findByUsername.isPresent()) {
			
			throw new UsernameNotFoundException("No user found with the given user name");
		}
		
		return new CustomUserDetails(findByUsername.get());
	}

}