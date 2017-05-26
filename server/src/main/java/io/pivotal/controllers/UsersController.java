package io.pivotal.controllers;

import io.pivotal.domain.UsersServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {

    private UsersServices usersServices;

    @Autowired
    public UsersController(UsersServices usersServices) {
        this.usersServices = usersServices;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getUsers() {
        return usersServices.getUsers();
    }

    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public String getUser(@PathVariable String userid) {
        return usersServices.getUser(userid);
    }
}