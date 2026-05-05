package com.sks.user.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sks.exception.UserAlreadyExistException;
import com.sks.exception.UserNotFoundException;
import com.sks.user.dto.Department;
import com.sks.user.dto.ResponseTemplateVO;
import com.sks.user.entity.Users;
import com.sks.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RestTemplate restTemplate;

	public Users saveUser(Users user) {
		log.info("UserService.saveUser()");
		Users userExist =userRepository.findById(user.getDepartmentId()).orElse(null);
		if(userExist == null) {
			return userRepository.save(user);	
		}else {
			
			throw new UserAlreadyExistException("User Already Exist");
		}
		
	}

	public ResponseTemplateVO getUserWithDepartment(Long userId) {
		log.info("UserService.getUserWithDepartment()");
		ResponseTemplateVO vo = new ResponseTemplateVO();
		//Users user = userRepository.findByUserId(userId);
		Users user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User does not exist"));
		
		Department department =restTemplate.getForObject("http://DEPARTMENT-SERVICE/departments/"+user.getDepartmentId(), Department.class);
		vo.setUser(user);
		vo.setDepartment(department);
		return vo;
	}
	
	public Users updateUser(Users user) {
		log.info("UserService.updateUser()");
		Users userExist =userRepository.findById(user.getDepartmentId()).orElse(null);
		if(userExist == null) {
			throw new UserNotFoundException("User does not exist to update");	
		}else {
			
			userExist.setDepartmentId(user.getDepartmentId());
			userExist.setEmail(user.getEmail());
			userExist.setFirstName(user.getFirstName());
			userExist.setLastName(user.getLastName());
			return userRepository.save(user);
		}
		
	}
}
