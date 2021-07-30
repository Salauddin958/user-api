package com.epam.userapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.epam.userapi.model.User;

@Repository
public interface UserRepositry extends CrudRepository<User, Integer>{
	User findByUsername(String username);
	User findByUsernameAndToken(String username, String token);
}
