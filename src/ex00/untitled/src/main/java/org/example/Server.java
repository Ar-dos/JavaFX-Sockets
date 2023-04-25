package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class Server {
    private static final String SIGN_UP_COMMAND = "signUp";
    private static final String SIGN_IN_COMMAND = "signIn";
    private static final String EXIT_COMMAND = "Exit";
    private int port;

    private Client player1 = null;
    private Client player2 = null;

    public void setPort(int port) {
        this.port = port;
    }

    private List<Client> clients;

    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.err.println("Server started, waiting for a client...");
            clients = new ArrayList<>();

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = serverSocket.accept();
                Client client = null;
                try {
                    System.err.println("Got new TCP connection from client");
                    if (player1 == null) {
                        player1 = new Client(socket);
                        System.err.println("Player one ready!");
                    } else if (player2 == null) {
                        player2 = new Client(socket);
                        player1.setEnemy(player2);
                        player2.setEnemy(player1);
                        Thread player1Thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    player1.run();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        Thread player2Thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    player2.run();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        player1Thread.start();
                        player2Thread.start();
                        player1.getWriter().write(ByteBuffer.allocate(4).putInt(777).array());
                        player2.getWriter().write(ByteBuffer.allocate(4).putInt(777).array());
                    }
                } catch (ExitException ignored) {
                    System.err.println("Client has been exited [user="
                            + (client == null ? null : client) + "].");
                } catch (IOException ignored) {
                    System.err.println("Client disconnected with error [user="
                            + (client == null ? null : client) + "].");
                }
            }
        }

    }


    public final class Client {
        private final Socket socket;
        private final BufferedInputStream reader;
        private final BufferedOutputStream writer;
        private Client enemy;

        public void setEnemy(Client enemy) {
            this.enemy = enemy;
        }

        public BufferedInputStream getReader() {
            return reader;
        }

        public BufferedOutputStream getWriter() {
            return writer;
        }

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            reader = new BufferedInputStream(socket.getInputStream());
            writer = new BufferedOutputStream(socket.getOutputStream());
        }

        public void run() throws IOException {
            try {
                while (true) {
                    int line = readNonEmpty();
                    System.out.println(line);
                    enemy.getWriter().write(ByteBuffer.allocate(4).putInt(line).array());
                    enemy.getWriter().flush();
                }
            } finally {
                close();
            }
        }

        public void close() throws IOException {
            reader.close();
            writer.close();
            socket.close();
        }

        private int readNonEmpty() throws IOException {
            while (true) {
                byte[] arr = new byte[Integer.BYTES];
                reader.read(arr);
                int line = 0;
                for (byte b : arr) {
                    line = (line << 8) + (b & 0xFF);
                }
                return line;

            }
        }

    }
}
