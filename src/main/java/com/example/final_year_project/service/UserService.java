package com.example.final_year_project.service;

import com.example.final_year_project.dto.user.PasswordChangeRequest;
import com.example.final_year_project.dto.user.UserStatsVO;
import com.example.final_year_project.dto.user.UserUpdateRequest;
import com.example.final_year_project.dto.user.UserVO;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.FavoriteRepository;
import com.example.final_year_project.repository.NoteLikeRepository;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FavoriteRepository favoriteRepository;
    private final NoteLikeRepository noteLikeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserVO getProfile(Long userId) {
        User user = findUserOrThrow(userId);
        return toVO(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, UserUpdateRequest req) {
        User user = findUserOrThrow(userId);

        // 邮箱查重
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            userRepository.findByEmail(req.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new BusinessException("This email is already taken");
                }
            });
            user.setEmail(req.getEmail().trim());
        }
        if (req.getNickname() != null) user.setNickname(req.getNickname().trim());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar().trim());

        return toVO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest req) {
        User user = findUserOrThrow(userId);
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    /** 个人中心的数据统计 */
    public UserStatsVO getStats(Long userId) {
        long published = noteRepository.countByUserId(userId);
        long views = noteRepository.sumViewCountByUserId(userId);
        long likes = noteLikeRepository.countLikesOnUserNotes(userId);
        long favorited = favoriteRepository.countFavoritesOnUserNotes(userId);
        long myFavs = favoriteRepository.countByUserId(userId);

        return UserStatsVO.builder()
                .notesPublished(published)
                .totalViews(views)
                .totalLikes(likes)
                .totalFavorited(favorited)
                .favoritesCount(myFavs)
                .build();
    }

    // ----------

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
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
