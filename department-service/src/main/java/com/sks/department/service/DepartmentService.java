package com.sks.department.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sks.department.entity.Department;
import com.sks.department.repository.DepartmentRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	public Department saveDepartment(Department department) {
		log.info("DepartmentService.saveDepartment()");
		return departmentRepository.save(department);
	}
	 public Department findDepartmentById(Long departmentId) {
	        log.info("DepartmentService.findDepartmentById()");
	        return departmentRepository.findByDepartmentId(departmentId);
	    }
}
