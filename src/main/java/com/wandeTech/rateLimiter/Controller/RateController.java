package com.wandeTech.rateLimiter.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateController {

    @GetMapping("/api/v1/hello")
    public String hello(){
        return "You are in rate limit bracket";
    }
}
