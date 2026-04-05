package es.codeurjc.AcademiaElSoto.Security;
import org.springframework.beans.factory.annotation.Autowired;
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
                        res.sendRedirect("/index");
                    }
                })
                .permitAll()
            )

            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

            

        return http.build();
    }

}


