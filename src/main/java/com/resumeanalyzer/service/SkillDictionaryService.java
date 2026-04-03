package com.resumeanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkillDictionaryService {

    @Getter
    private Map<String, List<String>> skillsByCategory;

    @PostConstruct
    public void init() {
        skillsByCategory = new HashMap<>();

        skillsByCategory.put("backend", Arrays.asList(
                "java", "spring boot", "spring mvc", "hibernate", "jpa", "microservices", "rest api", "nodejs", "python", "django", "c#", ".net"
        ));

        skillsByCategory.put("frontend", Arrays.asList(
                "html", "html5", "css", "css3", "javascript", "react", "angular", "vue", "typescript", "bootstrap", "tailwind"
        ));

        skillsByCategory.put("database", Arrays.asList(
                "postgresql", "mysql", "mongodb", "oracle", "sql", "nosql", "redis", "elasticsearch"
        ));

        skillsByCategory.put("cloud", Arrays.asList(
                "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "cicd", "terraform", "linux"
        ));

        skillsByCategory.put("soft", Arrays.asList(
                "communication", "leadership", "teamwork", "problem solving", "agile", "scrum"
        ));
    }

    public List<String> getAllSkillsFlat() {
        return skillsByCategory.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
