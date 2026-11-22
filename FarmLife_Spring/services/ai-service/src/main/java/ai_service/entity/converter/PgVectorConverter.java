package ai_service.entity.converter;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.SQLException;

/**
 * Map PostgreSQL vector(768) ↔ PGvector của thư viện com.pgvector.
 * PGvector.toString() trả "[0.1,0.2,...]" — đúng format Postgres nhận.
 * Đây là TYPE converter (JPA không có kiểu vector native), KHÔNG phải
 * enum converter — không thể tránh bằng UPPERCASE.
 */
@Converter
public class PgVectorConverter implements AttributeConverter<PGvector, String> {

    @Override
    public String convertToDatabaseColumn(PGvector v) {
        return v == null ? null : v.toString();
    }

    @Override
    public PGvector convertToEntityAttribute(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return new PGvector(v);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}