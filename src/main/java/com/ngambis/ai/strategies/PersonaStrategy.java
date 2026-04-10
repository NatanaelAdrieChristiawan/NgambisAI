package com.ngambis.ai.strategies;

/**
 * Strategy interface for generating AI evaluation prompts with different personas.
 *
 * <p>Implements the <b>Strategy Pattern</b> (GoF Design Pattern) to allow the
 * oral exam simulator to dynamically switch between different evaluation styles
 * at runtime without modifying the core evaluation logic.</p>
 *
 * @see StrictLecturerStrategy
 * @see FriendlySeniorStrategy
 */
public interface PersonaStrategy {

    /**
     * Generates a system prompt for the AI model to evaluate a student's answer
     * using this persona's communication style.
     *
     * @param question      the original exam question
     * @param referenceText the reference material from the PDF document
     * @param studentAnswer the student's transcribed spoken answer
     * @return a formatted prompt string for the AI model
     */
    String generateEvaluationPrompt(String question, String referenceText, String studentAnswer);
}
