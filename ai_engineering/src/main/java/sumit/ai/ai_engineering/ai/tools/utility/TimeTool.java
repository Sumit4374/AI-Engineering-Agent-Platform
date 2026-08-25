package sumit.ai.ai_engineering.ai.tools.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.ai.tools.AiTool;
import sumit.ai.ai_engineering.ai.tools.model.TimeInfo;
import sumit.ai.ai_engineering.ai.tools.model.ToolsCategory;



@Component
public class TimeTool implements AiTool {

    @Tool(description = "get the current local time, date, Date&Time and Zone")
    public TimeInfo now(){
        ZoneId zone = ZoneId.systemDefault();
        return new TimeInfo(
            LocalDate.now(),
            LocalTime.now(),
            LocalDateTime.now(),
            zone
        );
    }

    @Tool(description = "get the current local time, date, Date&Time for the requested time Zone")
    public TimeInfo timezone(
        @ToolParam(description = "input ZoneId for to fetch the local time, date, date&time of the zoneId") ZoneId zoneId){
        return new TimeInfo(
            LocalDate.now(zoneId),
            LocalTime.now(zoneId),
            LocalDateTime.now(zoneId),
            zoneId
        );
    }



    @Override
    public ToolsCategory category() {
        return ToolsCategory.UTILITY;
    }
}
