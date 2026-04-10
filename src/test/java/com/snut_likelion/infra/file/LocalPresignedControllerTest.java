package com.snut_likelion.infra.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalPresignedControllerTest {

    @TempDir
    Path tempDir;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalPresignedController controller = new LocalPresignedController(tempDir.toString());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void files_prefix_key는_200을_반환한다() throws Exception {
        mockMvc.perform(
                put("/api/v1/files/local/presigned-put")
                        .param("key", "files/notices/uuid-sample.pdf")
                        .contentType("application/pdf")
                        .content("dummy content")
        ).andExpect(status().isOk());
    }

    @Test
    void images_prefix_key는_200을_반환한다() throws Exception {
        mockMvc.perform(
                put("/api/v1/files/local/presigned-put")
                        .param("key", "images/notices/uuid-sample.png")
                        .contentType("image/png")
                        .content("dummy content")
        ).andExpect(status().isOk());
    }

    @Test
    void 허용되지_않는_prefix_key는_400을_반환한다() throws Exception {
        mockMvc.perform(
                put("/api/v1/files/local/presigned-put")
                        .param("key", "other/notices/uuid-sample.pdf")
                        .content("dummy content")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 경로_탈출_시도는_400을_반환한다() throws Exception {
        mockMvc.perform(
                put("/api/v1/files/local/presigned-put")
                        .param("key", "images/../etc/passwd")
                        .content("dummy content")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 빈_key는_400을_반환한다() throws Exception {
        mockMvc.perform(
                put("/api/v1/files/local/presigned-put")
                        .param("key", "")
                        .content("dummy content")
        ).andExpect(status().isBadRequest());
    }
}
