package com.ai_engineering.ai_service.tools.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.TimeInfo;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class TimeTool implements AiTool {

    @Tool(description = "get the current local time, date, Date&Time and Zone")
    public TimeInfo currentTime(){
        ZoneId zone = ZoneId.systemDefault();
        return new TimeInfo(
            LocalDate.now(),
            LocalTime.now(),
            LocalDateTime.now(),
            zone
        );
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
