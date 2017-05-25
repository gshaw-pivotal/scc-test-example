package io.pivotal.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectsController {

    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public String getProjects() {
        return "[{\"pid\": 555, \"project_name\": \"project_1\"}, {\"pid\": 888, \"project_name\": \"project_2\"}]";
    }

    @RequestMapping(value = "/projects/{projectid}", method = RequestMethod.GET)
    public String getUser(@PathVariable String projectid) {
        return "{\"pid\": " + projectid + ", \"project_name\": \"weird_project\"}";
    }
}
