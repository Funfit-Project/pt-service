package funfit.pt.api;

import funfit.pt.api.dto.User;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.ExternalServiceException;
import funfit.pt.kafka.dto.CompensatePointsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", fallbackFactory = AuthServiceClient.FallbackFactory.class, primary = true)
public interface AuthServiceClient {

    @GetMapping("/feignClient/user/pt")
    User getUserByEmail(@RequestParam(name = "email") String email);

    @PostMapping("/deduct/points")
    boolean deductPoints(@RequestBody CompensatePointsDto compensatePointsDto);

    @Slf4j
    @Component
    @RequiredArgsConstructor
    class Fallback implements AuthServiceClient {

        @Override
        public User getUserByEmail(String email) {
            log.info("auth service 문제 발생");
            return null;
        }

        @Override
        public boolean deductPoints(CompensatePointsDto compensatePointsDto) {
            log.info("auth service 문제 발생");
            throw new ExternalServiceException(ErrorCode.UNAVAILABLE_AUTH_SERVICE);
        }
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    class FallbackFactory implements org.springframework.cloud.openfeign.FallbackFactory<Fallback> {

        private final Fallback fallback;

        @Override
        public Fallback create(Throwable cause) {
            log.info(cause.getMessage());
            return fallback;
        }
    }
}
