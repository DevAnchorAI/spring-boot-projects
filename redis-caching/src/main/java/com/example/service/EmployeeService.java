package com.example.service;

import com.example.model.Employee;
import com.example.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository repository;

    @Cacheable(value = "employees", key = "#id")
    public Employee getEmployee(Long id) {

        System.out.println("Fetching from DB");

        return repository.findById(id)
                .orElseThrow();
    }

    //Update Cache
    @CachePut(value = "employees", key = "#employee.id")
    public Employee update(Employee employee){

        return repository.save(employee);

    }

    //Delete Cache
    @CacheEvict(value = "employees", key = "#id")
    public void delete(Long id){

        repository.deleteById(id);

    }
}
