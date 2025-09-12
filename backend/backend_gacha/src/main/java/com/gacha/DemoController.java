package com.gacha;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // Allow React dev server
public class DemoController {

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello from Spring Boot Backend!";
    }
}
