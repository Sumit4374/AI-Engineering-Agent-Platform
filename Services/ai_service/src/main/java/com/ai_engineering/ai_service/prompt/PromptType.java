package com.ai_engineering.ai_service.prompt;

public enum PromptType {
    CHAT("CHAT.prompt"),
    CODE_REVIEW("CODE_REVIEW.prompt"),
    DEBUG(""),
    DOCUMENTATION(""),
    ARCHITECTURE(""),
    SUMMARIZATION(""),
    EXPLAIN("EXPLAIN.prompt"),;
    
    private final String fileName;

    PromptType(String fileName) {
        this.fileName = fileName;
    }


    public String getFileName(){
        return fileName;
    }
}
