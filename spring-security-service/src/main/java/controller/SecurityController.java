package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class SecurityController {
	
	@PreAuthorize("hasRole('NORMAL')")
	@GetMapping("/normal")
	public ResponseEntity<String> normalUser(){
		return ResponseEntity.ok("Yes, I am Normal User");
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin")
	public ResponseEntity<String> adminUser(){
		return ResponseEntity.ok("Yes, I am Admin User");
	}
	

	@GetMapping("/public")
	public ResponseEntity<String> publicUser(){
		return ResponseEntity.ok("Yes, I am Public User");
	}
	

}
