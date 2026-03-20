package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.user.dto.res.SayingResponse;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SayingService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SayingResponse> getSayings() {
        List<User> users = userRepository.findAllBySayingIsNotNull();
        return users.stream()
                .map(SayingResponse::from)
                .toList();
    }
}
