package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity

public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public static BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private SSUserDetailsService userDetailsService;

    @Autowired
    private UserRepository appUserRepository;

    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception{
        return new SSUserDetailsService(appUserRepository);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{

        http
                .authorizeRequests()
                .antMatchers("/", "/detail/**", "/h2-console/**", "/register", "/logoutconfirm").permitAll()
                .anyRequest().authenticated()

                .and()
                .formLogin().loginPage("/login").permitAll()



                .and()
                .logout()
                .logoutRequestMatcher(
                        new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/logoutconfirm").permitAll() // if logout is successful it'll take us back to logout page.


                .and()
                .httpBasic(); // browser identifies you as a user. not good for security, remove for real apps

        http
                .csrf().disable();
        http
                .headers().frameOptions().disable();
    }

    // multiple users can be configured here but atm it is for single in-memory user
    // you can further specify how users are granted access to app if their details are stored in db
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{

        auth.userDetailsService(userDetailsServiceBean())
                .passwordEncoder(passwordEncoder());

//        auth.inMemoryAuthentication()
//                .withUser("user")
//                .password(passwordEncoder().encode("password"))
//                .authorities("USER")
//
//                .and()
//                // add another user
//                .withUser("user2")
//                .password(passwordEncoder().encode("password2"))
//                .authorities("USER")
//                // .authorities() defines the access type
//
//                .and()
//                // add admin
//                .withUser("admin")
//                .password(passwordEncoder().encode("masterpassword"))
//                .authorities("ADMIN");

    }
}

