package com.codecool.dungeoncrawl.agent.reporter;

import java.io.IOException;
import java.net.Socket;

public class JacocoAgentTest {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 6300;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to JaCoCo agent on " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Failed to connect to JaCoCo agent on " + host + ":" + port);
            e.printStackTrace();
        }
    }
}
