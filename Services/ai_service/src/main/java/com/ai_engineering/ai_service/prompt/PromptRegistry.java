package com.ai_engineering.ai_service.prompt;

import java.io.IOException;
import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class PromptRegistry {

    private final PromptLoader promptLoader;

    public PromptRegistry(PromptLoader promptLoader){
        this.promptLoader = promptLoader;
    }
    
    public String loadPrompt(String path, Map<String, Object> variables) throws IOException{
        String loadedText = promptLoader.load(path);
        PromptTemplate template = new PromptTemplate(loadedText);
        return template.render(variables);
    }
}
