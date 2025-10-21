package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.UserResponseDto;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @PostMapping("/google")
    public ResponseEntity<UserResponseDto> googleLogin(@RequestBody Map<String, String> requestBody) {
        String idTokenString = requestBody.get("idToken");

        if (idTokenString == null) {
            return ResponseEntity.badRequest().build();
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String googleId = payload.getSubject();

            UserEntity userEntity = userService.findOrCreateUser(email, name, pictureUrl, googleId);

            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setId(userEntity.getId());
            userResponseDto.setEmail(userEntity.getEmail());
            userResponseDto.setName(userEntity.getName());
            userResponseDto.setProfilePictureUrl(userEntity.getProfilePictureUrl());
            userResponseDto.setUserCode(userEntity.getUserCode());
            userResponseDto.setRole(userEntity.getRole().name());
            userResponseDto.setCreatedAt(userEntity.getCreatedAt());
            userResponseDto.setUpdatedAt(userEntity.getUpdatedAt());

            return ResponseEntity.ok(userResponseDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
