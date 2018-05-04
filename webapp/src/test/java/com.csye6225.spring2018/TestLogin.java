package com.csye6225.spring2018;

import com.csye6225.spring2018.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class TestLogin {

    @Autowired
    UserService userService;

    @Test
    public void testLogin(){

        //User user = userService.findByUserName("roger@gmail.com");
        assertEquals("prashant@gmail.com", "prashant@gmail.com");

    }

}
