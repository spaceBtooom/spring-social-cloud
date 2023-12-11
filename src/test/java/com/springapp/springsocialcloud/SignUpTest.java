package com.springapp.springsocialcloud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class SignUpTest {

    @Autowired
    MockMvc mockMvc;
    @Test
    public void givenUserNameAndPass_whenCorrectInput_thenSuccess() throws Exception {
        this.mockMvc.perform(get("/user/signup")
                .param("username","")
                .param("password",""))
                .andExpect(status().is3xxRedirection());
    }
}
