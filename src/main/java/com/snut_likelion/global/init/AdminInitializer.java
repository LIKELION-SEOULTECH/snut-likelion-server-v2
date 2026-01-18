package com.snut_likelion.global.init;

import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.email}")
    private String adminEmail;

    @Value("${admin.init.password}")
    private String adminPassword;

    @Value("${snut.likelion.current-generation}")
    private String currentGeneration;

    @Override
    @Transactional
    public void run(String... args) {
        if(userRepository.existsByEmail(adminEmail)) {
            log.info("관리자 계정이 이미 존재합니다. {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .username("superUser")
                .password(passwordEncoder.encode(adminPassword))
                .phoneNumber("01000000000")
                .build();
        admin.generateCurrentLionInfo(
                Integer.parseInt(currentGeneration),
                Part.PLANNING,
                Role.ROLE_ADMIN,
                null);

        userRepository.save(admin);

        log.warn("================================================");
        log.warn("✓ 슈퍼 관리자 계정이 생성되었습니다.");
        log.warn("  이메일: {}", adminEmail);
        log.warn("  기수: {}기", currentGeneration);
        log.warn("  ⚠️  로그인 후 반드시 비밀번호를 변경하세요!");
        log.warn("================================================");
    }
}