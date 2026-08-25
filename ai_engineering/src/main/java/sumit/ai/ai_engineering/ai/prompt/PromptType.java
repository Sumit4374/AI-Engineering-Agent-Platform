package sumit.ai.ai_engineering.ai.prompt;

public enum PromptType {
    CHAT("CHAT.prompt"),
    CODE_REVIEW("CODE_REVIEW.prompt"),
    DEBUG("DEBUG.prompt"),
    DOCUMENTATION("DOCUMENTATION.prompt"),
    ARCHITECTURE("ARCHITECTURE.prompt"),
    SUMMARIZATION("SUMMARIZATION.prompt"),
    EXPLAIN("EXPLAIN.prompt"),
    RAG("RAG.prompt"),
    AGENT_PLANNING("AGENT_PLANNING.prompt");
    
    private final String fileName;

    PromptType(String fileName) {
        this.fileName = fileName;
    }


    public String getFileName(){
        return fileName;
    }
}
