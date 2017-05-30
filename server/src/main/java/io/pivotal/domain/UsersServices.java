package io.pivotal.domain;

import io.pivotal.domain.models.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UsersServices {

    public List<User> getUsers() {
        return Arrays.asList(
            User.builder().id(Integer.valueOf(1234).intValue()).name("thename").build(),
            User.builder().id(Integer.valueOf(4567).intValue()).name("anothername").build()
        );
    }

    public User getUser(String userid) {
        if (userid.equals("1")) {
            return null;
        }
        return User.builder().id(Integer.valueOf(userid).intValue()).name("a_single_user").build();
    }
}
