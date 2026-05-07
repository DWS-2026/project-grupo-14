package es.codeurjc.AcademiaElSoto.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

import es.codeurjc.AcademiaElSoto.security.jwt.JwtTokenProvider;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.codeurjc.AcademiaElSoto.security.jwt.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

        @Autowired
        private RepositoryUserDetailsService userDetailService;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Bean
        public PasswordEncoder passwordEncoder() {
                // Increased cost factor to 12 for stronger encryption and better protection against brute-force attacks
                return new BCryptPasswordEncoder(12);
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
        @Order(0)
        public SecurityFilterChain apiDocsFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher(
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().permitAll())
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

                http.authenticationProvider(authenticationProvider());

                http
                                .securityMatcher("/api/**")
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("""
                                                                        {
                                                                          "status": 401,
                                                                          "error": "Unauthorized",
                                                                          "message": "Authentication is required"
                                                                        }
                                                                        """);
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("""
                                                                        {
                                                                          "status": 403,
                                                                          "error": "Forbidden",
                                                                          "message": "Access denied"
                                                                        }
                                                                        """);
                                                }));

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
                                                                "/api/v1/courses/*/comments",
                                                                "/api/v1/courses/*/recommended-books",
                                                                "/api/v1/comments",
                                                                "/api/v1/comments/*",
                                                                "/api/v1/users/*/image")
                                                .permitAll()

                                                // Admin endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/*")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")

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

                                                // Carts
                                                .requestMatchers(HttpMethod.GET, "/api/v1/carts").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/carts/me")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/carts/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/carts/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/carts/me/courses/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/me/courses")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/me/courses/*")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/*").hasRole("ADMIN")

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
                                                                "/error/**", "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml")
                                                .permitAll()

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

                                // --- A05:2025 INJECTION (XSRF) PROTECTION ---
                                // Enabled CSRF protection for web sessions using the Synchronizer Token Pattern.
                                // This prevents attackers from submitting unauthorized requests on behalf of the user.
                                .csrf(withDefaults())

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