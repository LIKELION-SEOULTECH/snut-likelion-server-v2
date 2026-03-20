package com.snut_likelion.infra.ai.mapping;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class IntentAnswerResolver {

    private static final String RESOURCE_PATH = "ai/intent-answer.xlsx";
    private static final String HEADER_INTENT = "intent";
    private static final String HEADER_ANSWER = "answer";

    private final DataFormatter dataFormatter = new DataFormatter();
    private Map<String, String> intentAnswerMap = Map.of();

    @PostConstruct
    void load() {
        intentAnswerMap = loadIntentAnswerMap();
    }

    public Optional<String> findAnswer(String intent) {
        if (intent == null) {
            return Optional.empty();
        }

        String normalizedIntent = intent.trim();
        if (normalizedIntent.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(intentAnswerMap.get(normalizedIntent));
    }

    private Map<String, String> loadIntentAnswerMap() {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("Intent-answer file not found: " + RESOURCE_PATH);
        }

        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalStateException("No sheet found in: " + RESOURCE_PATH);
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalStateException("Header row is missing in: " + RESOURCE_PATH);
            }

            int intentColumnIndex = findHeaderColumnIndex(headerRow, HEADER_INTENT);
            int answerColumnIndex = findHeaderColumnIndex(headerRow, HEADER_ANSWER);

            if (intentColumnIndex < 0 || answerColumnIndex < 0) {
                throw new IllegalStateException("Required headers are missing. Expected headers: intent, answer");
            }

            Map<String, String> loadedMap = new LinkedHashMap<>();
            int startRowIndex = headerRow.getRowNum() + 1;

            for (int rowIndex = startRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String intent = readCellValue(row.getCell(intentColumnIndex));
                String answer = readCellValue(row.getCell(answerColumnIndex));

                if (intent.isEmpty() && answer.isEmpty()) {
                    continue;
                }

                if (intent.isEmpty()) {
                    throw new IllegalStateException("Intent is empty at row index: " + rowIndex);
                }

                if (answer.isEmpty()) {
                    throw new IllegalStateException("Answer is empty at row index: " + rowIndex);
                }

                String previousAnswer = loadedMap.putIfAbsent(intent, answer);
                if (previousAnswer != null) {
                    throw new IllegalStateException("Duplicate intent found: " + intent);
                }
            }

            return Map.copyOf(loadedMap);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load intent-answer file: " + RESOURCE_PATH, ex);
        }
    }

    private int findHeaderColumnIndex(Row headerRow, String headerName) {
        for (Cell cell : headerRow) {
            String currentHeader = readCellValue(cell);
            if (headerName.equals(currentHeader)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private String readCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }
}
