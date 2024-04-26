package funfit.pt.user.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.user.dto.JoinRequest;
import funfit.pt.user.dto.JoinResponse;
import funfit.pt.user.dto.JwtDto;
import funfit.pt.user.dto.LoginRequest;
import funfit.pt.user.entity.Role;
import funfit.pt.user.entity.User;
import funfit.pt.user.repository.UserRepository;
import funfit.pt.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public JoinResponse join(JoinRequest joinRequest) {
        validateDuplicate(joinRequest.getEmail());
        User user = User.create(joinRequest.getEmail(), joinRequest.getPassword(), joinRequest.getName(),
                Role.find(joinRequest.getRole()), joinRequest.getPhoneNumber());
        userRepository.save(user);
        return new JoinResponse(user.getEmail(), user.getName(), user.getRole().getName());
    }

    private void validateDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    public JwtDto login(LoginRequest loginRequest) {
        validateEmailPassword(loginRequest);
        return jwtUtils.generateJwt(loginRequest.getEmail());
    }

    private void validateEmailPassword(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_EMAIL));
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }
}
