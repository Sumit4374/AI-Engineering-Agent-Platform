package com.ai_engineering.ai_service.Engine;

import java.io.IOException;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import com.ai_engineering.ai_service.Engine.Prompts.PromptLoader;
import com.ai_engineering.ai_service.Engine.Prompts.PromptType;

public class AIEngineImpl implements AIEngine {

    private final PromptLoader promptLoader;
    private final ChatClient chatClient;
    public AIEngineImpl(PromptLoader promptLoader, ChatClient chatClient){
        this.promptLoader = promptLoader;
        this.chatClient = chatClient;
    }

    @Override
    public <T> T generate(PromptType promptType, Map<String, Object> variables, Class<T> responseType) throws IOException {
        String loadedText = promptLoader.load(promptType.toString());
        PromptTemplate template = new PromptTemplate(loadedText);
        String prompt = template.render(variables);
        return chatClient
        .prompt(prompt)
        .call()
        .entity(responseType);
    }    
}
