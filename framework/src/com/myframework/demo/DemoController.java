package com.myframework.demo;

import com.myframework.annotations.WebRoute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

public class DemoController {

    @WebRoute("/")
    public String home() {
        return "DemoController: home";
    }

    @WebRoute("/hello")
    public void hello(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("Hello from DemoController!");
    }

    @WebRoute(value = "/time", method = "GET")
    public void time(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("Time now: " + LocalDateTime.now());
    }
}
