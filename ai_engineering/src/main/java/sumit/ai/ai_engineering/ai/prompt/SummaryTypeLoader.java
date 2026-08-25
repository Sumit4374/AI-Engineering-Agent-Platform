package sumit.ai.ai_engineering.ai.prompt;

import org.springframework.stereotype.Component;

@Component
public class SummaryTypeLoader {
    public String loadSummaryType(SummaryType type){
        return switch (type) {

            case GENERAL -> "summary/GENERAL.prompt";

            case TECHNICAL -> "summary/TECHNICAL.prompt";

            case RESEARCH -> "summary/RESEARCH.prompt";

            case MEETING -> "summary/MEETING.prompt";

            case EXECUTIVE -> "summary/EXECUTIVE.prompt";
        };
    }
}
