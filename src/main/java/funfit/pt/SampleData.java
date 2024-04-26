package funfit.pt;

import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.user.entity.Role;
import funfit.pt.user.entity.User;
import funfit.pt.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleData {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    public void initUser() {
        User user1 = User.create("user1@naver.com", "1234", "user1", Role.TRAINER, "01012341234");
        User user2 = User.create("user2@naver.com", "1234", "user2", Role.MEMBER, "01012341234");
        userRepository.save(user1);
        userRepository.save(user2);

        Relationship relationship = Relationship.create(user1, user2, "석세스짐", 20);
        relationshipRepository.save(relationship);
    }
}
