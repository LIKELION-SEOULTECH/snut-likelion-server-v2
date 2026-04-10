package com.snut_likelion.domain.user.service;

import com.snut_likelion.domain.user.dto.res.SayingResponse;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SayingServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SayingService sayingService;

    @Test
    void getSayings_returnsList() {
        User u1 = User.builder().username("kim").saying("열정").build();
        User u2 = User.builder().username("lee").saying("도전").build();
        when(userRepository.findAllBySayingIsNotNull()).thenReturn(List.of(u1, u2));

        List<SayingResponse> result = sayingService.getSayings();

        assertThat(result).hasSize(2)
                .extracting(SayingResponse::getSaying)
                .containsExactlyInAnyOrder("열정", "도전");
    }

    @Test
    void getSayings_empty_returnsEmptyList() {
        when(userRepository.findAllBySayingIsNotNull()).thenReturn(List.of());

        List<SayingResponse> result = sayingService.getSayings();

        assertThat(result).isEmpty();
    }
}
