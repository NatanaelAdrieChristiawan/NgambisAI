package com.ngambis.ai.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngambis.ai.dtos.response.AiEvaluationResponse;
import com.ngambis.ai.dtos.response.GeneratedQuizItemDto;
import com.ngambis.ai.exceptions.AiServiceException;
import com.ngambis.ai.models.Evaluation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for integrating with external AI APIs (Gemini)
 * and Speech-to-Text services for the oral exam simulator.
 *
 * <p><b>Optimizations Implemented:</b></p>
 * <ul>
 *   <li>Context Truncation — document text limited to relevant 2000-char window</li>
 *   <li>Sliding Window — only last 3 evaluations included as chat history</li>
 *   <li>Metrics tracking for AI calls and performance monitoring</li>
 *   <li>Configurable parameters from application.yml</li>
 *   <li>Real Speech-to-Text integration support</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiIntegrationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final SpeechToTextService speechToTextService;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${ai.gemini.model}")
    private String geminiModel;

    @Value("${ai.context.max-length:2000}")
    private int maxContextLength;

    @Value("${ai.context.window-size:2000}")
    private int keywordWindowSize;

    @Value("${ai.context.step-size:200}")
    private int stepSize;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    /**
     * Transcribes audio file to text using Speech-to-Text service.
     *
     * @param audioFile the uploaded audio file from the student
     * @return the transcribed text of the student's spoken answer
     */
    public String transcribeAudio(MultipartFile audioFile) {
        log.info("Transcribing audio file: {} (size: {} bytes)",
                audioFile.getOriginalFilename(), audioFile.getSize());

        incrementCounter("ai.stt.calls");

        return speechToTextService.transcribe(audioFile);
    }

    /**
     * Validates and processes browser-transcribed text from Web Speech API.
     * This is the FREE alternative to server-side audio transcription.
     *
     * @param transcript the text transcribed by the browser's Web Speech API
     * @return validated transcript text
     */
    public String validateBrowserTranscript(String transcript) {
        log.info("Validating browser transcript: {} chars", transcript != null ? transcript.length() : 0);

        incrementCounter("ai.stt.browser.calls");

        return speechToTextService.validateBrowserTranscript(transcript);
    }

    /**
     * Sends evaluation prompt to Gemini API with sliding window chat history.
     *
     * @param prompt          the evaluation prompt from PersonaStrategy
     * @param recentHistory   last 3 evaluations (sliding window)
     * @return AI evaluation with score and feedback
     */
    public AiEvaluationResponse evaluateAnswer(String prompt, List<Evaluation> recentHistory) {
        log.debug("Evaluating with Gemini (model: {}, prompt: {} chars, history: {} items)",
                geminiModel, prompt.length(), recentHistory.size());

        incrementCounter("ai.gemini.calls");

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            AiEvaluationResponse response = performEvaluation(prompt, recentHistory);
            sample.stop(Timer.builder("ai.gemini.duration")
                    .description("Gemini API call duration")
                    .register(meterRegistry));
            return response;
        } catch (Exception e) {
            incrementCounter("ai.gemini.errors");
            throw e;
        }
    }

    /**
     * Overloaded method for backward compatibility — no history.
     */
    public AiEvaluationResponse evaluateAnswer(String prompt) {
        return evaluateAnswer(prompt, Collections.emptyList());
    }

    /**
     * Generates quiz/flashcard items from document text using Gemini AI.
     *
     * @param documentText  combined extracted text from document(s)
     * @param questionCount number of questions to generate
     * @param itemType      "MULTIPLE_CHOICE" or "ESSAY"
     * @return list of generated quiz items
     */
    public List<GeneratedQuizItemDto> generateQuizItems(String documentText, int questionCount, String itemType) {
        log.info("Generating {} {} questions via Gemini", questionCount, itemType);
        incrementCounter("ai.gemini.generate.calls");

        String context = documentText.length() > 8000 ? documentText.substring(0, 8000) : documentText;

        String prompt;
        Map<String, Object> schema;

        if ("MULTIPLE_CHOICE".equals(itemType)) {
            prompt = String.format(
                "Kamu adalah pembuat soal ujian profesional. Berdasarkan teks dokumen berikut, " +
                "buat %d soal pilihan ganda dalam Bahasa Indonesia.\n\n" +
                "Teks Dokumen:\n%s\n\n" +
                "Buat soal yang menguji pemahaman mendalam, bukan hafalan. " +
                "Setiap soal harus memiliki 4 opsi (A, B, C, D) dan 1 jawaban benar.",
                questionCount, context
            );
            schema = Map.of(
                "type", "ARRAY",
                "items", Map.of(
                    "type", "OBJECT",
                    "properties", Map.of(
                        "questionText", Map.of("type", "STRING"),
                        "options", Map.of(
                            "type", "ARRAY",
                            "items", Map.of("type", "STRING")
                        ),
                        "correctAnswer", Map.of("type", "STRING"),
                        "referenceText", Map.of("type", "STRING")
                    ),
                    "required", List.of("questionText", "options", "correctAnswer", "referenceText")
                )
            );
        } else {
            prompt = String.format(
                "Kamu adalah pembuat soal ujian profesional. Berdasarkan teks dokumen berikut, " +
                "buat %d soal essay dalam Bahasa Indonesia.\n\n" +
                "Teks Dokumen:\n%s\n\n" +
                "Buat soal yang mendorong analisis dan pemahaman konsep mendalam.",
                questionCount, context
            );
            schema = Map.of(
                "type", "ARRAY",
                "items", Map.of(
                    "type", "OBJECT",
                    "properties", Map.of(
                        "questionText", Map.of("type", "STRING"),
                        "referenceText", Map.of("type", "STRING")
                    ),
                    "required", List.of("questionText", "referenceText")
                )
            );
        }

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            )),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 4096,
                "responseMimeType", "application/json",
                "responseSchema", schema
            )
        );

        try {
            String responseBody = webClient.post()
                .uri("/models/{model}:generateContent?key={apiKey}", geminiModel, geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();

            return parseGeneratedItems(responseBody, itemType);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException("AI question generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses Gemini JSON array response into GeneratedQuizItemDto list.
     */
    private List<GeneratedQuizItemDto> parseGeneratedItems(String responseBody, String itemType) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").get(0).path("content").path("parts");

            String generatedText = null;
            for (JsonNode part : parts) {
                if (part.has("thought") && part.get("thought").asBoolean()) continue;
                if (part.has("text")) generatedText = part.get("text").asText();
            }

            if (generatedText == null || generatedText.isBlank()) {
                throw new AiServiceException("Gemini returned empty quiz generation response");
            }

            generatedText = generatedText.trim()
                .replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();

            JsonNode itemsNode = objectMapper.readTree(generatedText);
            List<GeneratedQuizItemDto> result = new ArrayList<>();

            for (JsonNode item : itemsNode) {
                GeneratedQuizItemDto dto = new GeneratedQuizItemDto();
                dto.setItemType(itemType);
                dto.setQuestionText(item.path("questionText").asText());
                dto.setReferenceText(item.path("referenceText").asText(""));

                if ("MULTIPLE_CHOICE".equals(itemType) && item.has("options")) {
                    List<String> opts = new ArrayList<>();
                    item.path("options").forEach(o -> opts.add(o.asText()));
                    dto.setOptions(objectMapper.writeValueAsString(opts));
                    dto.setCorrectAnswer(item.path("correctAnswer").asText());
                }
                result.add(dto);
            }

            log.info("Generated {} quiz items", result.size());
            return result;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse generated quiz items", e);
            throw new AiServiceException("Failed to parse AI quiz generation response: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a chat message to Gemini AI, preserving conversation history.
     */
    public String chatMessage(String prompt, List<Map<String, Object>> chatHistory) {
        log.info("Sending chat message to Gemini, history size: {}", chatHistory.size());
        incrementCounter("ai.gemini.chat.calls");

        List<Map<String, Object>> contents = new ArrayList<>(chatHistory);
        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", prompt))
        ));

        Map<String, Object> requestBody = Map.of(
            "contents", contents,
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 2048
            )
        );

        try {
            String responseBody = webClient.post()
                .uri("/models/{model}:generateContent?key={apiKey}", geminiModel, geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();

            return parseSimpleGeminiResponse(responseBody);
        } catch (WebClientResponseException e) {
            log.error("Gemini API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException("AI chat failed: " + e.getMessage(), e);
        }
    }

    private String parseSimpleGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").get(0).path("content").path("parts");

            String generatedText = null;
            for (JsonNode part : parts) {
                if (part.has("thought") && part.get("thought").asBoolean()) continue;
                if (part.has("text")) generatedText = part.get("text").asText();
            }

            if (generatedText == null || generatedText.isBlank()) {
                throw new AiServiceException("Gemini returned empty text");
            }
            return generatedText.trim();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response", e);
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the actual AI evaluation call.
     */
    private AiEvaluationResponse performEvaluation(String prompt, List<Evaluation> recentHistory) {
        try {
            List<Map<String, Object>> contents = buildContentsWithHistory(prompt, recentHistory);

            Map<String, Object> requestBody = Map.of(
                    "contents", contents,
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 2048,
                            "responseMimeType", "application/json",
                            "responseSchema", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "score", Map.of("type", "INTEGER"),
                                            "feedback", Map.of("type", "STRING")
                                    ),
                                    "required", List.of("score", "feedback")
                            )
                    )
            );

            String responseBody = webClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", geminiModel, geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            log.debug("Received response from Gemini API");
            return parseGeminiResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException("AI API failed: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof AiServiceException) throw (AiServiceException) e;
            log.error("Failed to communicate with Gemini API", e);
            throw new AiServiceException("AI evaluation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Builds multi-turn conversation with sliding window history.
     */
    private List<Map<String, Object>> buildContentsWithHistory(String currentPrompt, List<Evaluation> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add chat history (max 3 evaluations)
        for (Evaluation eval : history) {
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text",
                            "Previous answer: " + truncateText(eval.getStudentAudioTranscript(), 300)))
            ));
            contents.add(Map.of(
                    "role", "model",
                    "parts", List.of(Map.of("text",
                            String.format("{\"score\":%d,\"feedback\":\"%s\"}",
                                    eval.getScore() != null ? eval.getScore() : 0,
                                    truncateText(eval.getFeedback() != null ? eval.getFeedback() : "", 200))))
            ));
        }

        // Add current prompt
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", currentPrompt))
        ));

        log.debug("Built {} content turns (including {} history pairs)", contents.size(), history.size());
        return contents;
    }

    /**
     * Context Truncation with Keyword Matching (Lightweight RAG).
     * Extracts a 2000-char relevant window from full document text.
     *
     * @param fullText      complete document text
     * @param studentAnswer student's transcribed answer
     * @return truncated relevant context (max 2000 chars)
     */
    public String extractRelevantContext(String fullText, String studentAnswer) {
        if (fullText == null || fullText.isEmpty()) {
            return "";
        }

        if (fullText.length() <= maxContextLength) {
            return fullText;
        }

        log.debug("Context truncation: {} → {} chars", fullText.length(), maxContextLength);

        Set<String> keywords = extractKeywords(studentAnswer);
        if (keywords.isEmpty()) {
            log.debug("No keywords — using first {} chars", maxContextLength);
            return fullText.substring(0, maxContextLength);
        }

        // Slide window to find best matching section
        int bestStart = 0;
        int bestScore = 0;

        for (int start = 0; start <= fullText.length() - keywordWindowSize; start += stepSize) {
            int end = Math.min(start + keywordWindowSize, fullText.length());
            String window = fullText.substring(start, end).toLowerCase();

            int score = (int) keywords.stream()
                    .filter(window::contains)
                    .count();

            if (score > bestScore) {
                bestScore = score;
                bestStart = start;
            }
        }

        if (bestScore == 0) {
            log.debug("No keyword matches — fallback to first {} chars", maxContextLength);
            return fullText.substring(0, maxContextLength);
        }

        int end = Math.min(bestStart + keywordWindowSize, fullText.length());
        log.info("Selected window [{}-{}] with {}/{} keyword matches",
                bestStart, end, bestScore, keywords.size());

        return fullText.substring(bestStart, end);
    }

    /**
     * Extracts meaningful keywords from text (filter stopwords, min 3 chars).
     */
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }

        Set<String> stopWords = Set.of(
                "yang", "dan", "atau", "dari", "untuk", "dengan", "adalah", "pada",
                "dalam", "ini", "itu", "the", "and", "for", "with", "this", "that",
                "was", "are", "been", "have", "has", "will", "can", "not", "but",
                "mock", "transcription", "placeholder", "spoken", "answer"
        );

        return Arrays.stream(text.toLowerCase().split("[\\s\\p{Punct}]+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }

    /**
     * Safely truncates text to max length.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    /**
     * Parses Gemini API JSON response.
     * Supports thinking models (gemini-2.5-flash) with thought parts.
     */
    private AiEvaluationResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isEmpty() || !candidates.isArray()) {
                throw new AiServiceException("Gemini API returned no candidates");
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isEmpty() || !parts.isArray()) {
                throw new AiServiceException("Gemini API returned no parts");
            }

            // Find last non-thought text part (for thinking models)
            String generatedText = null;
            for (JsonNode part : parts) {
                if (part.has("thought") && part.get("thought").asBoolean()) {
                    continue;
                }
                if (part.has("text")) {
                    generatedText = part.get("text").asText();
                }
            }

            if (generatedText == null || generatedText.isBlank()) {
                throw new AiServiceException("Gemini API returned empty text");
            }

            // Clean markdown code fences
            generatedText = generatedText.trim();
            if (generatedText.startsWith("```json")) {
                generatedText = generatedText.substring(7);
            }
            if (generatedText.startsWith("```")) {
                generatedText = generatedText.substring(3);
            }
            if (generatedText.endsWith("```")) {
                generatedText = generatedText.substring(0, generatedText.length() - 3);
            }
            generatedText = generatedText.trim();

            AiEvaluationResponse evaluation = objectMapper.readValue(
                    generatedText, AiEvaluationResponse.class);

            log.info("AI evaluation parsed — Score: {}", evaluation.getScore());
            return evaluation;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response", e);
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    /**
     * Increments a Micrometer counter metric.
     */
    private void incrementCounter(String counterName) {
        Counter.builder(counterName)
                .description("Count of " + counterName)
                .register(meterRegistry)
                .increment();
    }
}
