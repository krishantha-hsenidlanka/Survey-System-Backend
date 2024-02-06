package com.example.surveysystembackend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController@RequestMapping("/")
public class MainController {
    @GetMapping("/home")
    public String home() {
        return "This is home page";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String dashboard() {
        return "This is dashboard page";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manage() {
        return "This is manage page";
    }
}
