package com.example.final_year_project.service;

import com.example.final_year_project.dto.user.PasswordChangeRequest;
import com.example.final_year_project.dto.user.UserUpdateRequest;
import com.example.final_year_project.dto.user.UserVO;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserVO getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        return toVO(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, UserUpdateRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(userId)) throw new BusinessException("Email already in use");
            });
            user.setEmail(req.getEmail().trim());
        }
        if (req.getNickname() != null) user.setNickname(req.getNickname().trim());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar().trim());
        user = userRepository.save(user);
        return toVO(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private UserVO toVO(User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setEmail(u.getEmail());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setRole(u.getRole());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
