package com.ai_engineering.ai_service.Engine.Prompts;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLoader {
    
    public String load(String filename) throws IOException{
        ClassPathResource resource = new ClassPathResource("Prompts/"+filename);
        return new String(resource.getInputStream().readAllBytes());
    }
}
