package ai_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ai_service.dto.AskGuideRequest;
import ai_service.dto.AskGuideResponse;
import ai_service.entity.KnowledgeChunk;
import ai_service.entity.LlmCallLog;
import ai_service.entity.enums.LlmPurpose;
import ai_service.entity.enums.LlmStatus;
import ai_service.entity.enums.ServiceContext;
import ai_service.repository.KnowledgeChunkRepository;
import ai_service.repository.LlmCallLogRepository;
import ai_service.service.RagKnowledgeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagKnowledgeServiceImpl implements RagKnowledgeService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final LlmCallLogRepository llmCallLogRepository;

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Override
    @Transactional
    public AskGuideResponse askGuide(UUID playerId, AskGuideRequest request) {
        long startTime = System.currentTimeMillis();
        List<KnowledgeChunk> relevantChunks = new ArrayList<>();

        if (embeddingModel != null) {
            try {
                float[] embeddingVector = embeddingModel.embed(request.question());
                String vectorStr = Arrays.toString(embeddingVector);
                if (request.category() != null && !request.category().isBlank()) {
                    relevantChunks = knowledgeChunkRepository.findSimilarChunksByCategory(
                            request.category().toUpperCase(), vectorStr, 3);
                } else {
                    relevantChunks = knowledgeChunkRepository.findSimilarChunks(vectorStr, 3);
                }
            } catch (Exception ex) {
                log.warn("Embedding lookup failed: {}", ex.getMessage());
            }
        }

        if (relevantChunks.isEmpty()) {
            relevantChunks = knowledgeChunkRepository.findAll().stream().limit(3).toList();
        }

        StringBuilder contextBuilder = new StringBuilder();
        List<String> titles = new ArrayList<>();
        for (KnowledgeChunk chunk : relevantChunks) {
            titles.add(chunk.getTitle());
            contextBuilder.append("### ").append(chunk.getTitle()).append("\n")
                    .append(chunk.getContent()).append("\n\n");
        }

        String promptText = String.format("""
                Bạn là Cẩm nang Nông dân AI (FarmLife Guide).
                Hãy trả lời câu hỏi của người chơi dựa trên các tài liệu cẩm nang dưới đây:
                
                %s
                
                Câu hỏi của người chơi: %s
                Hãy trả lời ngắn gọn, thân thiện và hữu ích.
                """, contextBuilder, request.question());

        String answer;
        int promptTokens = promptText.length() / 4;
        int completionTokens = 50;

        if (chatModel != null) {
            try {
                var response = chatModel.call(new Prompt(promptText));
                answer = response.getResult().getOutput().getText();
                completionTokens = answer.length() / 4;
            } catch (Exception ex) {
                log.warn("LLM chat model call failed: {}", ex.getMessage());
                answer = "Chào bạn! Về vấn đề này: " + (relevantChunks.isEmpty() ? "Hãy tham khảo mục Trợ giúp trong nông trại của bạn." : relevantChunks.getFirst().getContent());
            }
        } else {
            answer = relevantChunks.isEmpty()
                    ? "Chào bạn! Hãy gieo hạt trên các ô đất đã cày xới, tưới nước đầy đủ mỗi ngày để cây lớn nhanh."
                    : relevantChunks.getFirst().getContent();
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        LlmCallLog logEntity = LlmCallLog.builder()
                .callId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .playerId(playerId)
                .serviceContext(ServiceContext.AI)
                .purpose(LlmPurpose.RAG_GUIDE)
                .model("qwen2.5:7b")
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .costUsd(BigDecimal.ZERO)
                .latencyMs((int) latencyMs)
                .status(LlmStatus.SUCCESS)
                .build();
        llmCallLogRepository.save(logEntity);

        return new AskGuideResponse(request.question(), answer, titles);
    }
}
