package com.sks.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sks.exception.ErrorResponse;
import com.sks.exception.UserAlreadyExistException;
import com.sks.exception.UserNotFoundException;
import com.sks.user.dto.ResponseTemplateVO;
import com.sks.user.entity.Users;
import com.sks.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/saveUser")
	public Users saveUser(@RequestBody Users user) {
		log.info("UserController.saveUser()");
		return userService.saveUser(user);
	}
	
	@GetMapping("/{id}")
	public ResponseTemplateVO getUserWithDepartment(@PathVariable("id")Long userId) {
		log.info("UserController.getUserWithDepartment()");
		return userService.getUserWithDepartment(userId);
	}
	
	@PutMapping("/updateUser")
	public Users updateUser(@RequestBody Users user) {
		log.info("UserController.updateUser()");
		return userService.updateUser(user);
	}
	
	@ExceptionHandler(UserAlreadyExistException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleUserAlreadyExistException(UserAlreadyExistException exception) {
		return new ErrorResponse(HttpStatus.CONFLICT.value(),exception.getMessage());
	}
	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleUserNotFoundException(UserNotFoundException exception) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(),exception.getMessage());
	}
}
