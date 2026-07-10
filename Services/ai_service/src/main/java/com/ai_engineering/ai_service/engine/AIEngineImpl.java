package com.ai_engineering.ai_service.engine;

import java.io.IOException;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.ai_engineering.ai_service.prompt.PromptLoader;
import com.ai_engineering.ai_service.prompt.PromptRegistry;
import com.ai_engineering.ai_service.prompt.PromptType;

@Service
public class AIEngineImpl implements AIEngine {

    private final PromptLoader promptLoader;
    private final ChatClient chatClient;
    private final PromptRegistry promptRegistry;
    public AIEngineImpl(PromptLoader promptLoader, ChatClient chatClient, PromptRegistry promptRegistry){
        this.promptLoader = promptLoader;
        this.chatClient = chatClient;
        this.promptRegistry = promptRegistry;
    }

    @Override
    public <T> T generateStructure(PromptType promptType, Map<String, Object> variables, Class<T> responseType) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType.toString(), variables);
        return chatClient
        .prompt(prompt)
        .call()
        .entity(responseType);
    }

    @Override
    public String generate(PromptType promptType, Map<String, Object> variables) throws IOException {
        String prompt = promptRegistry.loadPrompt(promptType.toString(), variables);
        System.out.println(prompt);
        return chatClient
        .prompt(prompt)
        .call()
        .content();
    }    
}
