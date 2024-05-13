package funfit.pt.relationship.controller;

import funfit.pt.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

}
