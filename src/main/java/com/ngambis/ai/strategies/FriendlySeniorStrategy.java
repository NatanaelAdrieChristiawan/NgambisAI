package com.ngambis.ai.strategies;

import org.springframework.stereotype.Component;

/**
 * Friendly Senior persona — uses compressed, token-efficient prompts
 * to evaluate student answers with an encouraging, analogy-rich style.
 *
 * <p><b>Phase 6 Optimization:</b> Prompt reduced from ~1600 chars to ~260 chars
 * (~83% token savings) while maintaining supportive evaluation quality.</p>
 */
@Component
public class FriendlySeniorStrategy implements PersonaStrategy {

    @Override
    public String generateEvaluationPrompt(String question, String referenceText, String studentAnswer) {
        return String.format(
                "Role: Friendly Senior Student. Task: Evaluate answer against Document Context. "
                + "Be supportive. Use simple analogies. Output ONLY JSON: {\"score\": 0-100, \"feedback\": \"Max 2 short sentences with an analogy\"}.\n\n"
                + "Q: %s\nContext: %s\nAnswer: %s",
                question, referenceText, studentAnswer
        );
    }
}
