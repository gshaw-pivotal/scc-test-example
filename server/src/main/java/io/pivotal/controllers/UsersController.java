package io.pivotal.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUsers() {
        return "[{\"id\": 1234, \"name\": \"thename\"}, {\"id\": 4567, \"name\": \"anothername\"}]";
    }

    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public String getUser(@PathVariable String userid) {
        if (userid.equals("1")) {
            return "{\"error\": \"No user found\"}";
        }
        return "{\"id\": " + userid + ", \"name\": \"a_single_user\"}";
    }
}