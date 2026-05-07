package es.codeurjc.AcademiaElSoto.restcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.AcademiaElSoto.security.jwt.AuthResponse;
import es.codeurjc.AcademiaElSoto.security.jwt.LoginRequest;
import es.codeurjc.AcademiaElSoto.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
public class SessionRestController {

    // A09: Logger to monitor authentication events and detect brute-force attacks
    private static final Logger log = LoggerFactory.getLogger(SessionRestController.class);

    private final UserLoginService userLoginService;

    public SessionRestController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        // Log the start of the authentication process
        log.info("AUTHENTICATION: Login attempt initiated for user '{}'", loginRequest.getUsername());

        ResponseEntity<AuthResponse> authResponse = userLoginService.login(response, loginRequest);

        if (authResponse.getStatusCode().is2xxSuccessful()) {
            // A09: Logging successful access
            log.info("AUTHENTICATION: User '{}' successfully logged in.", loginRequest.getUsername());
        } else {
            // A09: Logging failures is critical to detect credential stuffing or brute-force
            log.warn("AUTHENTICATION: Failed login attempt for user '{}'. Status: {}", 
                     loginRequest.getUsername(), authResponse.getStatusCode());
        }

        return authResponse;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        log.info("SESSION: Access token refresh requested via cookie.");

        ResponseEntity<AuthResponse> refreshResponse = userLoginService.refresh(response, refreshToken);

        if (refreshResponse.getStatusCode().is2xxSuccessful()) {
            log.info("SESSION: Access token successfully rotated.");
        } else {
            // A09: Warning for failed refreshes (could indicate token theft or tampering)
            log.warn("SESSION: Token refresh failed. The provided refresh token might be expired or invalid.");
        }

        return refreshResponse;
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {

        // Monitoring logouts helps track the end of user sessions
        log.info("SESSION: User logout request processed.");

        return ResponseEntity.ok(
                new AuthResponse(
                        AuthResponse.Status.SUCCESS,
                        userLoginService.logout(response)));
    }
}