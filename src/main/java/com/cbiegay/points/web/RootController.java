package com.cbiegay.points.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {
    /**
     * Shows a message for the root URI of the web app, in case it is opened in a browser.
     */
    @GetMapping
    public @ResponseBody String home() {
        return "See README.md for instructions on using this app.";
    }
}
