package sumit.ai.ai_engineering.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import sumit.ai.ai_engineering.agent.api.AgentController;
import sumit.ai.ai_engineering.ai.controller.AIController;
import sumit.ai.ai_engineering.conversation.api.ConversationController;
import sumit.ai.ai_engineering.mcp.api.McpController;
import sumit.ai.ai_engineering.rag.api.DocumentController;
import sumit.ai.ai_engineering.rag.api.RagController;
import sumit.ai.ai_engineering.user.Controller.AuthController;

/**
 * Architectural compliance test for the Modular Monolith.
 * Enforces that controllers remain thin and never directly inject Spring Data repositories,
 * preserving clean layered boundaries (Controller -> Service -> Domain/Repository).
 */
class ModularArchitectureTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            AuthController.class,
            AIController.class,
            ConversationController.class,
            DocumentController.class,
            RagController.class,
            McpController.class,
            AgentController.class
    );

    @Test
    void controllers_mustBeAnnotatedWithRestController() {
        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .as("Controller %s should be annotated with @RestController", controller.getSimpleName())
                    .isTrue();
        }
    }

    @Test
    void controllers_mustNotDirectlyInjectRepositories() {
        for (Class<?> controller : CONTROLLERS) {
            for (Field field : controller.getDeclaredFields()) {
                assertThat(Repository.class.isAssignableFrom(field.getType()))
                        .as("Controller %s must NOT directly inject Repository %s. Use Service layer.",
                                controller.getSimpleName(), field.getType().getSimpleName())
                        .isFalse();

                assertThat(field.getType().getSimpleName().endsWith("Repository"))
                        .as("Controller %s must NOT contain repository field %s.",
                                controller.getSimpleName(), field.getName())
                        .isFalse();
            }
        }
    }
}
