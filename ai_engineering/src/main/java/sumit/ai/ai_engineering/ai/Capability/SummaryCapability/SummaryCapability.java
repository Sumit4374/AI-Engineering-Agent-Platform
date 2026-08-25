package sumit.ai.ai_engineering.ai.Capability.SummaryCapability;

import java.io.IOException;



import reactor.core.publisher.Flux;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeRequest;
import sumit.ai.ai_engineering.ai.dto.SummarizeDTO.SummarizeResponse;

public interface SummaryCapability {
    SummarizeResponse execute(SummarizeRequest request) throws IOException;
    Flux<String> stream(SummarizeRequest request) throws IOException;
}
