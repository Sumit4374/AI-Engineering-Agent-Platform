package com.ai_engineering.ai_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AIConfiguration {
    
    @Bean
    ChatClient chatClient(ChatClient.Builder chatClient, ChatMemory chatMemory){
        return chatClient
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build()
        )
        .build();
    }
}
