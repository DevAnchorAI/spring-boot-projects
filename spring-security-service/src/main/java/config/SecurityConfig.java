package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		
		
		  UserDetails normalUser =User.withUsername("suman").password(passwordEncoder().encode("password")).	  
		  roles("NORMAL").build(); 
		  UserDetails adminUser  =User.withUsername("suman1").password(passwordEncoder().encode("password")).		 
		  roles("ADMIN").build();
		  
		  InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(normalUser,adminUser);
		   
		  return inMemoryUserDetailsManager; 
		
		//return new CustomUserDetailsService();
	}
	
	@Bean
	public SecurityFilterChain filterchain(HttpSecurity httpSecurity) throws Exception {
		 httpSecurity.csrf().disable()
				.authorizeRequests()
//				.requestMatchers("/home/admin")
//				.hasRole("ADMIN")
//				.requestMatchers("/home/normal")
//				.hasRole("NORMAL")
//				.requestMatchers("/home/public")
//				.permitAll()
				.anyRequest()
				.authenticated()
				.and()
				.formLogin();
		
		return httpSecurity.build();
		
	}

}
