package com.epam.userapi.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.userapi.model.User;
import com.epam.userapi.repository.UserRepositoryService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserRepositoryService service;
	
	@GetMapping("/all")
	public ResponseEntity<List<User>> getAllUsers(@RequestHeader HttpHeaders headers) {
		headers.entrySet().stream().forEach(key -> System.out.println(key));
		try {
			List<User> users =  service.getAllUsers();
			if(users == null || users.isEmpty()) {
				return new ResponseEntity<List<User>>(users, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<User>>(users, HttpStatus.OK);
		}
		catch(Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<User> getUser(@PathVariable("id") int id) {
		Optional<User> optional = service.getUser(id);
		try {
			return new ResponseEntity<>(optional.get(), HttpStatus.OK);
		}
		catch(Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/register")
    public ResponseEntity<User> addUser(@Validated @RequestBody User user) {
		try {
			User newUser = service.saveUser(user);
			HttpHeaders header = new HttpHeaders();
			header.add("token", newUser.getToken());
			return new ResponseEntity<User>(newUser, header, HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@Validated @RequestBody User user,
    		@PathVariable("id") int id) {
		try {
			User newUser = service.updateUser(user,id);
			return new ResponseEntity<User>(newUser, HttpStatus.OK);
		}catch (Exception e) {
			System.err.println("Error ::"+ e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> loginUser(@Validated @RequestBody User user,
			HttpServletResponse response) {
		try {
			User newUser = service.getLoginUser(user);
			if(newUser == null) {
				System.out.println("No user found");
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			Cookie cookie = new Cookie("token", newUser.getToken());
			cookie.setMaxAge(5*60);
			response.addCookie(cookie);
			return new ResponseEntity<User>(newUser, HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PostMapping("/validate")
	public boolean validateUser(@RequestBody User user) throws Exception {
		if(user.getUsername() == null || user.getToken() == null)
			throw new Exception("provide username and token");
		return service.validateToken(user.getToken(), user.getUsername());
	}
	
	@GetMapping("/logout")
	public String logOutUser(HttpServletRequest request) {
		Arrays.asList(request.getCookies()).forEach( cookie -> {
			cookie.setMaxAge(0);
		});
		return "logout";
	}
}
