package com.betoniarka.biblioteka.config;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.appuser.AppUserRole;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        seedAdminUser();
        seedCategories();
    }

    private void seedAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists.");
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setEmail("admin@example.com");
        admin.setFirstname("Admin");
        admin.setLastname("User");
        admin.setRole(AppUserRole.ADMIN);

        userRepository.save(admin);
        log.info("Default admin user created (admin/admin).");
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already seeded.");
            return;
        }

        List<String> categoryNames = List.of("Fantasy", "Science Fiction", "History", "Horror", "Classic", "Mystery");
        
        List<Category> categories = categoryNames.stream()
                .map(name -> {
                    Category category = new Category();
                    category.setName(name);
                    return category;
                })
                .toList();

        categoryRepository.saveAll(categories);
        log.info("Default categories seeded.");
    }
}
