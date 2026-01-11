package com.snut_likelion.domain.file;

import com.snut_likelion.global.provider.FileProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image", description = "이미지 조회 API")
@RequestMapping("/api/v1/images")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final FileProvider fileProvider;

    @Operation(summary = "이미지 조회", description = "이미지 파일명을 통해 해당 이미지 리소스를 직접 조회합니다.")
    @GetMapping(produces = "image/jpeg")
    public Resource getProjectImage(
            @Parameter(description = "조회할 이미지 파일명 (확장자 포함)")
            @RequestParam("imageName") String imageName
    ) {
        return fileProvider.getFile(imageName);
    }
}