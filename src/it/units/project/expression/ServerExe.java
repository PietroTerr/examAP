package it.units.project.expression;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class ServerExe {
    private final int port;
    private final String quitCommand;
    private final Function<String, String> stringProcessing;
    private final ExecutorService executorService;
    private final ServerStatistics statistics;

    public ServerExe(int port, String quitCommand, Function<String, String> stringProcessing, int concurrentClients) {
        this.port = port;
        this.quitCommand = quitCommand;
        this.stringProcessing = stringProcessing;
        this.executorService = Executors.newFixedThreadPool(concurrentClients);
        this.statistics = new ServerStatistics();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    executorService.submit(() -> {
                        try (socket) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            while (true) {
                                String command = br.readLine();
                                if (command == null) {
                                    System.err.println("Client closed connection...");
                                    break;
                                }
                                if (command.equals(quitCommand)) {
                                    System.out.println("Quitting connection...");
                                    break;
                                }

                                long startTime = System.currentTimeMillis();
                                String result = stringProcessing.apply(command);
                                long finishTime = System.currentTimeMillis();
                                long responseTime = finishTime - startTime;
                                statistics.recordResponse(responseTime);

                                if (result != null) {
                                    if (command.startsWith("STAT_")) {
                                        switch (command) {
                                            case "STAT_REQS":
                                                result = "OK;" + statistics.getTotalResponses();
                                                break;
                                            case "STAT_AVG_TIME":
                                                result = "OK;" + statistics.getAverageResponseTime();
                                                break;
                                            case "STAT_MAX_TIME":
                                                result = "OK;" + statistics.getMaxResponseTime();
                                                break;
                                        }
                                    }
                                    bw.write(result + System.lineSeparator());
                                    bw.flush();
                                }
                            }
                        } catch (IOException e) {
                            System.err.printf("IO error: %s", e);
                        }
                    });
                } catch (IOException e) {
                    System.err.printf("Cannot connect error, due to %s", e);
                }
            }
        } finally {
            executorService.shutdown();
        }
    }




    public static void main(String[] args) throws IOException {
        Function<String, String> stringProcessor = new StringProcessor();
        ServerExe server = new ServerExe(8080, "BYE", stringProcessor, 10);
        server.start();
    } //C'è UN PROBLEMA CON TELNET
}