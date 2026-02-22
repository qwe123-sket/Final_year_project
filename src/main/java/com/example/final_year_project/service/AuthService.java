package com.example.final_year_project.service;

import com.example.final_year_project.dto.auth.LoginRequest;
import com.example.final_year_project.dto.auth.LoginResponse;
import com.example.final_year_project.dto.auth.RegisterRequest;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import com.example.final_year_project.security.JwtUtil;
import com.example.final_year_project.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    @Value("${app.admin-register-secret:}")
    private String adminRegisterSecret;

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        // 校验图形验证码
        if (!captchaService.verify(req.getCaptchaKey(), req.getCaptchaCode())) {
            throw new BusinessException("Captcha is incorrect or expired");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        UserRole role = UserRole.USER;
        if (req.getAdminSecret() != null && !req.getAdminSecret().isBlank()
                && adminRegisterSecret != null && !adminRegisterSecret.isBlank()
                && req.getAdminSecret().equals(adminRegisterSecret)) {
            role = UserRole.ADMIN;
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail() != null ? req.getEmail().trim() : null)
                .nickname(req.getNickname() != null ? req.getNickname().trim() : null)
                .role(role)
                .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }

        LoginUser loginUser = new LoginUser(user);
        if (!loginUser.isAccountNonLocked()) {
            throw new BusinessException(403, "Your account has been disabled");
        }
        if (!loginUser.isEnabled()) {
            throw new BusinessException(403, "Account status abnormal, please contact admin");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }
}
