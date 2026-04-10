package com.ngambis.ai.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the AI's evaluation of a student's answer to a specific quiz item.
 * Stores the student's audio transcript, the computed score (0-100), and detailed feedback.
 *
 * <p>Each evaluation is linked to a specific user via the quiz item's session,
 * and also directly via the user field for efficient querying of user history.</p>
 */
@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizItem quizItem;

    /**
     * Direct reference to the user who submitted this evaluation.
     * Enables efficient querying of evaluation history per user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "student_audio_transcript", columnDefinition = "TEXT")
    private String studentAudioTranscript;

    /**
     * Score from 0 to 100, as evaluated by the AI examiner.
     */
    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
