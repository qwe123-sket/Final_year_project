package com.example.final_year_project.config;

import com.example.final_year_project.entity.*;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;



@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetLoader {

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final TagRepository tagRepo;
    private final NoteTagRepository noteTagRepo;
    private final NoteLikeRepository likeRepo;
    private final FavoriteRepository favRepo;
    private final BrowseRecordRepository browseRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * 批量导入笔记。
     * 如果 username 对应的用户不存在，会自动创建一个。
     *
     * @param items 笔记列表，每个 item 包含：username, title, content, tags
     * @return 导入的笔记数量
     */
    @Transactional
    public int importNotes(List<NoteImportItem> items) {
        int count = 0;
        String defaultPwd = passwordEncoder.encode("dataset123");

        for (NoteImportItem item : items) {
            // 查找或创建用户
            User user = userRepo.findByUsername(item.username).orElseGet(() -> {
                User newUser = User.builder()
                        .username(item.username)
                        .password(defaultPwd)
                        .nickname(item.username)
                        .build();
                return userRepo.save(newUser);
            });

            // 创建笔记
            Note note = noteRepo.save(Note.builder()
                    .userId(user.getId())
                    .title(item.title)
                    .content(item.content)
                    .status(NoteStatus.APPROVED)
                    .viewCount(item.viewCount != null ? item.viewCount : 0L)
                    .build());

            // 关联标签
            if (item.tags != null) {
                for (String tagName : item.tags) {
                    Tag tag = tagRepo.findByName(tagName.trim())
                            .orElseGet(() -> tagRepo.save(Tag.builder().name(tagName.trim()).build()));
                    noteTagRepo.save(NoteTag.builder().noteId(note.getId()).tagId(tag.getId()).build());
                }
            }
            count++;
        }
        log.info("Imported {} notes from dataset", count);
        return count;
    }

    /**
     * 批量导入用户交互数据（点赞、收藏）。
     * 用户必须已存在（可以先通过 importNotes 自动创建）。
     *
     * @param interactions 交互列表
     * @return 导入的交互记录数
     */
    @Transactional
    public int importInteractions(List<InteractionItem> interactions) {
        int count = 0;
        for (InteractionItem item : interactions) {
            var userOpt = userRepo.findByUsername(item.username);
            if (userOpt.isEmpty()) {
                log.warn("User not found: {}, skipping interaction", item.username);
                continue;
            }
            Long userId = userOpt.get().getId();

            if (noteRepo.findById(item.noteId).isEmpty()) {
                log.warn("Note not found: {}, skipping", item.noteId);
                continue;
            }

            if ("like".equals(item.type)) {
                if (!likeRepo.existsByUserIdAndNoteId(userId, item.noteId)) {
                    likeRepo.save(NoteLike.builder().userId(userId).noteId(item.noteId).build());
                    count++;
                }
            } else if ("favorite".equals(item.type)) {
                if (!favRepo.existsByUserIdAndNoteId(userId, item.noteId)) {
                    favRepo.save(Favorite.builder().userId(userId).noteId(item.noteId).build());
                    count++;
                }
            }
        }
        log.info("Imported {} interaction records", count);
        return count;
    }

    // ----- 数据结构定义 -----

    /**
     * 笔记导入数据项。
     * 在算法模块中解析你的 CSV/JSON，填充这个对象即可。
     */
    public static class NoteImportItem {
        public String username;
        public String title;
        public String content;
        public List<String> tags;
        public Long viewCount;

        public NoteImportItem() {}

        public NoteImportItem(String username, String title, String content, List<String> tags) {
            this.username = username;
            this.title = title;
            this.content = content;
            this.tags = tags;
        }
    }

    /**
     * 用户交互数据项。
     * type: "like" 或 "favorite"
     */
    public static class InteractionItem {
        public String username;
        public Long noteId;
        public String type; // "like" or "favorite"

        public InteractionItem() {}

        public InteractionItem(String username, Long noteId, String type) {
            this.username = username;
            this.noteId = noteId;
            this.type = type;
        }
    }
}
