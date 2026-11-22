package ai_service.service.impl;

import exception.BusinessException;
import exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ai_service.dto.NpcTalkRequest;
import ai_service.dto.NpcTalkResponse;
import ai_service.entity.LlmCallLog;
import ai_service.entity.NpcAgent;
import ai_service.entity.NpcPlayerMemory;
import ai_service.entity.enums.FsmState;
import ai_service.entity.enums.LlmPurpose;
import ai_service.entity.enums.LlmStatus;
import ai_service.entity.enums.Relationship;
import ai_service.entity.enums.ServiceContext;
import ai_service.repository.LlmCallLogRepository;
import ai_service.repository.NpcAgentRepository;
import ai_service.repository.NpcPlayerMemoryRepository;
import ai_service.service.AiOutboxService;
import ai_service.service.NpcAgentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NpcAgentServiceImpl implements NpcAgentService {

    private final NpcAgentRepository npcAgentRepository;
    private final NpcPlayerMemoryRepository memoryRepository;
    private final LlmCallLogRepository llmCallLogRepository;
    private final AiOutboxService outboxService;

    @Autowired(required = false)
    private ChatModel chatModel;

    @Override
    @Transactional
    public NpcTalkResponse talkToNpc(UUID playerId, String npcCode, NpcTalkRequest request) {
        NpcAgent npc = npcAgentRepository.findByNpcCode(npcCode)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "NPC not found: " + npcCode));

        NpcPlayerMemory memory = memoryRepository.findByNpcCodeAndPlayerId(npcCode, playerId)
                .orElseGet(() -> NpcPlayerMemory.builder()
                        .npcCode(npcCode)
                        .playerId(playerId)
                        .affectionScore(0)
                        .relationship(Relationship.STRANGER)
                        .memorySummary("Mới gặp gỡ lần đầu tại thung lũng FarmLife.")
                        .summaryUpdatedAt(Instant.now())
                        .lastInteracted(Instant.now())
                        .version(0L)
                        .build());

        String prompt = String.format("""
                %s
                
                Ký ức của bạn về người chơi này:
                %s
                Mức độ thân thiết: %d/1000 (%s).
                
                Người chơi nói: "%s"
                Hãy trả lời đúng tính cách của bạn (dưới 3 câu).
                """, npc.getPersonaPrompt(), memory.getMemorySummary(),
                memory.getAffectionScore(), memory.getRelationship().name(), request.message());

        String reply;
        long startTime = System.currentTimeMillis();
        int promptTokens = prompt.length() / 4;
        int completionTokens = 30;

        if (chatModel != null) {
            try {
                var response = chatModel.call(new Prompt(prompt));
                reply = response.getResult().getOutput().getText();
                completionTokens = reply.length() / 4;
            } catch (Exception ex) {
                log.warn("LLM chat with NPC {} failed: {}", npcCode, ex.getMessage());
                reply = "Chào bạn! Chúc nông trại của bạn hôm nay bội thu nhé!";
            }
        } else {
            reply = "Chào bạn! Chúc một ngày làm nông thật năng suất!";
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        // Update Memory & Affection
        int newAffection = Math.min(1000, memory.getAffectionScore() + 1);
        memory.setAffectionScore(newAffection);
        if (newAffection >= 500) {
            memory.setRelationship(Relationship.CLOSE_FRIEND);
        } else if (newAffection >= 200) {
            memory.setRelationship(Relationship.FRIEND);
        } else if (newAffection >= 50) {
            memory.setRelationship(Relationship.ACQUAINTANCE);
        }
        memory.setLastInteracted(Instant.now());
        memoryRepository.save(memory);

        // FSM state change if IDLE
        FsmState oldState = npc.getFsmState();
        if (oldState == FsmState.IDLE) {
            npc.setFsmState(FsmState.WORKING);
            npc.setLastStateChangeAt(Instant.now());
            npcAgentRepository.save(npc);

            Map<String, Object> statePayload = new HashMap<>();
            statePayload.put("npcCode", npcCode);
            statePayload.put("oldState", oldState.name());
            statePayload.put("newState", FsmState.WORKING.name());
            statePayload.put("mapCode", npc.getCurrentMapCode());
            statePayload.put("coordX", npc.getCoordX());
            statePayload.put("coordY", npc.getCoordY());
            statePayload.put("changedAt", Instant.now().toString());

            outboxService.enqueue("NPC", npcCode, "npc-state-changed.v1", statePayload);
        }

        // Log LLM Call
        LlmCallLog callLog = LlmCallLog.builder()
                .callId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .playerId(playerId)
                .serviceContext(ServiceContext.AI)
                .purpose(LlmPurpose.NPC_DIALOGUE)
                .model("qwen2.5:7b")
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .costUsd(BigDecimal.ZERO)
                .latencyMs((int) latencyMs)
                .status(LlmStatus.SUCCESS)
                .build();
        llmCallLogRepository.save(callLog);

        return new NpcTalkResponse(
                npc.getNpcCode(),
                npc.getDisplayName(),
                reply,
                npc.getFsmState(),
                memory.getAffectionScore(),
                memory.getRelationship()
        );
    }
}
