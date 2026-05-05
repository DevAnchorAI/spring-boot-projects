package com.sks.cloud.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackMethodController {
	
	@GetMapping("/userServiceFallBack")
	public String userServiceFallBackMethod() {
		return "UserService is taking time longer than usal...kindy try after some time.";
	}
	
	@GetMapping("/departmentServiceFallBack")
	public String departmentServiceFallBackMethod() {
		return "DepartmentService is taking time longer than usal...kindy try after some time.";
	}

}
