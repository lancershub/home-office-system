package com.yourcompany.reception.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OfficeUserDetailsService users) throws Exception {
        http.userDetailsService(users);
        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/", "/hello", "/js/**", "/css/**", "/images/**", "/lib/**").permitAll()
            .antMatchers(HttpMethod.POST, "/login", "/register").permitAll()
            .antMatchers("/main", "/list", "/addVisitor", "/updateVisitor", "/deleteVisitor",
                "/attendanceList", "/exportAttendance", "/dept/**", "/inst/**",
                "/user/assignPage", "/user/list", "/user/assignDept").hasRole("ADMIN")
            .antMatchers("/visitorIndex", "/clockIn", "/clockOut", "/file/**", "/schedule/**").hasRole("EMPLOYEE")
            .antMatchers("/user/chatPage", "/user/chatHistory", "/native/chat/*", "/logout.action").authenticated()
            .anyRequest().denyAll();
        http.formLogin().loginPage("/hello").loginProcessingUrl("/login")
            .failureUrl("/hello?error=true")
            .successHandler((request, response, authentication) -> {
                HttpSession session = request.getSession();
                session.removeAttribute("adminUser");
                session.removeAttribute("visitorId");
                session.removeAttribute("visitorName");
                boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (admin) session.setAttribute("adminUser", authentication.getName());
                else {
                    session.setAttribute("visitorId", Integer.valueOf(authentication.getName()));
                    session.setAttribute("visitorName", users.displayName(authentication.getName()));
                }
                response.sendRedirect(request.getContextPath() + (admin ? "/main" : "/visitorIndex"));
            });
        http.logout().logoutUrl("/logout.action").logoutSuccessUrl("/hello").invalidateHttpSession(true).deleteCookies("JSESSIONID");
        http.exceptionHandling().authenticationEntryPoint((req, res, ex) -> res.sendError(401))
            .accessDeniedHandler((req, res, ex) -> res.sendError(403));
        http.headers().frameOptions().sameOrigin();
        http.sessionManagement().sessionFixation().changeSessionId();
        http.addFilterBefore(new LoginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class);
        // CSRF remains enabled, including login, registration, logout and multipart uploads.
        return http.build();
    }
}
