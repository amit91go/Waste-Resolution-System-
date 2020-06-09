package de.bamberg.uni.isosysc.dsg.financer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import de.bamberg.uni.isosysc.dsg.shared.models.CustomUserDetailsService;

/*
 *
 * Main class to authenticate users with digest authentication.
 */
@Component
@Configuration
@EnableWebSecurity
@ComponentScan("de.bamberg.uni.isosysc.dsg")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private WebApplicationContext applicationContext;
	private CustomUserDetailsService userDetailsService;
	
    @PostConstruct
    public void completeSetup() {
    	userDetailsService = applicationContext.getBean(CustomUserDetailsService.class);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http
            .csrf().disable()
            .addFilter(digestAuthenticationFilter())              // register digest entry point
            .exceptionHandling().authenticationEntryPoint(digestEntryPoint())      // on exception ask for digest authentication                 // it indicate basic authentication is requires
            .and()
            .authorizeRequests()
            .anyRequest().hasAuthority("Financer"); // Only accessible by financiers.

    }

    DigestAuthenticationFilter digestAuthenticationFilter() throws Exception {

        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setUserDetailsService(userDetailsService);
        //digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean());
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
        return digestAuthenticationFilter;
    }

    /*@Override
    @Bean
    public UserDetailsService userDetailsServiceBean() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
        inMemoryUserDetailsManager.createUser(User.withUsername("zone").password("password").roles("USER").build());
        return inMemoryUserDetailsManager;

    }*/

    @Bean
    DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint bauth = new DigestAuthenticationEntryPoint();
        bauth.setRealmName("Resolver");
        bauth.setKey("ResolverKey");
        return bauth;
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}


