package com.resumeanalyzer.service;

import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final SkillDictionaryService skillDictionaryService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public ResumeAnalysis analyzeResume(Resume resume, String extractedText) {
        log.info("Starting analysis for resume id: {}", resume.getId());
        
        // 1. Normalize text
        String normalizedText = normalizeText(extractedText);
        
        // 2. Identify Skills and Categories
        Map<String, List<String>> dict = skillDictionaryService.getSkillsByCategory();
        
        Set<String> matchedSkills = new HashSet<>();
        Set<String> missingSkills = new HashSet<>();
        Map<String, Integer> categoryScores = new HashMap<>();
        
        List<String> strongAreas = new ArrayList<>();
        List<String> weakAreas = new ArrayList<>();

        int totalMatched = 0;
        int totalExpected = 0;

        for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
            String category = entry.getKey();
            List<String> expectedSkills = entry.getValue();
            log.debug("Analyzing category: {} with {} expected skills", category, expectedSkills.size());

            int catMatched = 0;

            for (String skill : expectedSkills) {
                // Use a more efficient way to check for word boundaries
                // Pattern.quote is safer than manual replace
                String escapedSkill = java.util.regex.Pattern.quote(skill);
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b" + escapedSkill + "\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher matcher = pattern.matcher(normalizedText);

                if (matcher.find()) {
                    matchedSkills.add(skill);
                    catMatched++;
                    log.trace("Matched skill: {}", skill);
                } else {
                    missingSkills.add(skill);
                }
            }

            int catScore = expectedSkills.isEmpty() ? 0 : (int) Math.round(((double) catMatched / expectedSkills.size()) * 100);
            categoryScores.put(category, catScore);

            if (catScore >= 50) {
                strongAreas.add(category);
            } else if (catScore < 30) {
                weakAreas.add(category);
            }

            totalMatched += catMatched;
            totalExpected += expectedSkills.size();
        }

        log.info("Analysis complete. Total matched: {}, Total expected: {}", totalMatched, totalExpected);
        
        // 3. Compute overall score
        int overallScore = totalExpected == 0 ? 0 : (int) Math.round(((double) totalMatched / totalExpected) * 100);
        log.debug("Computed raw overall score: {}", overallScore);
        // Boost score a bit for realism, otherwise it might be 15% 
        overallScore = Math.min(100, overallScore * 3); 
        log.info("Final boosted overall score: {}", overallScore);
        
        // Update category scores similarly for visual appeal if needed, or leave accurate.
        categoryScores.replaceAll((k, v) -> Math.min(100, v * 3));
        
        // 4. Generate Suggestions
        List<String> suggestions = generateSuggestions(weakAreas, missingSkills);
        
        // 5. Save Analysis
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .score(overallScore)
                .matchedSkills(new ArrayList<>(matchedSkills))
                .missingSkills(new ArrayList<>(missingSkills).subList(0, Math.min(missingSkills.size(), 10))) // Keep top 10
                .strongAreas(strongAreas)
                .weakAreas(weakAreas)
                .categoryScores(categoryScores)
                .suggestions(suggestions)
                .build();
                
        return resumeAnalysisRepository.save(analysis);
    }
    
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9+#.\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    private List<String> generateSuggestions(List<String> weakAreas, Set<String> missingSkills) {
        List<String> suggestions = new ArrayList<>();
        if (weakAreas.contains("backend")) {
            suggestions.add("Enhance your backend capabilities by adding skills like Spring Boot and Node.js.");
        }
        if (weakAreas.contains("cloud")) {
            suggestions.add("Cloud and DevOps skills are highly sought after. Consider learning Docker, AWS, or CI/CD pipelines.");
        }
        if (weakAreas.contains("database")) {
            suggestions.add("Improve your database knowledge with PostgreSQL or MongoDB.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Your resume has a great balance. Consider adding more advanced architectural skills.");
        }
        return suggestions;
    }
}
