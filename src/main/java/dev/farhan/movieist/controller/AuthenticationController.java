package dev.farhan.movieist.controller;

import dev.farhan.movieist.dto.LoginUserDto;
import dev.farhan.movieist.dto.RegisterUserDto;
import dev.farhan.movieist.dto.VerifyUserDto;
import dev.farhan.movieist.model.RefreshToken;
import dev.farhan.movieist.model.User;
import dev.farhan.movieist.repository.UserRepository;
import dev.farhan.movieist.responses.LoginResponse;
import dev.farhan.movieist.service.AuthenticationService;
import dev.farhan.movieist.service.JwtService;
import dev.farhan.movieist.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }



    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto,
                                          HttpServletResponse response,
                                          HttpServletRequest request) {
        try {
            // ✅ Authenticate the user
            User authenticatedUser = authenticationService.authenticate(loginUserDto);

            // ✅ Generate Access Token
            String jwtToken = jwtService.generateToken(authenticatedUser);

            // ✅ Generate Refresh Token (changed: call refreshTokenService.mint)
            String userAgentHash = refreshTokenService.sha256(request.getHeader("User-Agent"));
            String ipAddress = request.getRemoteAddr();
            String refreshToken = refreshTokenService.mint(
                    authenticatedUser.getId(),
                    userAgentHash,
                    ipAddress
            );

            // ✅ Build refresh token cookie (changed: added cookie handling)
            ResponseCookie refreshCookie = refreshTokenService.buildRefreshCookie(
                    refreshToken,
                    true
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


            // ✅ Return both access token + expiry info
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setId(authenticatedUser.getId().toHexString());
            loginResponse.setUsername(authenticatedUser.getUsername());
            loginResponse.setLikedMoviesIds(authenticatedUser.getLikedMoviesIds());
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());
            loginResponse.setEmail(authenticatedUser.getEmail());
            return ResponseEntity.ok(loginResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account Verified Successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request,
                                                HttpServletResponse response) {
        try {
            // 1️⃣ Extract refresh token from cookie
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
            }

            // 2️⃣ Validate refresh token
            RefreshToken storedToken = refreshTokenService.validateAndGet(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));

            // 3️⃣ CHANGED: Load User (you only had userId, so fetch via UserRepository instead)
            User user = userRepository.findById(storedToken.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 4️⃣ Generate new access token
            String newAccessToken = jwtService.generateToken(user);

            // 5️⃣ Rotate refresh token
            String newRefreshValue = refreshTokenService.rotate(
                    storedToken,
                    hashUserAgent(request.getHeader("User-Agent")), // helper
                    request.getRemoteAddr()
            );

            // 6️⃣ Send new refresh token cookie
            ResponseCookie newRefreshCookie = refreshTokenService.buildRefreshCookie(
                    newRefreshValue, true
            );
            response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

            // 7️⃣ Return new access token
            Map<String, Object> body = new HashMap<>();
            body.put("token", newAccessToken);
            body.put("expiresIn", jwtService.getExpirationTime());

            return ResponseEntity.ok(body);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /** 🔹 CHANGED: Helper to hash User-Agent consistently */
    private String hashUserAgent(String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    digest.digest(userAgent.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            return null;
        }
    }


}
