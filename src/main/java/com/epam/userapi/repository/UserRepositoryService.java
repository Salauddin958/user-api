package com.epam.userapi.repository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.userapi.model.User;

@Service
public class UserRepositoryService {
	
	@Autowired
	UserRepositry repository;
	
	private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
	private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

	
	public List<User> getAllUsers() {
		return (List<User>) repository.findAll();
	}
	
	public Optional<User> getUser(int id) {
		return repository.findById(id);
	}
	
	public User saveUser(User user) {
		String pwd = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
		user.setPassword(pwd);
		user.setId((int)System.currentTimeMillis());
		byte[] randomBytes = new byte[24];
		secureRandom.nextBytes(randomBytes);
		String token = base64Encoder.encodeToString(randomBytes);
		user.setToken(token);
		return repository.save(user);
	}
	
	public User updateUser(User user, int id) throws Exception{
		Optional<User> optional = repository.findById(id);
		if(!optional.isPresent()) {
			throw new Exception("No user found");
		}
		User existing = optional.get();
		if(user.getEmail() != null)
			existing.setEmail(user.getEmail());
		if(user.getUsername() != null)
			existing.setUsername(user.getUsername());
		if(user.getPassword() != null) {
			String pwd = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			existing.setPassword(pwd);
		}
		return repository.save(existing);
	}
	
	public User getLoginUser(User user) {
		User ur =  repository.findByUsername(user.getUsername());
		if(ur==null) return null;
		String hash = ur.getPassword();
		if(BCrypt.checkpw(user.getPassword(), hash))
			return ur;
		return null;
	}
	
	public boolean validateToken(String token, String username) {
		System.out.println(repository.findByUsernameAndToken(username, token));
		return repository.findByUsernameAndToken(username, token) != null;
	}

}
