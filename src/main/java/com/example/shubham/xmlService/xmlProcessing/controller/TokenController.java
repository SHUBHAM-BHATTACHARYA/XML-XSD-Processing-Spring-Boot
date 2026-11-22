package com.example.shubham.xmlService.xmlProcessing.controller;

import com.example.shubham.xmlService.xmlProcessing.service.JwtGeneratorService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtGeneratorService jwtGeneratorService;

    public TokenController(JwtGeneratorService jwtGeneratorService) {
        this.jwtGeneratorService = jwtGeneratorService;
    }

    @PostMapping("/generate-token")
    public Map<String, String> getToken(@RequestParam(defaultValue = "xml-client") String clientId) {
        String token = jwtGeneratorService.generateToken(
                clientId,
                Map.of("scope", "xml:read xml:write")
        );
        return Map.of("token", token);
    }
}