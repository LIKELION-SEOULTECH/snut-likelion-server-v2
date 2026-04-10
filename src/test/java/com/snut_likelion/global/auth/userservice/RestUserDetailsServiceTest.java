package com.snut_likelion.global.auth.userservice;

import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    RestUserDetailsService restUserDetailsService;

    @Test
    void loadUserByUsername_success() {
        ReflectionTestUtils.setField(restUserDetailsService, "currentGeneration", 14);
        User user = User.builder().id(1L).email("test@test.com").username("tester").build();
        when(userRepository.findWithLionInfoByEmail("test@test.com")).thenReturn(Optional.of(user));

        SnutLikeLionUser result = restUserDetailsService.loadUserByUsername("test@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        ReflectionTestUtils.setField(restUserDetailsService, "currentGeneration", 14);
        when(userRepository.findWithLionInfoByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restUserDetailsService.loadUserByUsername("none@test.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }
}
