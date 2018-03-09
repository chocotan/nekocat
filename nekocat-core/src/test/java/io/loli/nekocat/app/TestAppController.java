package io.loli.nekocat.app;

import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class TestAppController {

    @GetMapping("/")
    public String indexPage() {
        return "<html><a href='/get'>click</a></html>";
    }

    @GetMapping("/get")
    public String page() {
        return "hello";
    }


    @PostMapping("post")
    public String testPost(String name, HttpServletRequest request) {
        return request.getHeader("testheader") + name;
    }


}
