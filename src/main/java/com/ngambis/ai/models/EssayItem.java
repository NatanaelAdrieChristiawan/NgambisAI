package com.ngambis.ai.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * An essay-type quiz item that requires a free-form written or spoken answer.
 * Extends {@link QuizItem} using SINGLE_TABLE inheritance.
 *
 * <p>Essay items are typically evaluated by the AI using the selected
 * {@link com.ngambis.ai.strategies.PersonaStrategy}.</p>
 */
@Entity
@DiscriminatorValue("ESSAY")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EssayItem extends QuizItem {

    // Essay items use the inherited questionText and referenceText fields.
    // No additional fields required — the evaluation is performed by the AI
    // based on the student's spoken/written answer compared to the referenceText.
}
