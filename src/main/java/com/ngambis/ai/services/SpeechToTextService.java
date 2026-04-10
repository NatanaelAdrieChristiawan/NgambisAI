package com.ngambis.ai.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for Speech-to-Text transcription.
 *
 * <p><b>Architecture:</b> This service supports two transcription modes:</p>
 * <ol>
 *   <li><b>Browser-based (Web Speech API)</b> — FREE, recommended.
 *       Frontend performs transcription using the browser's built-in Web Speech API
 *       and sends the resulting text directly to the backend via the text-based endpoint.
 *       Supports Indonesian (id-ID) natively.</li>
 *   <li><b>Server-side (audio file)</b> — When audio files are sent directly,
 *       the service returns a mock transcription (unless a paid STT service is configured).
 *       This mode is kept for backward compatibility and testing.</li>
 * </ol>
 *
 * <p><b>Why Web Speech API?</b></p>
 * <ul>
 *   <li>100% FREE — no billing, no API keys, no credentials needed</li>
 *   <li>Native Indonesian (id-ID) language support</li>
 *   <li>Runs entirely in the browser (Chrome, Edge)</li>
 *   <li>Good accuracy for conversational speech</li>
 * </ul>
 */
@Service
@Slf4j
public class SpeechToTextService {

    @Value("${ai.stt.language-code:id-ID}")
    private String languageCode;

    /**
     * Validates and returns browser-transcribed text.
     * Used when the frontend sends text from Web Speech API.
     *
     * @param transcript the text transcribed by the browser's Web Speech API
     * @return validated transcript text
     */
    public String validateBrowserTranscript(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            log.warn("Received empty browser transcript — using fallback");
            return getFallbackMessage();
        }

        log.info("Browser STT received — {} characters, language: {}", transcript.length(), languageCode);
        return transcript.trim();
    }

    /**
     * Transcribes audio file to text (legacy/fallback mode).
     * Returns mock transcription since Google Cloud STT requires billing.
     * Frontend should use Web Speech API instead for free transcription.
     *
     * @param audioFile uploaded audio file (WAV, MP3, FLAC, OGG)
     * @return transcribed text (mock in free mode)
     */
    public String transcribe(MultipartFile audioFile) {
        log.warn("Server-side STT called — returning mock. Use Web Speech API on frontend for free STT.");
        return getMockTranscription();
    }

    /**
     * Returns mock transcription for audio file uploads.
     */
    private String getMockTranscription() {
        return "[Mock Transcription] Server-side audio STT membutuhkan Google Cloud billing. "
                + "Gunakan Web Speech API di browser (gratis) dengan endpoint POST /api/simulator/evaluate-text.";
    }

    /**
     * Returns fallback message when browser transcript is empty.
     */
    private String getFallbackMessage() {
        return "[Empty Transcript] Tidak ada teks yang diterima dari browser. "
                + "Pastikan mikrofon aktif dan browser mendukung Web Speech API.";
    }
}
