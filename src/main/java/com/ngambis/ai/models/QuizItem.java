package com.ngambis.ai.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for quiz items using SINGLE_TABLE inheritance.
 * Subclasses: {@link MultipleChoiceItem} and {@link EssayItem}.
 *
 * <p>Demonstrates OOP Inheritance with JPA — all quiz item types are stored
 * in a single database table with a discriminator column to differentiate types.</p>
 */
@Entity
@Table(name = "quiz_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class QuizItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizSession session;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "reference_text", columnDefinition = "TEXT")
    private String referenceText;

    @OneToMany(mappedBy = "quizItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Evaluation> evaluations = new ArrayList<>();
}
