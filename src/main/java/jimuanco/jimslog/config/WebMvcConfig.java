package jimuanco.jimslog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    @Value("${cors.client}")
    private String corsClient;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 경로에 대해
        registry.addMapping("/**")
                .allowedOrigins(corsClient)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 모든 헤더와
                .allowCredentials(true) // 인증에 관한 정보도 허용
                .maxAge(MAX_AGE_SECS);
    }
}
