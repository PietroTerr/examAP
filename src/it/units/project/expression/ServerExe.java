package it.units.project.expression;

import it.units.project.expression.StringProcessor;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ServerExe {
    private final int port;
    private final String quitCommand;
    private final Function<String, String> stringProcessing;
    private final ExecutorService executorService;

    public ServerExe(int port, String quitCommand, Function<String, String> stringProcessing, int concurrentClients) {
        this.port = port;
        this.quitCommand = quitCommand;
        this.stringProcessing = stringProcessing;
        this.executorService = Executors.newFixedThreadPool(concurrentClients);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    executorService.submit(() -> handleClient(socket));
                } catch (IOException e) {
                    System.err.println("Error accepting client connection");
                    e.printStackTrace();
                }
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void handleClient(Socket socket) {
        try (socket) {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write("You are connected to the server.\n");
            bw.flush();
            while (true) {
                String command = br.readLine();
                if (command == null) {
                    System.err.println("Client closed connection...");
                    System.out.println("Closed connection with client: " + socket.getInetAddress());
                    break;
                }
                if (command.equals(quitCommand)) {
                    System.out.println("Quitting connection...");
                    break;
                }
                try {
                    String response = stringProcessing.apply(command);
                    bw.write(response + "\n");
                    bw.flush();
                } catch (Exception e) {
                    System.err.println("Error processing command: " + e.getMessage());
                    e.printStackTrace();
                    bw.write("ERR; processing error\n");
                    bw.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 12345; // example port
        String quitCommand = "BYE";
        Function<String, String> stringProcessor = new StringProcessor();
        int concurrentClients = 10; // example number of concurrent clients

        ServerExe server = new ServerExe(port, quitCommand, stringProcessor, concurrentClients);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Error starting server");
            e.printStackTrace();
        }
    }
}
