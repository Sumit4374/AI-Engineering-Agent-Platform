package com.ai_engineering.ai_service.Capability.SummaryCapability;

import java.io.IOException;

import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeRequest;
import com.ai_engineering.ai_service.dto.SummarizeDTO.SummarizeResponse;

import reactor.core.publisher.Flux;

public interface SummaryCapability {
    SummarizeResponse execute(SummarizeRequest request) throws IOException;
    Flux<String> stream(SummarizeRequest request) throws IOException;
}
