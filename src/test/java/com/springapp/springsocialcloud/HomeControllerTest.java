package com.springapp.springsocialcloud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(TestSecurityConfiguration.class)
class HomeControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void authenticatedUserShouldDisplayHomePage() throws Exception {
        mockMvc
                .perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsAllowedToVisitHomePage() throws Exception {
        mockMvc
                .perform(get("/"))
                .andExpect(status().isOk());
    }
//    @Test
//    @WithAnonymousUser
//    void anonymousUserIsNotAllowedToSeeHomePage() throws Exception{
//        mockMvc.perform(get("/"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(header().stringValues("Location", "http://localhost/login"))
//        ;
//
//    }
}