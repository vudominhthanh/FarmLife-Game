package social_quest_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages", schema = "social_quest")
@IdClass(ChatMessage.ChatMessageId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id", nullable = false, updatable = false)
    private Long msgId;

    @Id
    @Column(name = "sent_at", nullable = false, updatable = false)
    @NotNull
    private Instant sentAt = Instant.now();

    @Column(name = "channel", length = 50, nullable = false)
    @NotBlank @Size(max = 50)
    private String channel;

    @Column(name = "sender_id", nullable = false)
    @NotNull
    private UUID senderId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    @NotBlank
    private String content;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class ChatMessageId implements Serializable {
        private Long msgId;
        private Instant sentAt;
    }
}