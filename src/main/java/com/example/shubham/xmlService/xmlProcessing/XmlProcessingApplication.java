package com.example.shubham.xmlService.xmlProcessing;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SecurityScheme(
		name = "bearerAuth", // Must match the name in @SecurityRequirement
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
public class XmlProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmlProcessingApplication.class, args);
	}

}
