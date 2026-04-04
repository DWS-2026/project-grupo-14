package es.codeurjc.AcademiaElSoto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("pass"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
            .authorizeHttpRequests(authorize -> authorize
                // 🌐 Páginas públicas
                .requestMatchers("/", "/teachers", "/information", "/index",
                                 "/courses", "/css/**", "/js/**", "/img/**", "/assets/**",
                                 "/error/**", "/register").permitAll()

                // 👤 Páginas USER
                .requestMatchers("/user", "/complete-purchase", "/cart")
                    .hasAnyRole("USER", "ADMIN") // ADMIN también puede entrar si quieres

                // 👑 Páginas ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 🔐 Cualquier otra requiere autenticación
                .anyRequest().authenticated()
            )

            // 🔑 Form Login
            .formLogin(form -> form
                .loginPage("/login")                 // URL de login personalizada
                .loginProcessingUrl("/login")        // URL de procesamiento
                .failureUrl("/loginerror")           // error login
                .successHandler((req, res, auth) -> { // redirige según rol
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (isAdmin) {
                        res.sendRedirect("/admin");
                    } else {
                        res.sendRedirect("/indx");
                    }
                })
                .permitAll()
            )

            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )

            // 🚫 Desactivar CSRF por ahora (solo para pruebas)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

}


