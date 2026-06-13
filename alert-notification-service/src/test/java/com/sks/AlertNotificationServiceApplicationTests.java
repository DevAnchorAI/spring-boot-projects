//package com.sks;
//
//import com.sks.dto.AlertRequest;
//import com.sks.entity.AlertSeverity;
//import com.sks.service.AlertService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class AlertNotificationServiceApplicationTests {
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Autowired
//	private AlertService alertService;
//
//	@Test
//	void contextLoads() {
//		assertNotNull(alertService);
//	}
//
//	@Test
//	void testCreateAlert() {
//		AlertRequest request = AlertRequest.builder()
//			.title("Test Alert")
//			.description("This is a test alert")
//			.recipientEmail("test@example.com")
//			.severity(AlertSeverity.HIGH)
//			.build();
//
//		ResponseEntity<String> response = restTemplate.postForEntity(
//			"/api/alerts",
//			request,
//			String.class
//		);
//
//		assertEquals(HttpStatus.CREATED, response.getStatusCode());
//		assertNotNull(response.getBody());
//	}
//
//	@Test
//	void testGetAllAlerts() {
//		ResponseEntity<String> response = restTemplate.getForEntity(
//			"/api/alerts",
//			String.class
//		);
//
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//}
