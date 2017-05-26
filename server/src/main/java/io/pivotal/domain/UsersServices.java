package io.pivotal.domain;

import io.pivotal.domain.models.User;
import org.springframework.stereotype.Component;

@Component
public class UsersServices {

    public String getUsers() {
        return "[{\"id\": 1234, \"name\": \"thename\"}, {\"id\": 4567, \"name\": \"anothername\"}]";
    }

    public Object getUser(String userid) {
        if (userid.equals("1")) {
            return "{\"error\": \"No user found\"}";
        }
        return User.builder().id(Integer.valueOf(userid).intValue()).name("a_single_user").build();
    }
}
