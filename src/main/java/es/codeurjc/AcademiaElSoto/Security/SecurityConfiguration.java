package es.codeurjc.AcademiaElSoto.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import es.codeurjc.AcademiaElSoto.security.jwt.JwtTokenProvider;
import es.codeurjc.AcademiaElSoto.security.jwt.UnauthorizedHandlerJwt;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.codeurjc.AcademiaElSoto.security.jwt.JwtRequestFilter;
import es.codeurjc.AcademiaElSoto.security.jwt.JwtTokenProvider;
import es.codeurjc.AcademiaElSoto.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

        @Autowired
        private RepositoryUserDetailsService userDetailService;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

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
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

                http.authenticationProvider(authenticationProvider());

                http
                                .securityMatcher("/api/**")
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint(unauthorizedHandlerJwt));

                http
                                .authorizeHttpRequests(authorize -> authorize

                                                // Auth endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/signup").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/refresh").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/logout").permitAll()

                                                // Public GET endpoints
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/courses",
                                                                "/api/v1/courses/*",
                                                                "/api/v1/courses/*/image",
                                                                "/api/v1/comments",
                                                                "/api/v1/comments/*",
                                                                "/api/v1/comments/course/*",
                                                                "/api/v1/users/*/image")
                                                .permitAll()

                                                // Admin endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*")
                                                .hasRole("ADMIN")

                                                // Logged users
                                                .requestMatchers(HttpMethod.GET, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")

                                                .requestMatchers(HttpMethod.POST, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")

                                                // Course image management: admin only
                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/image")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*/image")
                                                .hasRole("ADMIN")

                                                // Carts: authenticated users; ownership checked in controller/service
                                                .requestMatchers(HttpMethod.GET, "/api/v1/carts/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/carts/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/carts/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/**")
                                                .hasAnyRole("USER", "ADMIN")

                                                // User image changes
                                                .requestMatchers(HttpMethod.POST, "/api/v1/users/*/image")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*/image")
                                                .hasAnyRole("USER", "ADMIN")

                                                .anyRequest().authenticated());

                http.formLogin(formLogin -> formLogin.disable());
                http.csrf(csrf -> csrf.disable());
                http.httpBasic(httpBasic -> httpBasic.disable());

                http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.addFilterBefore(
                                new JwtRequestFilter(userDetailService, jwtTokenProvider),
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

                http.authenticationProvider(authenticationProvider());

                http
                                .authorizeHttpRequests(authorize -> authorize

                                                // Public web pages
                                                .requestMatchers(
                                                                "/", "/index", "/teachers", "/information",
                                                                "/courses", "/course/*", "/course/*/image",
                                                                "/user/*/image",
                                                                "/register", "/login", "/loginerror",
                                                                "/403", "/404", "/500",
                                                                "/css/**", "/js/**", "/img/**", "/assets/**",
                                                                "/error/**")
                                                .permitAll()

                                                // Public REST authentication endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/signup").permitAll()

                                                // Public REST GET endpoints
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/courses",
                                                                "/api/v1/courses/*",
                                                                "/api/v1/courses/*/image",
                                                                "/api/v1/comments",
                                                                "/api/v1/comments/*",
                                                                "/api/v1/comments/course/*",
                                                                "/api/v1/users/*/image")
                                                .permitAll()

                                                // Admin-only REST endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*")
                                                .hasRole("ADMIN")

                                                // Logged users REST endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*")
                                                .hasAnyRole("USER", "ADMIN")

                                                .requestMatchers(HttpMethod.POST, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**")
                                                .hasAnyRole("USER", "ADMIN")

                                                // Private web pages
                                                .requestMatchers(
                                                                "/profile", "/cart",
                                                                "/course/*/add-cart",
                                                                "/cart/remove/*",
                                                                "/complete-purchase",
                                                                "/course/*/comment",
                                                                "/profile/comments/*/edit",
                                                                "/profile/comments/*/delete")
                                                .hasAnyRole("USER", "ADMIN")

                                                // Admin web pages
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .failureUrl("/loginerror")
                                                .successHandler((request, response, authentication) -> {
                                                        boolean isAdmin = authentication.getAuthorities().stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_ADMIN"));

                                                        if (isAdmin) {
                                                                response.sendRedirect("/admin");
                                                        } else {
                                                                response.sendRedirect("/profile");
                                                        }
                                                })
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .permitAll())

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                                response.setContentType("application/json");
                                                                response.getWriter()
                                                                                .write("""
                                                                                                {
                                                                                                  "status": 401,
                                                                                                  "error": "Unauthorized",
                                                                                                  "message": "Authentication is required"
                                                                                                }
                                                                                                """);
                                                        } else {
                                                                response.sendRedirect("/login");
                                                        }
                                                })

                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                                response.setContentType("application/json");
                                                                response.getWriter().write("""
                                                                                {
                                                                                  "status": 403,
                                                                                  "error": "Forbidden",
                                                                                  "message": "Access denied"
                                                                                }
                                                                                """);
                                                        } else {
                                                                response.sendRedirect("/403");
                                                        }
                                                }));

                return http.build();
        }
}