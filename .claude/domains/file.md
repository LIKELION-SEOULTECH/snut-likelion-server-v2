# 도메인: File (파일 업로드)

## 개요

S3 Presigned URL 직접 업로드 방식을 사용한다.
서버를 경유하지 않으므로 메모리·트래픽 비용 없음.

---

## 4단계 업로드 플로우

```
[1] POST /api/v1/files/presigned-url
    Body: { fileName, contentType, storageType }
    → 서버: UUID 기반 S3 key 생성 + Presigned PUT URL 발급

[2] 서버 → 클라이언트: { key, presignedUrl }

[3] 클라이언트 → S3: presignedUrl로 파일 직접 PUT

[4] 도메인 저장 시 key 포함하여 서버에 요청
    → 서버: S3 실존 여부 확인 (HeadObject)
    → 통과 시 UploadedFile 엔티티 저장 + 도메인 연결
```

---

## S3 Key 형식

```
{storageRoot}/{category}/{UUID}-{originalName}.{ext}
```

예시:
- `images/blog/550e8400-e29b-41d4-a716-446655440000-thumbnail.png`
- `files/notice/550e8400-...-attachment.pdf`

**핵심 설계 결정**
- 서버가 key를 직접 생성 → Path Traversal 방지
- DB에는 URL이 아닌 key만 저장 → CDN 변경·버킷 이전 시 마이그레이션 불필요
- 다운로드 URL은 `FileProvider.buildFileDownloadUrl(key)`로 동적 생성

---

## FileStorageType

```java
public enum FileStorageType {
    IMAGE("images/"),   // 이미지 파일
    FILE("files/");     // 일반 파일 (PDF, ZIP 등)

    private final String storageRoot;
}
```

---

## UploadedFile 엔티티

```
id | key | originalName | contentType | storageType | domain | domainId
```

- `domain`: 연결된 도메인 타입 (`BLOG`, `NOTICE`, `PROJECT`, `MEMBER`)
- `domainId`: 연결된 엔티티 ID
- 도메인 엔티티 저장 시 UploadedFile 등록 여부를 재검증해 미완료 업로드 차단

---

## ContentType → 확장자 매핑

| ContentType | 확장자 |
|-------------|--------|
| `image/png` | `png` |
| `image/jpeg` | `jpg` |
| `image/gif` | `gif` |
| `image/webp` | `webp` |
| `image/svg+xml` | `svg` |
| `application/pdf` | `pdf` |
| `application/zip` | `zip` |
| `application/vnd.openxmlformats-officedocument.wordprocessingml.document` | `docx` |
| 기타 | `bin` |

---

## 관련 패키지

```
domain/file/
  entity/   UploadedFile
  service/  FileUploadService
  infra/    S3FileProvider (FileProvider 구현체)

infra/s3/
  FileProvider (interface)
  S3FileProvider (AWS SDK S3 구현)
```

---

## 적용 도메인

Blog · Project · Member 프로필 · Notice (이미지 + 일반 파일)
