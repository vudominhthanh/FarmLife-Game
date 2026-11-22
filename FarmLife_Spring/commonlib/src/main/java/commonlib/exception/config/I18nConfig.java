package exception.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class I18nConfig {

    private static final Locale DEFAULT_LOCALE = Locale.of("vi");

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        // Đọc file messages của từng service trước, nếu không thấy sẽ tìm trong common_messages
        source.setBasenames("classpath:i18n/messages", "classpath:i18n/common_messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setDefaultLocale(DEFAULT_LOCALE);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                // 1. Ưu tiên đọc từ header nội bộ X-Player-Language (từ API Gateway)
                String playerLang = request.getHeader("X-Player-Language");
                if (StringUtils.hasText(playerLang)) {
                    return Locale.forLanguageTag(playerLang.trim());
                }
                // 2. Đọc từ chuẩn HTTP Accept-Language (Unity gửi khi Login/Register)
                String acceptLang = request.getHeader("Accept-Language");
                if (StringUtils.hasText(acceptLang)) {
                    return Locale.lookup(Locale.LanguageRange.parse(acceptLang),
                            java.util.List.of(Locale.of("vi"), Locale.of("en"), Locale.of("ja"))) != null
                            ? Locale.forLanguageTag(acceptLang.split(",")[0].trim())
                            : DEFAULT_LOCALE;
                }
                return DEFAULT_LOCALE;
            }
        };
    }
}