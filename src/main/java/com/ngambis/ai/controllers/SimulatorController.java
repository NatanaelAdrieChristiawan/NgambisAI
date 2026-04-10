package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.EvaluationResponse;
import com.ngambis.ai.services.SimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for the oral exam simulation.
 * Handles receiving student answers (audio or text), triggering evaluation
 * via persona strategy and AI, and returning evaluation results.
 *
 * <p><b>Two evaluation modes:</b></p>
 * <ul>
 *   <li><b>POST /api/simulator/evaluate-text</b> — Recommended. Receives pre-transcribed
 *       text from the frontend (via browser's Web Speech API). FREE, no billing needed.</li>
 *   <li><b>POST /api/simulator/evaluate</b> — Legacy. Receives audio file for server-side
 *       transcription. Requires Google Cloud billing (currently returns mock).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
@Slf4j
public class SimulatorController {

    private final SimulatorService simulatorService;

    /**
     * Submit a text-based answer for AI evaluation (recommended).
     *
     * <p>Flow: Receive text (from Web Speech API) → Apply Persona Strategy
     * → Call AI API → Save & Return Evaluation.</p>
     *
     * <p>The frontend uses the browser's Web Speech API to transcribe the student's
     * speech to text, then sends the text here. This approach is 100% FREE.</p>
     *
     * @param sessionId  the quiz session ID
     * @param quizItemId the quiz item being answered
     * @param transcript the student's spoken answer transcribed by Web Speech API
     * @return the AI evaluation result with score and feedback
     */
    @PostMapping("/evaluate-text")
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluateTextAnswer(
            @RequestParam("sessionId") UUID sessionId,
            @RequestParam("quizItemId") UUID quizItemId,
            @RequestParam("transcript") String transcript) {

        log.info("POST /api/simulator/evaluate-text — Session: {}, QuizItem: {}, Transcript: {} chars",
                sessionId, quizItemId, transcript != null ? transcript.length() : 0);

        EvaluationResponse evaluation = simulatorService.evaluateTextAnswer(
                sessionId, quizItemId, transcript);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Answer evaluated successfully. Score: " + evaluation.getScore(),
                        evaluation));
    }

    /**
     * Submit an audio answer for AI evaluation (legacy — requires billing).
     *
     * <p>Flow: Receive audio → Transcribe (mock STT) → Apply Persona Strategy
     * → Call AI API → Save & Return Evaluation.</p>
     *
     * <p><b>Note:</b> Server-side STT requires Google Cloud billing.
     * Use /evaluate-text endpoint with Web Speech API for free transcription.</p>
     *
     * @param sessionId  the quiz session ID
     * @param quizItemId the quiz item being answered
     * @param audioFile  the student's audio recording (multipart/form-data)
     * @return the AI evaluation result with score and feedback
     */
    @PostMapping(value = "/evaluate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluateAnswer(
            @RequestParam("sessionId") UUID sessionId,
            @RequestParam("quizItemId") UUID quizItemId,
            @RequestParam("audioFile") MultipartFile audioFile) {

        log.info("POST /api/simulator/evaluate — Session: {}, QuizItem: {}, Audio: {} ({} bytes)",
                sessionId, quizItemId,
                audioFile.getOriginalFilename(), audioFile.getSize());

        EvaluationResponse evaluation = simulatorService.evaluateAnswer(
                sessionId, quizItemId, audioFile);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Answer evaluated successfully. Score: " + evaluation.getScore(),
                        evaluation));
    }
}
