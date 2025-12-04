package controller;

import dto.ResponseDto;
import dto.SignInDto;
import dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDto> signUp(@RequestBody SignUpDto signUpDto) {
        ResponseDto response = userService.signUp(signUpDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<ResponseDto> signIn(@RequestBody SignInDto signInDto) {
        ResponseDto response = userService.signIn(signInDto);
        return ResponseEntity.ok(response);
    }
}
