package com.sks.controller;

import com.sks.dto.ResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/hello/{message}")
    public ResponseDto greetingMessag(@PathVariable String message){
        ResponseDto responseDto = new ResponseDto(200,"Hello: "+message);
    return responseDto;
    }
}
