package com.yourcompany.reception.security;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.mock.web.*;
import javax.servlet.http.HttpSession;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes=SecurityIntegrationTest.Config.class)
public class SecurityIntegrationTest {
    @Configuration @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean OfficeUserDetailsService users(){return mock(OfficeUserDetailsService.class);}
        @Bean Endpoints endpoints(){return new Endpoints();}
        @Bean com.yourcompany.reception.controller.ReceptionController reception(){
            return new com.yourcompany.reception.controller.ReceptionController(mock(JdbcTemplate.class),mock(com.yourcompany.reception.service.AccountService.class));
        }
    }
    @RestController static class Endpoints {
        @GetMapping({"/attendanceList","/exportAttendance"}) public String get(){return "ok";}
        @PostMapping({"/clockIn","/file/upload","/schedule/add","/dept/save"}) public String post(){return "ok";}
    }
    @Autowired WebApplicationContext context;
    @Autowired OfficeUserDetailsService users;
    @Autowired PasswordEncoder encoder;
    private MockMvc mvc;
    @Before public void setup(){
        mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(users.loadUserByUsername("1")).thenReturn(User.withUsername("1").password(encoder.encode("test-password-2026")).roles("EMPLOYEE").build());
        when(users.displayName("1")).thenReturn("Employee");
    }
    @Test public void anonymousCannotAccessManagement()throws Exception{
        for(String path:new String[]{"/main","/list","/attendanceList","/exportAttendance"})
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post("/deleteVisitor").with(csrf())).andExpect(status().isUnauthorized());
    }
    @Test public void applicationRootRedirectsWithinDeploymentContext()throws Exception{
        mvc.perform(get("/demo-1/").contextPath("/demo-1"))
            .andExpect(status().isFound()).andExpect(redirectedUrl("/demo-1/hello"));
    }
    @Test public void employeeCannotAccessManagementEvenWithCsrf()throws Exception{
        mvc.perform(get("/list").with(user("1").roles("EMPLOYEE"))).andExpect(status().isForbidden());
        mvc.perform(post("/deleteVisitor").with(user("1").roles("EMPLOYEE")).with(csrf())).andExpect(status().isForbidden());
    }
    @Test public void everyWriteNeedsCsrfAndGetCannotDelete()throws Exception{
        mvc.perform(post("/addVisitor").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/addVisitor").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/deleteVisitor").with(user("admin").roles("ADMIN"))).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/register")).andExpect(status().isForbidden());
        mvc.perform(post("/clockIn").with(user("1").roles("EMPLOYEE")).with(csrf())).andExpect(status().isOk());
    }
    @Test public void multipartUploadUsesCsrfHeader()throws Exception{
        MockMultipartFile file=new MockMultipartFile("file","test.txt","text/plain",new byte[]{1});
        mvc.perform(multipart("/file/upload").file(file).with(user("1").roles("EMPLOYEE"))).andExpect(status().isForbidden());
        mvc.perform(multipart("/file/upload").file(file).with(user("1").roles("EMPLOYEE")).with(csrf().asHeader())).andExpect(status().isOk());
    }
    @Test public void loginRotatesSessionAndClearsPreviousRole()throws Exception{
        MockHttpSession old=new MockHttpSession();old.setAttribute("adminUser","admin");String id=old.getId();
        MvcResult result=mvc.perform(post("/login").session(old).with(csrf()).param("username","1").param("password","test-password-2026")).andExpect(status().is3xxRedirection()).andReturn();
        HttpSession session=result.getRequest().getSession(false);
        Assert.assertNotEquals(id,session.getId());Assert.assertNull(session.getAttribute("adminUser"));Assert.assertEquals(1,session.getAttribute("visitorId"));
    }
    @Test public void unknownEndpointsFailClosedAndLogoutRequiresToken()throws Exception{
        mvc.perform(get("/unregistered").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/logout.action").with(user("1").roles("EMPLOYEE"))).andExpect(status().isForbidden());
        mvc.perform(post("/logout.action").with(user("1").roles("EMPLOYEE")).with(csrf())).andExpect(status().is3xxRedirection());
    }
    @Test public void loginRateLimitIsBoundedAndExpires(){
        LoginRateLimitFilter filter=new LoginRateLimitFilter();
        for(int i=0;i<10;i++)Assert.assertTrue(filter.allow("127.0.0.1",1000));
        Assert.assertFalse(filter.allow("127.0.0.1",1001));
        Assert.assertTrue(filter.allow("127.0.0.1",61000));
    }
    @Test public void plaintextPasswordRecordsAreRejected(){
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq(1))).thenReturn(java.util.Collections.singletonList(java.util.Collections.singletonMap("password",(Object)"123456")));
        Assert.assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,()->new OfficeUserDetailsService(jdbc).loadUserByUsername("1"));
    }
}
