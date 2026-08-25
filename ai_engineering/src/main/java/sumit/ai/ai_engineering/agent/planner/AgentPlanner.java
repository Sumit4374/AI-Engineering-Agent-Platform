package sumit.ai.ai_engineering.agent.planner;

import sumit.ai.ai_engineering.agent.model.AgentPlan;

public interface AgentPlanner {

    AgentPlan createPlan(String goal);
}
