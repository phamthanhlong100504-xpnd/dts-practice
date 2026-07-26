package com.dts.practice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "service", "DTS Practice Service",
            "version", "1.0.0",
            "description", "Thi thu sat hach lai xe"
        );
    }
}
