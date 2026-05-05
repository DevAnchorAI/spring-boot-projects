package com.sks.department.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sks.department.entity.Department;
import com.sks.department.service.DepartmentService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/departments")
@Slf4j
public class DepartmentController {
	
	@Autowired
	private DepartmentService departmentService;
	
	@PostMapping("/")
	public Department saveDepartment(@RequestBody Department department) {
	log.info("DepartmentController.saveDepartment()" );
		return departmentService.saveDepartment(department);
	}
	@GetMapping("/{id}")
	public List<Department> findDepartmentById(@PathVariable("id") Long departmentId) {
		log.info("DepartmentController.findDepartmentById()");
		List<Department> departmentList = new ArrayList<>();
		departmentList.add(departmentService.findDepartmentById(departmentId));
		return departmentList;
	}
	

}
