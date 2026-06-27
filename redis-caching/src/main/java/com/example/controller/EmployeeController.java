package com.example.controller;

import com.example.dto.ResponseVO;
import com.example.model.Employee;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emp")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{id}")
    public ResponseVO getEmployeeById(@PathVariable("id")Long id ){
        log.info("EmployeeController:: getEmployeeById:  {}",id);
       Employee emp = employeeService.getEmployee(id);
        log.info(" {} ",emp);
        ResponseVO response = new ResponseVO(emp);
       return response;
    }
}
