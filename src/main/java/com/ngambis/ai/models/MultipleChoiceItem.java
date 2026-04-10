package com.ngambis.ai.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * A multiple-choice quiz item with a set of options and a correct answer.
 * Extends {@link QuizItem} using SINGLE_TABLE inheritance.
 */
@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultipleChoiceItem extends QuizItem {

    /**
     * JSON-formatted string containing the answer options.
     * Example: ["Option A", "Option B", "Option C", "Option D"]
     */
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    /**
     * The correct answer for this multiple-choice question.
     */
    @Column(name = "correct_answer", length = 500)
    private String correctAnswer;
}
