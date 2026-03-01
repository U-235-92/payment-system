package aq.project.controllers;

import aq.project.dto.IndividualUserRegistrationRequest;
import aq.project.dto.IndividualUserRegistrationResponse;
import aq.project.entities.User;
import aq.project.exceptions.UserExistsException;
import aq.project.mappers.IndividualUserRegistrationMapper;
import aq.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/person")
@RequiredArgsConstructor
public class UserRestControllerV1 {

    private final UserService userService;
    private final IndividualUserRegistrationMapper individualUserRegistrationMapper;

    @PostMapping("/registration")
    public ResponseEntity<IndividualUserRegistrationResponse> register(@RequestBody IndividualUserRegistrationRequest request) throws UserExistsException {
        User user = individualUserRegistrationMapper.toUser(request);
        return ResponseEntity.ok(userService.register(user));
    }
}
