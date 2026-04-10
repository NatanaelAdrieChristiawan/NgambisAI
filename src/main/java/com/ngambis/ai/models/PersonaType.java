package com.ngambis.ai.models;

/**
 * Enum representing the type of AI persona used in an oral exam simulation.
 * Each persona generates evaluation prompts with a distinct communication style.
 */
public enum PersonaType {

    /**
     * A strict, academic persona that evaluates answers with high standards
     * and formal academic language.
     */
    STRICT_LECTURER,

    /**
     * A friendly, encouraging persona that uses relatable analogies
     * and supportive feedback to guide learning.
     */
    FRIENDLY_SENIOR
}
