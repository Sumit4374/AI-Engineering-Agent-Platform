package com.ai_engineering.ai_service.prompt;

public enum PromptType {
    CHAT("CHAT.prompt"),
    CODE_REVIEW("CODE_REVIEW.prompt"),
    DEBUG("DEBUG.prompt"),
    DOCUMENTATION("DOCUMENTATION.prompt"),
    ARCHITECTURE("ARCHITECTURE.prompt"),
    SUMMARIZATION("SUMMARIZATION.prompt"),
    EXPLAIN("EXPLAIN.prompt"),
    REQUIREMENT_ANALYSIS("REQUIREMENT_ANALYSIS.prompt"),
    API_EXPLANATION("API_EXPLANATION.prompt");
    
    private final String fileName;

    PromptType(String fileName) {
        this.fileName = fileName;
    }


    public String getFileName(){
        return fileName;
    }
}
