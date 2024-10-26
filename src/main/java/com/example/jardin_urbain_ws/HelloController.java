package com.example.jardin_urbain_ws;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

// import org.apache.jena.rdf.model.Model;
// import org.apache.jena.rdf.model.ModelFactory;

@RestController
public class HelloController {

    @RequestMapping("hello")
    public String hello() {

        return "0-4";

    }

}
