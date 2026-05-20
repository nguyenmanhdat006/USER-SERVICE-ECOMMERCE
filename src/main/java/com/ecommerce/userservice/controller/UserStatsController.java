package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users/stats")
@RequiredArgsConstructor
@Slf4j
public class UserStatsController {

    private final UserRepository userRepository;

    @GetMapping("/total-count")
    public ResponseEntity<Long> countTotalUsers() {
        log.info("Request received to count total users");
        Long count = userRepository.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-yesterday")
    public ResponseEntity<Long> countUsersYesterday() {
        log.info("Request received to count users registered yesterday");
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Long count = userRepository.countByCreatedAtBetween(yesterday, today);
        return ResponseEntity.ok(count);
    }
}
