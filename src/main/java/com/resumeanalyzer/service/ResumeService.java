package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ResumeUploadResponse;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.FileProcessingException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AnalysisService analysisService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ResumeUploadResponse uploadAndAnalyze(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        if (file.isEmpty() || originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new FileProcessingException("Invalid file format. Only PDF files are allowed.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            // 1. Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Save file with UUID
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            // 3. Save Resume record
            Resume resume = Resume.builder()
                    .user(user)
                    .fileName(originalFilename)
                    .filePath(filePath.toString())
                    .build();
            resume = resumeRepository.save(resume);

            // 4. Extract text using PDFBox
            String extractedText = extractTextFromPdf(filePath.toFile());

            // 5. Analyze text
            ResumeAnalysis analysis = analysisService.analyzeResume(resume, extractedText);

            log.info("Successfully analyzed resume {} for user {}", resume.getId(), user.getEmail());

            return ResumeUploadResponse.builder()
                    .resumeId(resume.getId())
                    .score(analysis.getScore())
                    .matchedSkills(analysis.getMatchedSkills())
                    .message("Resume uploaded and analyzed successfully.")
                    .build();

        } catch (IOException e) {
            log.error("Failed to process file: {}", e.getMessage());
            throw new FileProcessingException("Failed to store or read file. " + e.getMessage());
        }
    }

    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
