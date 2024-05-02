package funfit.pt.relationship.service;

import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.RequestUserByEmail;
import funfit.pt.rabbitMq.dto.RequestValidateTrainerCode;
import funfit.pt.rabbitMq.dto.ResponseUser;
import funfit.pt.rabbitMq.RabbitMqService;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.dto.AddTrainerRequest;
import funfit.pt.relationship.dto.AddTrainerResponse;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final JwtUtils jwtUtils;
    private final RabbitMqService rabbitMqService;
    private final RedisTemplate redisTemplate;

    public AddTrainerResponse addTrainer(AddTrainerRequest addTrainerRequest, HttpServletRequest request) {

        String email = jwtUtils.getEmailFromHeader(request);

        // 캐시에 사용자가 있는지 확인 후, 없으면 MQ를 통해 받아온 후 저장
        ResponseUser user = (ResponseUser) redisTemplate.opsForValue().get(email);
        if (user == null) {
            rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "user"));
        }
        // 캐시에서 사용자 정보 받아오기
        ResponseUser responseUser = (ResponseUser) redisTemplate.opsForValue().get(email);
        validateRole(responseUser);

        rabbitMqService.requestValidateTrainerCode(new RequestValidateTrainerCode(addTrainerRequest.getUserCode(),
                "response_validate_trainer_code"));

        // 캐시에서 트레이너 정보 받아오기
        ResponseValidateTrainerCode responseValidateTrainerCode = (ResponseValidateTrainerCode) redisTemplate.opsForValue().get(addTrainerRequest.getUserCode());
        if (!responseValidateTrainerCode.isExistTrainerCode()) {
            throw new BusinessException(ErrorCode.INVALID_USER_CODE);
        }
        validateDuplicate(responseUser.getUserId(), responseValidateTrainerCode.getTrainerUserId());

        Relationship relationship = Relationship.create(responseUser.getUserId(), responseValidateTrainerCode.getTrainerUserId(),
                addTrainerRequest.getCenterName(), addTrainerRequest.getRegistrationCount());
        relationshipRepository.save(relationship);

        return new AddTrainerResponse(responseValidateTrainerCode.getUserName(), relationship.getCenterName(), relationship.getRegistrationCount(), relationship.getRegistrationCount());
    }

    private void validateRole(ResponseUser user) {
        if (user.getRoleName().equals("TRAINER")) {
            throw new BusinessException(ErrorCode.REGISTER_ONLY_FOR_MEMBER);
        }
    }

    private void validateDuplicate(long memberUserId, long trainerUserId) {
        Optional<Relationship> optionalRelationship = relationshipRepository.findByMemberAndTrainer(memberUserId, trainerUserId);
        if (optionalRelationship.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_RELATIONSHIP);
        }
    }
}
