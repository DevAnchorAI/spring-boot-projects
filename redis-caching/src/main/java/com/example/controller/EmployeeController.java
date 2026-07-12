package com.example.controller;

import com.example.dto.ResponseVO;
import com.example.model.Employee;
import com.example.service.EmployeeService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/emp")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private Bucket bucket;


    @GetMapping("/{id}")
    public Object getEmployeeById(@PathVariable("id")Long id ){
        log.info("EmployeeController:: getEmployeeById:  {}",id);
        if(bucket.tryConsume(1)){
            ResponseVO response = new ResponseVO(employeeService.getEmployee(id));
            return response;
        }

       return ResponseEntity.status(429).body("Too Many Requests");
    }
    @PostMapping("/update")
    public ResponseVO saveOrUpdate(@Valid @RequestBody Employee employee){
        log.info("EmployeeController:: saveOrUpdate:  {}",employee);
        Employee emp = employeeService.update(employee);

        ResponseVO response = new ResponseVO(emp);
        return response;
    }

    @DeleteMapping("/{id}")
    public void  deleteEmployeeById(@PathVariable("id")Long id ){
        log.info("EmployeeController:: deleteEmployeeById:  {}",id);
        employeeService.delete(id);
    }

    @Bean
    public static Bucket bucket() {

        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
