package com.ai_engineering.ai_service.tools.utility;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {
    
    @Tool(description = "Adds two double numbers together and returns the sum")
    public double add(
        @ToolParam(description = "number one in datatype double") double a,
        @ToolParam(description = "number two in datatype double") double b
    ){
        return a+b;
    }

    @Tool(description = "Subtract two double numbers together and returns the difference")
    public double subtract(
        @ToolParam(description = "number one in datatype double") double a,
        @ToolParam(description = "number two in datatype double") double b
    ){
        return a-b;
    }

    @Tool(description = "Multiply two double numbers together and return the product")
    public double multiply(
        @ToolParam(description = "number one in datatype double") double a,
        @ToolParam(description = "number two in datatype double") double b
    ){
        return a*b;
    }

    @Tool(description = "Divide two double number togethe and return the Quotient in double format")
    public double divide(
        @ToolParam(description = "number one in datatype double") double a,
        @ToolParam(description = "number two in datatype double") double b
    ){
        if(b == 0){
            throw new IllegalArgumentException("Division By Zero");
        }
        return a/b;
    }
}
