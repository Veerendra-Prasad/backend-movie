package dev.farhan.movieist.service;

import dev.farhan.movieist.dto.LoginUserDto;
import dev.farhan.movieist.dto.RegisterUserDto;
import dev.farhan.movieist.dto.VerifyUserDto;
import dev.farhan.movieist.model.User;
import dev.farhan.movieist.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User signup(RegisterUserDto input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));  // Todo : change it back to 10 minutes
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify user account");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);

        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset=\\"UTF-8\\">
                  <title>Verify Your Email</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      background-color: #f4f4f4;
                      margin: 0;
                      padding: 0;
                    }
                    .container {
                      max-width: 600px;
                      margin: 30px auto;
                      background-color: #ffffff;
                      padding: 20px 30px;
                      border-radius: 8px;
                      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                      text-align: center;
                      padding-bottom: 20px;
                    }
                    .code-box {
                      font-size: 24px;
                      letter-spacing: 4px;
                      font-weight: bold;
                      background-color: #f1f1f1;
                      padding: 15px;
                      border-radius: 6px;
                      text-align: center;
                      margin: 20px 0;
                    }
                    .footer {
                      text-align: center;
                      font-size: 12px;
                      color: #999999;
                      margin-top: 30px;
                    }
                    a {
                      color: #007bff;
                      text-decoration: none;
                    }
                  </style>
                </head>
                <body>
                  <div class=\\"container\\">
                    <div class=\\"header\\">
                      <h2>Email Verification</h2>
                      <p>Please use the code below to verify your email address.</p>
                    </div>
                
                    <div class=\\"code-box\\">
                      %s
                    </div>
                
                    <p>If you didn’t request this, you can safely ignore this email.</p>
                
                    <p>Thanks,<br>The Team</p>
                
                    <div class=\\"footer\\">
                      &copy; 2025 Your Company. All rights reserved.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(verificationCode);
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlContent);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
