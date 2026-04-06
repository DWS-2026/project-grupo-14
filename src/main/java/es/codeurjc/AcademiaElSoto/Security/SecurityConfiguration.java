package es.codeurjc.AcademiaElSoto.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    public RepositoryUserDetailsService userDetailService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/teachers", "/information", "/index",
                                "/courses", "/course/*", "/course/*/image", "/user/*/image",
                                "/css/**", "/js/**", "/img/**", "/assets/**",
                                "/error/**", "/register", "/login", "/loginerror",
                                "/403", "/404", "/500")
                        .permitAll()

                        .requestMatchers(
                                "/profile", "/cart",
                                "/course/*/add-cart",
                                "/cart/remove/*",
                                "/complete-purchase",
                                "/course/*/comment",
                                "/profile/comments/*/edit",
                                "/profile/comments/*/delete")
                        .hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureUrl("/loginerror")
                        .successHandler((req, res, auth) -> {
                            boolean isAdmin = auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                res.sendRedirect("/admin");
                            } else {
                                res.sendRedirect("/profile");
                            }
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403"));

        return http.build();
    }
}