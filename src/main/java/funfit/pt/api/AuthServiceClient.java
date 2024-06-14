package funfit.pt.api;

import funfit.pt.api.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/feignClient/user/pt")
    User getUser(@RequestParam String email);
}
