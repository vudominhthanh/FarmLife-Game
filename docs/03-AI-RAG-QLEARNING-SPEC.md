\## 5. Nội dung file docs/03-AI-RAG-QLEARNING-SPEC.md



Bạn tạo file \*\*farmlife-backend/docs/03-AI-RAG-QLEARNING-SPEC.md\*\* để hướng dẫn Agent lập trình chính xác các thuật toán AI trong \*\*economy-service\*\* và \*\*ai-service\*\*:



```markdown

\# ĐẶC TẢ KỸ THUẬT AI AGENT, RAG PGVECTOR \& Q-LEARNING MARKET (V6.1)



\## 1. Thuật toán Định giá Động bằng Q-Learning (Đặt trong `economy-service`)

Class `QLearningPricingScheduler` chạy định kỳ mỗi 15 phút để cập nhật bảng `economy.market\_tickers` và ghi nến giá vào `economy.price\_candles`:



\### 1.1. Không gian Trạng thái (State $S\_t$) và Hành động (Action $A\_t$)

\- Tính tỷ lệ mất cân bằng cung cầu:

&#x20; `imbalance = (demand\_volume\_24h - supply\_volume\_24h) / (demand\_volume\_24h + supply\_volume\_24h + 1.0)`

\- Phân loại `imbalance` thành 5 trạng thái rời rạc ($S\_t$):

&#x20; - `SEVERE\_OVERSUPPLY`: nhỏ hơn -0.50

&#x20; - `MILD\_OVERSUPPLY`: từ -0.50 đến -0.15

&#x20; - `BALANCED`: từ -0.15 đến +0.15

&#x20; - `MILD\_SHORTAGE`: từ +0.15 đến +0.50

&#x20; - `SEVERE\_SHORTAGE`: lớn hơn +0.50

\- Tập 5 hành động điều chỉnh giá ($A\_t$):

&#x20; - `DEC\_10` (-10%), `DEC\_5` (-5%), `HOLD` (0%), `INC\_5` (+5%), `INC\_10` (+10%).



\### 1.2. Công thức Cập nhật Q-Table và Ép trần Giá

\- Đọc ma trận `q\_table` từ cột JSONB `economy.market\_tickers.ai\_state`.

\- Chọn hành động $A\_t$ theo chiến lược $\\epsilon$-greedy ($\\epsilon = 0.10$).

\- Tính giá mới và kẹp trong biên độ an toàn:

&#x20; `newPrice = Math.clamp(Math.round(currentPrice \* (1.0 + actionPct)), minPrice, maxPrice)`

\- Cập nhật giá trị Q theo phương trình Bellman ($\\alpha = 0.2$, $\\gamma = 0.85$):

&#x20; `Q(S, A) = Q(S, A) + alpha \* (reward + gamma \* maxNextQ - Q(S, A))`

&#x20; Trong đó `reward` đạt cực đại khi lượng giao dịch tiến về trạng thái `BALANCED`.

\- Ghi 1 dòng vào `economy.price\_candles` và phát sự kiện `farmlife.economy.market-price-tick.v1`.



\## 2. Cơ chế Pre-generated Quest Pool \& Guardrail Clamping (Đặt trong `ai-service`)

Tuyệt đối không gọi LLM đồng bộ lúc người chơi đang đứng đợi trước mặt NPC.



\### 2.1. Luồng Tạo sẵn Nhiệm vụ ở Background (`BackgroundQuestGenerator`)

1\. Khi nhận sự kiện `player-created.v1` hoặc `playstyle-updated.v1`, `ai-service` kiểm tra số lượng nhiệm vụ có `status = 'ready'` của `player\_id` trong bảng `ai.pregenerated\_quests`.

2\. Nếu số lượng nhỏ hơn 3:

&#x20;  - Truy vấn quy tắc phù hợp nhất từ `ai.quest\_generation\_rules` theo `level` và `play\_style\_tag`.

&#x20;  - Gọi Spring AI `ChatClient` với `BeanOutputConverter<GeneratedQuestDto>` yêu cầu LLM đóng vai NPC tạo nhiệm vụ chỉ sử dụng các vật phẩm nằm trong `allowed\_items`.

3\. \*\*Bước Guardrail Clamping (Bắt buộc thực thi trong code Java trước khi lưu DB):\*\*

&#x20;  ```java

&#x20;  if (! Arrays.asList(rule.getAllowedItems()).contains(dto.targetItemCode())) {

&#x20;      targetItem = rule.getAllowedItems()\[0]; // Fallback về vật phẩm hợp lệ đầu tiên

&#x20;  }

&#x20;  int safeQty = Math.clamp(dto.goalTarget(), rule.getMinTargetQty(), rule.getMaxTargetQty());

&#x20;  long safeCoins = Math.clamp(dto.coinReward(), 10L, rule.getMaxCoinReward());

&#x20;  long safeXp = Math.clamp(dto.xpReward(), 5L, rule.getMaxXpReward());

