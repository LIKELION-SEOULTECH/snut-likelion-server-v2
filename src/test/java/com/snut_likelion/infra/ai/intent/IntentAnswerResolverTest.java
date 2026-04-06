package com.snut_likelion.infra.ai.intent;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntentAnswerResolverTest {

    @TempDir
    Path tempDir;

    private IntentAnswerResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new IntentAnswerResolver();
        resolver.setResourceLoader(new DefaultResourceLoader());
    }

    // ── findAnswer() ──────────────────────────────────────────────────────

    @Test
    void findAnswer_whenIntentExists_returnsAnswer() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "지원 방법은 다음과 같습니다."}
        });

        assertThat(resolver.findAnswer("faq-apply"))
                .isPresent()
                .contains("지원 방법은 다음과 같습니다.");
    }

    @Test
    void findAnswer_whenIntentNotExists_returnsEmpty() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "지원 방법"}
        });

        assertThat(resolver.findAnswer("unknown-intent")).isEmpty();
    }

    @Test
    void findAnswer_whenIntentIsNull_returnsEmpty() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "지원 방법"}
        });

        assertThat(resolver.findAnswer(null)).isEmpty();
    }

    @Test
    void findAnswer_whenIntentIsBlank_returnsEmpty() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "지원 방법"}
        });

        assertThat(resolver.findAnswer("   ")).isEmpty();
    }

    @Test
    void findAnswer_whenIntentHasLeadingTrailingWhitespace_trimsAndFinds() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "지원 방법"}
        });

        assertThat(resolver.findAnswer("  faq-apply  "))
                .isPresent()
                .contains("지원 방법");
    }

    @Test
    void findAnswer_whenMultipleIntentsLoaded_findsCorrectAnswer() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"},
                {"intent-1", "답변 1"},
                {"intent-2", "답변 2"},
                {"intent-3", "답변 3"}
        });

        assertThat(resolver.findAnswer("intent-1")).contains("답변 1");
        assertThat(resolver.findAnswer("intent-2")).contains("답변 2");
        assertThat(resolver.findAnswer("intent-3")).contains("답변 3");
    }

    // ── load() 오류 시나리오 ───────────────────────────────────────────────

    @Test
    void load_whenFileNotFound_throwsIllegalStateException() {
        resolver.resourcePath = "classpath:ai/non-existent-file.xlsx";

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Intent-answer file not found");
    }

    @Test
    void load_whenHeadersAreMissing_throwsIllegalStateException() throws IOException {
        setResourcePath(new String[][]{
                {"wrong-col-1", "wrong-col-2"},
                {"faq-apply", "지원 방법"}
        });

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Required headers are missing");
    }

    @Test
    void load_whenIntentHeaderOnlyMissing_throwsIllegalStateException() throws IOException {
        setResourcePath(new String[][]{
                {"answer", "no-intent-header"},
                {"지원 방법", "faq-apply"}
        });

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Required headers are missing");
    }

    @Test
    void load_whenDuplicateIntentExists_throwsIllegalStateException() throws IOException {
        setResourcePath(new String[][]{
                {"intent", "answer"},
                {"faq-apply", "답변 1"},
                {"faq-apply", "답변 2"}
        });

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate intent found: faq-apply");
    }

    @Test
    void load_whenIntentCellIsEmpty_throwsIllegalStateException() throws IOException {
        setResourcePath(new String[][]{
                {"intent", "answer"},
                {"", "답변"}
        });

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Intent is empty at row index");
    }

    @Test
    void load_whenAnswerCellIsEmpty_throwsIllegalStateException() throws IOException {
        setResourcePath(new String[][]{
                {"intent", "answer"},
                {"faq-apply", ""}
        });

        assertThatThrownBy(() -> resolver.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Answer is empty at row index");
    }

    @Test
    void load_whenOnlyHeaderRowExists_loadsEmptyMap() throws IOException {
        loadWith(new String[][]{
                {"intent", "answer"}
        });

        assertThat(resolver.findAnswer("any-intent")).isEmpty();
    }

    // ── 헬퍼 ───────────────────────────────────────────────────────────────

    private void loadWith(String[][] data) throws IOException {
        setResourcePath(data);
        resolver.load();
    }

    private void setResourcePath(String[][] data) throws IOException {
        String xlsxPath = createXlsxFile(data);
        resolver.resourcePath = "file:" + xlsxPath;
    }

    private String createXlsxFile(String[][] data) throws IOException {
        Path xlsxPath = tempDir.resolve("test-intent-answer.xlsx");
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(xlsxPath.toFile())) {
            Sheet sheet = workbook.createSheet("Sheet1");
            for (int rowIdx = 0; rowIdx < data.length; rowIdx++) {
                Row row = sheet.createRow(rowIdx);
                for (int colIdx = 0; colIdx < data[rowIdx].length; colIdx++) {
                    row.createCell(colIdx).setCellValue(data[rowIdx][colIdx]);
                }
            }
            workbook.write(fos);
        }
        return xlsxPath.toAbsolutePath().toString();
    }
}
