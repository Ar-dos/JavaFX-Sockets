package org.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class Main {
    private void run(String[] args) throws IOException {
            Server server = new Server();
            server.setPort(8081);
            server.run();
    }

    public static void main(String[] args) throws IOException {
        new org.example.Main().run(args);
    }
}