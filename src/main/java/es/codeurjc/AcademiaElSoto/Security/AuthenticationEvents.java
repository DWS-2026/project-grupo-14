package es.codeurjc.AcademiaElSoto.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import es.codeurjc.AcademiaElSoto.service.UserService;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEvents {

    @Autowired
    private UserService userService;

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String userName = event.getAuthentication().getName();
        userService.findByUserName(userName).ifPresent(user -> {
            userService.increaseFailedAttempts(user); //Increase  count
        });
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String userName = event.getAuthentication().getName();
        userService.resetFailedAttempts(userName); //Reset count
    }
}
