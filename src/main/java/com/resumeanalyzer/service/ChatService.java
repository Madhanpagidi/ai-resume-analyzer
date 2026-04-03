package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ChatRequest;
import com.resumeanalyzer.dto.ChatResponse;
import com.resumeanalyzer.entity.ResumeAnalysis;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public ChatResponse handleChat(ChatRequest request, Long userId) {
        log.info("Received chat message from user {}", userId);
        String message = request.getMessage().toLowerCase();
        
        // If context (resumeId) is provided, fetch latest analysis
        ResumeAnalysis analysis = null;
        if (request.getResumeId() != null) {
            analysis = resumeAnalysisRepository.findByResumeId(request.getResumeId()).orElse(null);
        }

        String reply;

        if (message.contains("skills") || message.contains("missing")) {
            if (analysis != null && analysis.getMissingSkills() != null && !analysis.getMissingSkills().isEmpty()) {
                reply = "Based on your resume analysis, you are missing: " + String.join(", ", analysis.getMissingSkills()) + 
                        ". Adding these will improve your chances.";
            } else {
                reply = "I don't have your resume analysis yet. Please upload a resume first to find out missing skills.";
            }
        } 
        else if (message.contains("improve") || message.contains("suggestions")) {
             if (analysis != null && analysis.getSuggestions() != null && !analysis.getSuggestions().isEmpty()) {
                reply = "Here are some suggestions to improve: " + String.join(" ", analysis.getSuggestions());
            } else {
                reply = "Upload your resume first, and I'll give you specific suggestions to improve it.";
            }
        }
        else if (message.contains("score")) {
             if (analysis != null) {
                reply = "Your resume score is " + analysis.getScore() + "/100. " +
                        "This is based on how well your skills match industry standard roles in " + 
                        String.join(", ", analysis.getCategoryScores().keySet()) + ".";
            } else {
                reply = "I cannot see a score right now. Have you uploaded a resume?";
            }
        }
        else if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            reply = "Hello! I am your AI Resume Assistant. You can ask me about your resume score, missing skills, or how to improve.";
        }
        else {
            reply = "I'm a simple AI assistant. I can help answer questions about your 'skills', 'score', or how to 'improve'. Try asking me one of those!";
        }

        return new ChatResponse(reply);
    }
}
