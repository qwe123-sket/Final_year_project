package com.example.final_year_project.service;

import com.example.final_year_project.dto.user.DashboardVO;
import com.example.final_year_project.dto.user.PasswordChangeRequest;
import com.example.final_year_project.dto.user.UserStatsVO;
import com.example.final_year_project.dto.user.UserUpdateRequest;
import com.example.final_year_project.dto.user.UserVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.NoteTag;
import com.example.final_year_project.entity.Tag;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FavoriteRepository favoriteRepository;
    private final NoteLikeRepository noteLikeRepository;
    private final NoteTagRepository noteTagRepository;
    private final TagRepository tagRepository;
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

    public DashboardVO getDashboard(Long userId) {
        UserStatsVO stats = getStats(userId);

        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Note> recentNotes = noteRepository.findByUserIdAndCreatedAtAfter(userId, since);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        ZoneId zone = ZoneId.systemDefault();

        // last 30 days, by date
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i).format(fmt), 0L);
        }
        for (Note n : recentNotes) {
            String day = n.getCreatedAt().atZone(zone).toLocalDate().format(fmt);
            dailyMap.merge(day, 1L, Long::sum);
        }
        List<DashboardVO.DailyCount> dailyNotes = dailyMap.entrySet().stream()
                .map(e -> new DashboardVO.DailyCount(e.getKey(), e.getValue())).toList();

        // views per day
        Map<String, Long> viewMap = new LinkedHashMap<>(dailyMap);
        for (var key : viewMap.keySet()) viewMap.put(key, 0L);
        for (Note n : recentNotes) {
            String day = n.getCreatedAt().atZone(zone).toLocalDate().format(fmt);
            viewMap.merge(day, n.getViewCount(), Long::sum);
        }
        List<DashboardVO.DailyCount> dailyViews = viewMap.entrySet().stream()
                .map(e -> new DashboardVO.DailyCount(e.getKey(), e.getValue())).toList();

        // tag distribution (top 10)
        List<Note> allNotes = noteRepository.findByUserIdAndCreatedAtAfter(userId, Instant.EPOCH);
        List<Long> noteIds = allNotes.stream().map(Note::getId).toList();
        List<DashboardVO.TagCount> tagDistribution = new ArrayList<>();
        if (!noteIds.isEmpty()) {
            var noteTags = noteTagRepository.findByNoteIdIn(noteIds);
            Map<Long, Long> tagCount = new HashMap<>();
            for (NoteTag nt : noteTags) {
                tagCount.merge(nt.getTagId(), 1L, Long::sum);
            }
            Set<Long> tagIds = tagCount.keySet();
            if (!tagIds.isEmpty()) {
                Map<Long, String> nameMap = new HashMap<>();
                for (Tag t : tagRepository.findByIdIn(new ArrayList<>(tagIds))) {
                    nameMap.put(t.getId(), t.getName());
                }
                tagCount.entrySet().stream()
                        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                        .limit(10)
                        .forEach(e -> {
                            String name = nameMap.getOrDefault(e.getKey(), "unknown");
                            tagDistribution.add(new DashboardVO.TagCount(name, e.getValue()));
                        });
            }
        }

        return DashboardVO.builder()
                .stats(stats)
                .dailyNotes(dailyNotes)
                .dailyViews(dailyViews)
                .tagDistribution(tagDistribution)
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
