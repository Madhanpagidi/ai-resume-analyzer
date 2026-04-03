package com.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private Long resumeId;
    private Integer score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> suggestions;
    private List<String> strongAreas;
    private List<String> weakAreas;
    private Map<String, Integer> categoryScores;
}
