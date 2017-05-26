package io.pivotal.domain;

import org.springframework.stereotype.Component;

@Component
public class UsersServices {

    public String getUsers() {
        return "[{\"id\": 1234, \"name\": \"thename\"}, {\"id\": 4567, \"name\": \"anothername\"}]";
    }

    public String getUser(String userid) {
        if (userid.equals("1")) {
            return "{\"error\": \"No user found\"}";
        }
        return "{\"id\": " + userid + ", \"name\": \"a_single_user\"}";
    }
}
