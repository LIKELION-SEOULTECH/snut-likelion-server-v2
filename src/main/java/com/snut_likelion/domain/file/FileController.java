package com.snut_likelion.domain.file;

import com.snut_likelion.global.provider.FileProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Tag(name = "File", description = "파일 다운로드 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileProvider fileProvider;

    // 파일 다운로드
    @Operation(summary = "파일 다운로드", description = "서버에 저장된 파일을 다운로드합니다.")
    @GetMapping(value = "/download")
//    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("fileName") String fileName,
            HttpServletRequest request
    ) throws IOException {
        Resource file = fileProvider.getFile(fileName);

        String contentType = URLConnection
                .guessContentTypeFromName(fileName);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.getFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(file);
    }
}
