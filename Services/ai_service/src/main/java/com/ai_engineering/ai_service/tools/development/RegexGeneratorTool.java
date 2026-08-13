package com.ai_engineering.ai_service.tools.development;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_engineering.ai_service.tools.AiTool;
import com.ai_engineering.ai_service.tools.model.ToolsCategory;

@Component
public class RegexGeneratorTool implements AiTool {

    @Tool(description = "Generate a regular expression to validate standard email addresses (local-part@domain)")
    public String emailRegex(){
        return "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    }

    @Tool(description = "Generate a regular expression to validate 10-digit phone numbers (digits only, no leading zero)")
    public String phoneRegex(){
        return "^[1-9]{10}$";
    }

    @Tool(description = "check if the provided regular expression is valid")
    public boolean isvalid(
        @ToolParam(description = "The regular expression to validate") String regex
    ){
        try {
            Pattern.compile(regex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(description = "check if the provided input string matches the provided regular expression")
    public boolean matches(
        @ToolParam(description = "The regular expression to use for matching") String regex,
        @ToolParam(description = "The input string to test against the regular expression") String input
    ){
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(input).matches();
        } catch (Exception e) {
            return false;
        }
    }    

    @Tool(description = "Find all matches of the provided regular expression in the input string")
    public List<String> matchers(
        @ToolParam(description = "The regular expression to use for matching") String regex,
        @ToolParam(description = "The input string to test against the regular expression") String input
    ){
        try {
            Pattern pattern = Pattern.compile(regex);
            var matcher = pattern.matcher(input);
            List<String> matches = new java.util.ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            return matches;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Tool(description = "Explain the purpose and usage of a regular expression")
    public String explain(
        @ToolParam(description = "The regular expression to explain") String regex
    ){
        // Implementation for explaining the regex
        return "This is a simple explanation for the regex: " + regex;
    }

    @Override
    public ToolsCategory category() {
        return ToolsCategory.DEVELOPMENT;
    }
}
