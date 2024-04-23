package funfit.pt.config;

import funfit.pt.filter.JwtInterceptor;
import funfit.pt.filter.TrainerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final TrainerInterceptor trainerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .order(1)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/join", "/api/login");

        registry.addInterceptor(trainerInterceptor)
                .order(2)
                .addPathPatterns("/api/trainer/**");
    }
}
