package io.loli.nekocat.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class NekoCatTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(NekoCatTestApplication.class, args);
    }

}
