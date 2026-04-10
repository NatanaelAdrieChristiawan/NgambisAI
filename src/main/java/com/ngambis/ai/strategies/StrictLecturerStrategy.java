package com.ngambis.ai.strategies;

import org.springframework.stereotype.Component;

/**
 * Strict Lecturer persona — uses compressed, token-efficient prompts
 * to evaluate student answers with high academic standards.
 *
 * <p><b>Phase 6 Optimization:</b> Prompt reduced from ~1800 chars to ~250 chars
 * (~85% token savings) while maintaining evaluation quality.</p>
 */
@Component
public class StrictLecturerStrategy implements PersonaStrategy {

    @Override
    public String generateEvaluationPrompt(String question, String referenceText, String studentAnswer) {
        return String.format(
                "Role: Strict Univ Lecturer. Task: Evaluate student answer against Document Context. "
                + "Be critical. Output ONLY JSON: {\"score\": 0-100, \"feedback\": \"Max 2 short sentences pointing out flaws\"}.\n\n"
                + "Q: %s\nContext: %s\nAnswer: %s",
                question, referenceText, studentAnswer
        );
    }
}
