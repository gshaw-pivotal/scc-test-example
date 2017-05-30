package io.pivotal.controllers;

import io.pivotal.domain.UsersServices;
import io.pivotal.domain.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsersController {

    private UsersServices usersServices;

    @Autowired
    public UsersController(UsersServices usersServices) {
        this.usersServices = usersServices;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers() {
        return buildResponse(usersServices.getUsers());
    }

    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public ResponseEntity getUser(@PathVariable String userid) {
        User returnedUser = usersServices.getUser(userid);

        if (returnedUser == null) {
            return buildResponse("{\"error\": \"No user found\"}");
        }

        return buildResponse(returnedUser);
    }

    private ResponseEntity buildResponse(Object responseBody) {
        return new ResponseEntity(
                responseBody, HttpStatus.OK
        );
    }
}