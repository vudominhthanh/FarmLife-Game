\## 6. Nội dung file docs/04-UNITY-CLIENT-CONTRACT.md



Bạn tạo file \*\*farmlife-backend/docs/04-UNITY-CLIENT-CONTRACT.md\*\* để chuẩn hóa giao tiếp giữa các Microservices và Unity 2D Client:



```markdown

\# CHUẨN GIAO TIẾP API, WEBSOCKET \& ĐỒNG BỘ OFFLINE VỚI UNITY CLIENT (V6.1)



\## 1. Quy chuẩn HTTP Headers Bắt buộc

Mọi yêu cầu từ Unity Client gửi qua `api-gateway` (Port 8080) phải tuân thủ:

\- `Authorization: Bearer <access\_token>`

\- `Idempotency-Key: <uuid\_v4>` (Bắt buộc cho mọi phương thức POST, PUT, PATCH, DELETE).

\- Khi `api-gateway` xác thực JWT thành công, Gateway sẽ tự động đính kèm header nội bộ `X-Player-Id: <player\_uuid>` trước khi chuyển tiếp xuống các service phía sau.



\## 2. Cấu trúc Phản hồi Chuẩn \& Xử lý Lỗi (RFC 7807 ProblemDetail)

\- Mọi lỗi nghiệp vụ trả về định dạng chuẩn `application/problem+json`:

&#x20; ```json

&#x20; {

&#x20;   "type": "\[https://farmlife.com/errors/insufficient-items](https://farmlife.com/errors/insufficient-items)",

&#x20;   "title": "Insufficient Items",

&#x20;   "status": 422,

&#x20;   "detail": "Bạn cần 10 Hạt giống Bí ngô nhưng chỉ có 4 hạt khả dụng.",

&#x20;   "errorCode": "ECON\_INSUFFICIENT\_ITEMS",

&#x20;   "timestamp": "2026-11-06T01:00:00Z"

&#x20; }

