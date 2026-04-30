package es.codeurjc.AcademiaElSoto.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.AcademiaElSoto.security.jwt.AuthResponse;
import es.codeurjc.AcademiaElSoto.security.jwt.LoginRequest;
import es.codeurjc.AcademiaElSoto.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class SessionRestController {

    private final UserLoginService userLoginService;

    public SessionRestController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        return userLoginService.login(response, loginRequest);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        return userLoginService.refresh(response, refreshToken);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {

        return ResponseEntity.ok(
                new AuthResponse(
                        AuthResponse.Status.SUCCESS,
                        userLoginService.logout(response)));
    }
}