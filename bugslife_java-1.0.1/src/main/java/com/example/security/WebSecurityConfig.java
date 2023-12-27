package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;


	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(c -> c.ignoringRequestMatchers("auth/login", "auth/logout")
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
				.cors(c -> c.disable())
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/", "/css/**", "js/**", "/image/**").permitAll()
						.requestMatchers("/*.ico").permitAll()
						.requestMatchers("/admin/").hasRole("ADMIN")
						.requestMatchers("/transactionAmounts/{id}").hasRole("USER")
						.requestMatchers("/transactionAmounts/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.formLogin((form) -> form
						.loginPage("/auth/login")
						.permitAll())
				.logout((logout) -> {
					logout
							.logoutSuccessHandler(customLogoutSuccessHandler);
					String logoutUrl = "/auth/logout";
					logout.logoutRequestMatcher(
							new OrRequestMatcher(
									new AntPathRequestMatcher(logoutUrl, "GET"),
									new AntPathRequestMatcher(logoutUrl, "POST"),
									new AntPathRequestMatcher(logoutUrl, "PUT"),
									new AntPathRequestMatcher(logoutUrl, "DELETE")))
							.invalidateHttpSession(false)
							.clearAuthentication(true);
					logout.permitAll();
				});

		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		UserDetails user = User.builder()
				.username("user")
				.password("{noop}password")
				.roles("USER")
				.build();

		UserDetails admin = User.builder()
				.username("admin")
				.password("{noop}password")
				.roles("USER", "ADMIN")
				.build();
				
		return new CustomInMemoryUserDetailsManager(user, admin);
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
