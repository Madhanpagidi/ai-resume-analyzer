/**
 * Frontend Configuration for AI Resume Analyzer
 * 
 * Centralized API base URLs for production and development.
 */

// Deployment Backend URL (Render)
const BASE_URL = "https://ai-resume-analyzer-cev2.onrender.com";

// API Versioned URL
const API_URL = `${BASE_URL}/api/v1`;

console.log("Config loaded. API_URL:", API_URL);
