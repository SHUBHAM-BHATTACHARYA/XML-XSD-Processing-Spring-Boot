package com.example.shubham.xmlService.xmlProcessing.service;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtGeneratorService {

    @Value("${jwt.private-key-location}")
    private Resource privateKeyResource;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    private PrivateKey privateKey;

    @PostConstruct
    public void loadPrivateKey() throws Exception {
        String pemContent = new String(
                privateKeyResource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String cleanPem = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedKey = Base64.getDecoder().decode(cleanPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(subject)
                .claims(extraClaims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}
