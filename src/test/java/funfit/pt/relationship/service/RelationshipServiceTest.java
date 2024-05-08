package funfit.pt.relationship.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
import funfit.pt.rabbitMq.service.RabbitMqService;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.dto.AddTrainerRequest;
import funfit.pt.relationship.dto.AddTrainerResponse;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Slf4j
class RelationshipServiceTest {

    private final Key signingKey;
    private final RelationshipRepository relationshipRepository;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, UserDto> redisTemplate;
    private final RabbitMqService rabbitMqService;
    private RelationshipService relationshipService;
    private long memberUserId = 1;
    private long trainerUserId = 2;


    @Autowired
    public RelationshipServiceTest(@Value("${jwt.secret}") String secretKey,
                                   RelationshipRepository relationshipRepository,
                                   JwtUtils jwtUtils,
                                   RedisTemplate<String, UserDto> redisTemplate,
                                   RabbitMqService rabbitMqService) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.relationshipRepository = relationshipRepository;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.rabbitMqService = rabbitMqService;
    }

    @Test
    @DisplayName("트레이너 추가 성공")
    public void addTrainerTestSuccess() {
        relationshipService =  new RelationshipService(relationshipRepository, jwtUtils, new StubUserServiceByMemberRequest(redisTemplate, rabbitMqService));

        // given
        HttpServletRequest request = generateRequest();
        AddTrainerRequest requestDto = new AddTrainerRequest("userCode", "centerName", 10);

        // when
        AddTrainerResponse responseDto = relationshipService.addTrainer(requestDto, request);

        // then
        Assertions.assertThat(responseDto.getTrainerName()).isEqualTo("trainerName");
        Assertions.assertThat(responseDto.getCenterName()).isEqualTo("centerName");
        Assertions.assertThat(responseDto.getRegistrationCount()).isEqualTo(10);
        Assertions.assertThat(responseDto.getRemainingCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("트레이너 추가 실패-회원이 아닌 경우")
    public void addTrainerTestFailByOnlyMember() {
        relationshipService =  new RelationshipService(relationshipRepository, jwtUtils, new StubUserServiceByTrainerRequest(redisTemplate, rabbitMqService));

        // given
        HttpServletRequest request = generateRequest();
        AddTrainerRequest requestDto = new AddTrainerRequest("userCode", "centerName", 10);

        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            relationshipService.addTrainer(requestDto, request);
        });
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REGISTER_ONLY_FOR_MEMBER);
    }

    @Test
    @DisplayName("트레이너 추가 실패-이미 추가된 트레이너인 경우")
    public void addTrainerTestFailByAlreadyRegister() {
        relationshipService =  new RelationshipService(relationshipRepository, jwtUtils, new StubUserServiceByMemberRequest(redisTemplate, rabbitMqService));

        // given
        Relationship relationship = Relationship.create(memberUserId, trainerUserId, "centerName", 10);
        relationshipRepository.save(relationship);

        HttpServletRequest request = generateRequest();
        AddTrainerRequest requestDto = new AddTrainerRequest("userCode", "centerName", 10);

        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            relationshipService.addTrainer(requestDto, request);
        });
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_RELATIONSHIP);
    }

    public HttpServletRequest generateRequest() {
        Claims claims = Jwts.claims()
                .setSubject("user@naver.com");
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60)))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization",  "Bearer " + accessToken);
        return request;
    }

    public class StubUserServiceByMemberRequest extends UserService {

        public StubUserServiceByMemberRequest(RedisTemplate<String, UserDto> redisTemplateForUserDto, RabbitMqService rabbitMqService) {
            super(redisTemplateForUserDto, rabbitMqService);
        }

        @Override
        public UserDto getUserDto(String email) {
            return new UserDto(memberUserId, email, "user", "회원", "01011112222", "userCode");
        }

        @Override
        public ResponseValidateTrainerCode getTrainerDto(String userCode) {
            log.info("stub 실행!!!!!!");
            return new ResponseValidateTrainerCode(true, trainerUserId, "trainerName", "userCode");
        }
    }

    public class StubUserServiceByTrainerRequest extends UserService {

        public StubUserServiceByTrainerRequest(RedisTemplate<String, UserDto> redisTemplateForUserDto, RabbitMqService rabbitMqService) {
            super(redisTemplateForUserDto, rabbitMqService);
        }

        @Override
        public UserDto getUserDto(String email) {
            return new UserDto(memberUserId, email, "user", "트레이너", "01011112222", "userCode");
        }

        @Override
        public ResponseValidateTrainerCode getTrainerDto(String userCode) {
            return new ResponseValidateTrainerCode(true, trainerUserId, "trainerName", "userCode");
        }
    }
}
