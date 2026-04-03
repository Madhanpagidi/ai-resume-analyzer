package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AnalysisResponse;
import com.resumeanalyzer.dto.ResumeUploadResponse;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "Upload and analyze resumes")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF resume and trigger analysis")
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        return new ResponseEntity<>(resumeService.uploadAndAnalyze(file, user.getId()), HttpStatus.OK);
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Get detailed analysis of a resume by its ID")
    public ResponseEntity<AnalysisResponse> getAnalysis(@PathVariable Long id) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found for resume id: " + id));

        AnalysisResponse response = AnalysisResponse.builder()
                .resumeId(analysis.getResume().getId())
                .score(analysis.getScore())
                .matchedSkills(analysis.getMatchedSkills())
                .missingSkills(analysis.getMissingSkills())
                .suggestions(analysis.getSuggestions())
                .strongAreas(analysis.getStrongAreas())
                .weakAreas(analysis.getWeakAreas())
                .categoryScores(analysis.getCategoryScores())
                .build();

        return ResponseEntity.ok(response);
    }
}
