package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.prompt.PromptLoader;
import com.ai_engineering.ai_service.prompt.PromptRegistry;

@Service
public class AIEngineImpl implements AIEngine {

    private final ChatClient chatClient;
    private final PromptRegistry promptRegistry;
    public AIEngineImpl(PromptLoader promptLoader, ChatClient chatClient, PromptRegistry promptRegistry){
        this.chatClient = chatClient;
        this.promptRegistry = promptRegistry;
    }

    @Override
    public <T> T generateStructure(String promptType, Map<String, Object> variables, Class<T> responseType) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        return chatClient
        .prompt(prompt)
        .call()
        .entity(responseType);
    }

    @Override
    public String generate(String promptType, Map<String, Object> variables) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType, variables);
        System.out.println(prompt);
        return chatClient
        .prompt(prompt)
        .call()
        .content();
    }    
}
