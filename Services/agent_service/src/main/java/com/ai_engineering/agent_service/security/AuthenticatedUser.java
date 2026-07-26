package com.ai_engineering.agent_service.security;

/**
 * The authenticated caller, derived purely from a validated JWT.
 *
 * <p>Unlike {@code auth_service}, this service has no user database — it trusts
 * the claims in a signature-verified token. {@code userId} and {@code role}
 * come from the custom claims {@code auth_service} mints; {@code username} is
 * the token subject.
 */
public record AuthenticatedUser(Long userId, String username, String role) {
}
