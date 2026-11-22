\## 3. Nội dung file docs/01-ARCHITECTURE-AND-EVENTS.md



Bạn tạo file \*\*farmlife-backend/docs/01-ARCHITECTURE-AND-EVENTS.md\*\* để Agent nắm rõ danh sách 12 Kafka Topics và cấu trúc JSON chuẩn khi giao tiếp giữa các service:



```markdown

\# ĐẶC TẢ KIẾN TRÚC LIÊN SERVICE \& HỢP ĐỒNG SỰ KIỆN KAFKA (V6.1)



\## 1. Quy ước Khung Bản tin Kafka Chuẩn (Event Envelope)

Mọi sự kiện ghi vào bảng `outbox\_events` và đẩy lên Kafka bắt buộc phải bọc trong cấu trúc `EventEnvelope<T>` thống nhất đặt tại module `farmlife-common`:



```java

package com.farmlife.common.contracts;



import java.time.Instant;

import java.util.UUID;



public record EventEnvelope<T>(

&#x20;   UUID eventId,           // Lấy từ outbox\_events.event\_id (dùng để kiểm tra bảng processed\_events)

&#x20;   String eventType,       // Tên sự kiện kèm phiên bản, ví dụ: "crop.harvested.v1"

&#x20;   String aggregateType,   // "PLAYER", "FARM", "INVENTORY", "MARKET", "MEDIA", "AI\_QUEST"

&#x20;   String aggregateId,     // Dùng làm Kafka Partition Key (thường là player\_id để đảm bảo thứ tự)

&#x20;   Instant occurredAt,     // Thời điểm phát sinh sự kiện (UTC)

&#x20;   T payload               // Dữ liệu chi tiết của sự kiện

) {}

