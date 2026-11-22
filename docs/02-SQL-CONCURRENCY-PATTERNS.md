\## 4. Nội dung file docs/02-SQL-CONCURRENCY-PATTERNS.md



Bạn tạo file \*\*farmlife-backend/docs/02-SQL-CONCURRENCY-PATTERNS.md\*\* chứa các câu lệnh SQL bắt buộc phải dùng nguyên bản trong tầng Repository:



```markdown

\# CÁC MẪU TRUY VẤN SQL CHỐNG XUNG ĐỘT \& CRON JOBS CHUẨN (V6.1)



Agent BẮT BUỘC phải sử dụng chính xác các câu truy vấn Native SQL dưới đây trong `@Query(nativeQuery = true)` của Spring Data JPA hoặc `JdbcClient`. Tuyệt đối không tự viết câu lệnh UPDATE túi đồ thiếu giới hạn `slot\_id`.



\## 1. Pattern A — Cộng vật phẩm vào ĐÚNG 1 ô chưa đầy trong Túi đồ

Khi thu hoạch hoặc nhận thưởng, thực hiện theo thứ tự 3 bước trong cùng 1 `@Transactional`:



\### Bước A1: Tìm và cộng vào 1 ô đang chứa cùng loại vật phẩm và cùng chất lượng còn chỗ trống

```sql

WITH target AS (

&#x20;   SELECT i.slot\_id

&#x20;   FROM economy.inventory\_items i

&#x20;   JOIN economy.items\_catalog c ON c.item\_code = i.item\_code

&#x20;   WHERE i.inventory\_id = :invId

&#x20;     AND i.item\_code = :itemCode

&#x20;     AND i.quality = :quality

&#x20;     AND i.quantity + :addQty <= c.max\_stack

&#x20;   ORDER BY i.slot\_index ASC

&#x20;   LIMIT 1

&#x20;   FOR UPDATE

)

UPDATE economy.inventory\_items i

SET quantity = i.quantity + :addQty,

&#x20;   version = i.version + 1

FROM target

WHERE i.slot\_id = target.slot\_id

RETURNING i.slot\_id;

