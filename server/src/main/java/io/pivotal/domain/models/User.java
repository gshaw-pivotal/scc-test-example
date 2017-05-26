package io.pivotal.domain.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class User {

    private int id;
    private String name;
}
