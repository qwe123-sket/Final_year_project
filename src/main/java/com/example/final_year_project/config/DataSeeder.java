package com.example.final_year_project.config;

import com.example.final_year_project.entity.*;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 测试数据初始化。
 * 仅在数据库为空时自动填充一批样例数据，方便开发调试。
 * 如果数据库里已经有用户了就跳过，不会重复插入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final TagRepository tagRepo;
    private final NoteTagRepository noteTagRepo;
    private final NoteLikeRepository likeRepo;
    private final FavoriteRepository favRepo;
    private final ReplyRepository replyRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 用 alice 这个测试账号来判断，因为 admin 可能是用户手动注册的
        if (userRepo.findByUsername("alice").isPresent()) {
            log.info("Test data already seeded, skipping.");
            return;
        }
        log.info("Seeding test data...");

        // ===== 1. 创建用户（已存在的就复用） =====
        String encodedPwd = passwordEncoder.encode("123456");

        User admin = getOrCreateUser("admin", encodedPwd, "admin@example.com", "Administrator", UserRole.ADMIN);
        User alice = getOrCreateUser("alice", encodedPwd, "alice@example.com", "Alice Wang", UserRole.USER);
        User bob = getOrCreateUser("bob", encodedPwd, "bob@example.com", "Bob Li", UserRole.USER);
        User carol = getOrCreateUser("carol", encodedPwd, "carol@example.com", "Carol Zhang", UserRole.USER);
        User dave = getOrCreateUser("dave", encodedPwd, "dave@example.com", "Dave Chen", UserRole.USER);

        List<User> users = List.of(admin, alice, bob, carol, dave);
        log.info("Test users ready (password: 123456 for new accounts)");

        // ===== 2. 创建标签 =====
        String[] tagNames = {"Java", "Spring Boot", "Vue", "Frontend", "Backend",
                "Database", "Redis", "Algorithm", "Machine Learning", "Project",
                "Tutorial", "Tips", "Architecture", "DevOps", "Testing"};

        Map<String, Tag> tagMap = new LinkedHashMap<>();
        for (String name : tagNames) {
            tagMap.put(name, tagRepo.save(Tag.builder().name(name).build()));
        }

        // ===== 3. 创建笔记 =====
        // 每篇笔记的 [标题, 内容, 作者index, 标签...]
        Object[][] noteData = {
                {"Getting Started with Spring Boot",
                        "Spring Boot makes it easy to create stand-alone, production-grade Spring applications. "
                                + "In this note, I'll walk through setting up a new project from scratch using Spring Initializr. "
                                + "We'll cover project structure, dependency management with Maven, and writing your first REST controller. "
                                + "The key advantage of Spring Boot is its opinionated defaults - you can get a working web service "
                                + "running with just a few lines of code. Auto-configuration handles most of the boilerplate, "
                                + "so you can focus on business logic instead of XML configuration.\n\n"
                                + "Key steps:\n1. Go to start.spring.io and select your dependencies\n"
                                + "2. Import the generated project into your IDE\n"
                                + "3. Create a @RestController class\n"
                                + "4. Run the application with mvn spring-boot:run\n\n"
                                + "That's it! You now have a working REST API.",
                        1, "Java", "Spring Boot", "Tutorial"},

                {"Vue 3 Composition API Deep Dive",
                        "The Composition API is one of the biggest changes in Vue 3. Unlike the Options API where you organize "
                                + "code by options (data, methods, computed...), the Composition API lets you organize code by logical concern. "
                                + "This is especially useful for complex components where related logic gets scattered across different options.\n\n"
                                + "Key concepts:\n- ref() and reactive() for reactive state\n- computed() for derived state\n"
                                + "- watch() and watchEffect() for side effects\n- onMounted(), onUnmounted() lifecycle hooks\n"
                                + "- Custom composables for reusable logic (like our useTheme.js)\n\n"
                                + "The biggest win is code reuse. With composables, you can extract and share stateful logic between components "
                                + "without mixins or renderless components.",
                        2, "Vue", "Frontend", "Tutorial"},

                {"Understanding JWT Authentication",
                        "JSON Web Tokens (JWT) are a popular way to handle authentication in modern web applications. "
                                + "Unlike session-based auth where the server stores state, JWT is stateless - all the information "
                                + "is encoded in the token itself.\n\n"
                                + "A JWT has three parts: Header (algorithm info), Payload (claims like userId and role), "
                                + "and Signature (verification). The server signs the token with a secret key, and the client "
                                + "sends it back in the Authorization header on each request.\n\n"
                                + "Pros: stateless, scalable, works well with SPAs\n"
                                + "Cons: can't easily revoke tokens, token size can be large\n\n"
                                + "In our project, we use jjwt library to generate and validate tokens. The JwtAuthFilter extracts "
                                + "the token from each request and sets the SecurityContext if valid.",
                        0, "Java", "Spring Boot", "Backend", "Architecture"},

                {"Redis Caching Strategies",
                        "Caching is essential for high-performance applications. Redis, as an in-memory data store, "
                                + "provides sub-millisecond response times for cached data.\n\n"
                                + "Common caching patterns:\n"
                                + "1. Cache-aside: App checks cache first, falls back to DB on miss, then populates cache\n"
                                + "2. Write-through: Every write goes to both cache and DB\n"
                                + "3. Write-behind: Writes go to cache first, asynchronously sync to DB\n\n"
                                + "In our project, we use cache-aside for view counts and like counts. "
                                + "The NoteCacheService increments view count in Redis and periodically syncs back to MySQL. "
                                + "This dramatically reduces DB write pressure during high traffic.",
                        3, "Redis", "Backend", "Architecture"},

                {"Building a Masonry Layout with CSS",
                        "The Pinterest-style masonry layout is popular for content-heavy sites. "
                                + "While JavaScript libraries like Masonry.js exist, pure CSS can achieve a similar effect "
                                + "using column-count.\n\n"
                                + "```css\n.masonry {\n  column-count: 3;\n  column-gap: 20px;\n}\n.masonry-item {\n  break-inside: avoid;\n  margin-bottom: 20px;\n}\n```\n\n"
                                + "The key property is break-inside: avoid, which prevents cards from being split across columns. "
                                + "Combined with responsive column-count via media queries, you get a clean grid that adapts to screen size.\n\n"
                                + "Limitation: items flow top-to-bottom per column, not left-to-right. For true masonry ordering, "
                                + "you'd need JS or CSS Grid with masonry value (still experimental).",
                        1, "Frontend", "Vue", "Tips"},

                {"MySQL Index Optimization",
                        "Proper indexing can make or break database performance. Here are some practical tips from "
                                + "working on this project.\n\n"
                                + "1. Always index foreign keys (user_id, note_id) - JPA doesn't do this automatically\n"
                                + "2. Composite indexes should follow the leftmost prefix rule\n"
                                + "3. Use EXPLAIN to check if your queries are using indexes\n"
                                + "4. Don't over-index - each index slows down writes\n"
                                + "5. Consider covering indexes for frequently queried columns\n\n"
                                + "In our Note entity, we have indexes on user_id, status, and created_at. "
                                + "The status + created_at combination is particularly useful for the listApproved() query "
                                + "which filters by APPROVED status and sorts by creation time.",
                        4, "Database", "Backend", "Tips"},

                {"Dark Mode Implementation in Vue",
                        "Adding dark mode to a Vue app involves three main pieces:\n\n"
                                + "1. CSS Variables: Define color tokens that change based on a data-theme attribute\n"
                                + "2. Theme Composable: Manage the current theme state and persist user preference\n"
                                + "3. System Detection: Respect prefers-color-scheme media query as default\n\n"
                                + "Our useTheme.js composable stores the preference in localStorage and sets "
                                + "a data-theme attribute on the <html> element. All colors reference CSS variables "
                                + "like var(--color-bg), var(--color-text), etc.\n\n"
                                + "Pro tip: Use watchEffect() instead of watch() for the DOM sync - it runs immediately "
                                + "and tracks dependencies automatically, so the theme applies before the first paint.",
                        2, "Vue", "Frontend", "Tutorial"},

                {"N+1 Query Problem and How to Fix It",
                        "The N+1 problem is one of the most common performance issues in ORM-based applications. "
                                + "It happens when you load a list of N entities and then make a separate query for each one "
                                + "to fetch related data.\n\n"
                                + "Example: Loading 20 notes, then calling userRepo.findById() for each note's author = 21 queries!\n\n"
                                + "Solutions:\n"
                                + "1. Batch loading: Collect all user IDs, do one findAllById() call, build a Map\n"
                                + "2. JOIN FETCH in JPQL: SELECT n FROM Note n JOIN FETCH n.user\n"
                                + "3. @EntityGraph annotation on repository methods\n\n"
                                + "We went with option 1 (batch loading) in our toBatchVO() method because it gives us "
                                + "the most control and works well with our DTO pattern. The same approach is used for "
                                + "batch loading tags and like counts.",
                        0, "Java", "Database", "Architecture"},

                {"Element Plus Form Validation Tips",
                        "Form validation in Element Plus is powerful but has some gotcha moments. Here's what I learned:\n\n"
                                + "1. Always use ref() for the form model, not reactive()\n"
                                + "2. Rules must match the model's property names exactly\n"
                                + "3. Use formRef.value.validate() with async/await\n"
                                + "4. Custom validators receive (rule, value, callback) - call callback() on success, callback(new Error()) on fail\n\n"
                                + "For password strength validation, we combined a custom validator with a visual strength bar. "
                                + "The validator checks length, uppercase, lowercase, and special characters, while the bar "
                                + "provides real-time visual feedback.",
                        3, "Vue", "Frontend", "Tips"},

                {"Spring Security Filter Chain Explained",
                        "Understanding the Spring Security filter chain is crucial for debugging auth issues. "
                                + "Here's the order of operations for each request:\n\n"
                                + "1. CorsFilter - handles CORS preflight\n"
                                + "2. SecurityContextPersistenceFilter - loads/saves SecurityContext\n"
                                + "3. Our JwtAuthFilter - extracts JWT, sets authentication\n"
                                + "4. UsernamePasswordAuthenticationFilter - form login (we don't use this)\n"
                                + "5. ExceptionTranslationFilter - converts auth exceptions to HTTP responses\n"
                                + "6. FilterSecurityInterceptor - checks authorization rules\n\n"
                                + "We add our JwtAuthFilter before UsernamePasswordAuthenticationFilter so that "
                                + "JWT-authenticated requests are recognized before the default form login check.\n\n"
                                + "Common pitfall: forgetting to add new public endpoints to permitAll() in SecurityConfig!",
                        4, "Spring Boot", "Backend", "Architecture"},

                {"Pinia State Management Best Practices",
                        "Pinia is Vue 3's recommended state management library, replacing Vuex. Some tips:\n\n"
                                + "1. Use setup stores (function syntax) over option stores - they feel more natural with Composition API\n"
                                + "2. Keep stores focused - one store per domain (user, notes, etc.)\n"
                                + "3. Don't put everything in stores - component-local state is fine for UI-only state\n"
                                + "4. Use computed for derived state instead of duplicating data\n\n"
                                + "Our useUserStore handles auth state: token, user info, login/logout actions. "
                                + "We persist the token and user info to localStorage so it survives page refreshes. "
                                + "The store exposes isLogin and isAdmin as computed properties for easy template usage.",
                        1, "Vue", "Frontend"},

                {"Recommendation System Architecture Overview",
                        "Our recommendation system is designed with a clean separation between data collection "
                                + "and algorithm execution.\n\n"
                                + "Data collection layer (already implemented):\n"
                                + "- BrowseRecord: tracks what users read and for how long\n"
                                + "- NoteLike: tracks user likes\n"
                                + "- Favorite: tracks user bookmarks\n"
                                + "- NoteTag: content-based features through tagging\n\n"
                                + "Algorithm interface (to be implemented):\n"
                                + "- RecommendService provides two entry points:\n"
                                + "  1. getRecommendListByNoteIds(List<Long>) - for simple ranked ID lists\n"
                                + "  2. getRecommendListByNoteIdsWithScores(Map<Long, Double>) - for scored results\n\n"
                                + "The algorithm module can use collaborative filtering, content-based filtering, "
                                + "or a hybrid approach. All the user interaction data is available in the database.",
                        0, "Algorithm", "Machine Learning", "Architecture", "Project"},

                {"Deploying Spring Boot + Vue to Production",
                        "When it's time to deploy, here's the general approach:\n\n"
                                + "1. Build the Vue frontend: npm run build (outputs to dist/)\n"
                                + "2. Copy dist/ contents to Spring Boot's src/main/resources/static/\n"
                                + "3. Build the Spring Boot JAR: mvn package\n"
                                + "4. Deploy the single JAR file to your server\n\n"
                                + "Or keep them separate:\n"
                                + "- Frontend behind Nginx on port 80\n"
                                + "- Backend on port 8080\n"
                                + "- Nginx proxy_pass /api to backend\n\n"
                                + "Don't forget:\n"
                                + "- Set up MySQL and Redis on the server\n"
                                + "- Configure application.yml with production database credentials\n"
                                + "- Use environment variables for secrets, not hardcoded values\n"
                                + "- Enable HTTPS with Let's Encrypt",
                        4, "DevOps", "Spring Boot", "Project"},

                {"Writing Good Git Commit Messages",
                        "Your future self will thank you for writing clear commit messages. My approach:\n\n"
                                + "Format: <type>: <short description>\n\n"
                                + "Types: feat, fix, refactor, docs, test, chore\n\n"
                                + "Good examples:\n"
                                + "- feat: add like/unlike endpoints for notes\n"
                                + "- fix: handle null pointer in user profile query\n"
                                + "- refactor: extract batch VO builder to reduce N+1\n\n"
                                + "Bad examples:\n"
                                + "- update code\n"
                                + "- fix bug\n"
                                + "- WIP\n\n"
                                + "Keep commits small and focused. One logical change per commit makes it easy to "
                                + "review, revert, and understand the project history.",
                        2, "Project", "Tips"},

                {"Responsive Design with CSS Media Queries",
                        "Making a web app responsive doesn't have to be painful. Here's our approach:\n\n"
                                + "1. Mobile-first: Start with the mobile layout, add breakpoints for larger screens\n"
                                + "2. Use relative units: rem, em, %, vh/vw instead of fixed px\n"
                                + "3. CSS Grid + Flexbox: Grid for page layout, Flexbox for component internals\n"
                                + "4. clamp() for fluid typography: font-size: clamp(1rem, 2.5vw, 1.5rem)\n\n"
                                + "Our masonry layout uses column-count with three breakpoints:\n"
                                + "- Desktop (>900px): 3 columns\n"
                                + "- Tablet (520-900px): 2 columns\n"
                                + "- Mobile (<520px): 1 column\n\n"
                                + "The sidebar collapses on mobile and the top bar becomes more compact.",
                        3, "Frontend", "Tips"},

                {"Rate Limiting to Prevent Abuse",
                        "Every public API needs some form of rate limiting to prevent brute-force attacks and abuse. "
                                + "We implemented a simple in-memory rate limiter for auth endpoints.\n\n"
                                + "The approach:\n"
                                + "- Use a ConcurrentHashMap to track request counts per IP\n"
                                + "- Each IP gets a time window (60 seconds) and a request limit (15)\n"
                                + "- If exceeded, return HTTP 429 Too Many Requests\n"
                                + "- Windows reset automatically after expiry\n\n"
                                + "For production, you'd want to use Redis-based rate limiting (e.g., with a sliding window algorithm) "
                                + "so it works across multiple server instances. Our current implementation is fine for a single-server setup.",
                        0, "Backend", "Spring Boot", "Architecture"},

                {"How Vite Dev Server Proxy Works",
                        "When developing with a separate frontend (Vite on :5173) and backend (Spring Boot on :8080), "
                                + "you'll run into CORS issues. Vite's proxy config is the clean solution.\n\n"
                                + "In vite.config.js:\n"
                                + "```js\nserver: {\n  proxy: {\n    '/api': {\n      target: 'http://localhost:8080',\n      changeOrigin: true\n    }\n  }\n}\n```\n\n"
                                + "This tells Vite: any request to /api/* on port 5173 gets forwarded to port 8080. "
                                + "The browser only talks to 5173, so no CORS issues. In production, Nginx handles the same routing.\n\n"
                                + "We still have CORS config in SecurityConfig as a safety net, but the proxy is what makes "
                                + "local development smooth.",
                        1, "Frontend", "DevOps", "Tips"},

                {"Unit Testing Spring Boot Services",
                        "Testing is something I need to do more of, honestly. Here's what I've learned about testing "
                                + "Spring Boot services.\n\n"
                                + "For service layer tests, you want to mock the repositories:\n"
                                + "```java\n@ExtendWith(MockitoExtension.class)\nclass NoteServiceTest {\n    @Mock NoteRepository noteRepo;\n    @InjectMocks NoteService noteService;\n    \n    @Test\n    void shouldThrowWhenNoteNotFound() {\n        when(noteRepo.findById(1L)).thenReturn(Optional.empty());\n        assertThrows(BusinessException.class, () -> noteService.getById(1L, null));\n    }\n}\n```\n\n"
                                + "For integration tests, use @SpringBootTest with @Transactional so tests auto-rollback.\n\n"
                                + "Key principle: test behavior, not implementation. Don't test that a method calls .save() - "
                                + "test that the expected state change actually happens.",
                        4, "Testing", "Java", "Spring Boot"},
        };

        Random rng = new Random(42);
        List<Note> allNotes = new ArrayList<>();

        for (Object[] row : noteData) {
            String title = (String) row[0];
            String content = (String) row[1];
            int authorIdx = (int) row[2];
            User author = users.get(authorIdx);

            // 随机浏览量，让数据看起来真实一点
            long views = rng.nextInt(500) + 20;

            Note note = noteRepo.save(Note.builder()
                    .userId(author.getId())
                    .title(title)
                    .content(content)
                    .status(NoteStatus.APPROVED)
                    .viewCount(views)
                    .build());
            allNotes.add(note);

            // 关联标签
            for (int i = 3; i < row.length; i++) {
                String tn = (String) row[i];
                Tag tag = tagMap.get(tn);
                if (tag != null) {
                    noteTagRepo.save(NoteTag.builder().noteId(note.getId()).tagId(tag.getId()).build());
                }
            }
        }
        log.info("Created {} test notes with tags", allNotes.size());

        // ===== 4. 随机生成一些点赞和收藏 =====
        int likeCount = 0, favCount = 0;
        for (Note note : allNotes) {
            for (User u : users) {
                if (u.getId().equals(note.getUserId())) continue;
                // 约 40% 概率点赞
                if (rng.nextDouble() < 0.4) {
                    likeRepo.save(NoteLike.builder().userId(u.getId()).noteId(note.getId()).build());
                    likeCount++;
                }
                // 约 25% 概率收藏
                if (rng.nextDouble() < 0.25) {
                    favRepo.save(Favorite.builder().userId(u.getId()).noteId(note.getId()).build());
                    favCount++;
                }
            }
        }
        log.info("Created {} likes and {} favorites", likeCount, favCount);

        // ===== 5. 一些评论 =====
        String[] sampleReplies = {
                "Great write-up, very helpful!",
                "Thanks for sharing this, I learned something new.",
                "I had the exact same issue, this solved it for me.",
                "Could you go into more detail about the configuration part?",
                "Nice summary! Bookmarked for reference.",
                "This is exactly what I was looking for.",
                "Well explained, especially the diagrams.",
                "I disagree about the caching strategy, but good overview otherwise.",
                "Any plans to cover testing in a follow-up?",
                "Learned a lot from this, thanks!",
        };

        int replyCount = 0;
        for (int i = 0; i < allNotes.size(); i++) {
            Note note = allNotes.get(i);
            // 每篇笔记 1~3 条评论
            int numReplies = rng.nextInt(3) + 1;
            for (int j = 0; j < numReplies; j++) {
                User commenter = users.get(rng.nextInt(users.size()));
                String replyText = sampleReplies[rng.nextInt(sampleReplies.length)];
                replyRepo.save(Reply.builder()
                        .noteId(note.getId())
                        .userId(commenter.getId())
                        .content(replyText)
                        .build());
                replyCount++;
            }
        }
        log.info("Created {} replies", replyCount);

        log.info("=== Test data seeding complete ===");
        log.info("Test accounts (password for all: 123456):");
        log.info("  admin / alice / bob / carol / dave");
    }

    private User getOrCreateUser(String username, String encodedPwd, String email, String nickname, UserRole role) {
        return userRepo.findByUsername(username).orElseGet(() ->
                userRepo.save(User.builder()
                        .username(username).password(encodedPwd)
                        .email(email).nickname(nickname).role(role).build()));
    }
}
