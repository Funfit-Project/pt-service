package funfit.pt.relationship.controller;

import funfit.pt.relationship.dto.AddTrainerRequest;
import funfit.pt.relationship.dto.AddTrainerResponse;
import funfit.pt.relationship.service.RelationshipService;
import funfit.pt.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/api/relationship/add")
    public ResponseEntity addTrainer(@RequestBody AddTrainerRequest addTrainerRequest, HttpServletRequest request) {
        AddTrainerResponse addTrainerResponse = relationshipService.addTrainer(addTrainerRequest, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("트레이너 추가 성공", addTrainerResponse));
    }
}
