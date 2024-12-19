package recipeSharing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://172.20.10.3:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // config.addAllowedOriginPattern("*"); // Use this if you want to allow all origins, but it's not recommended for production
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
