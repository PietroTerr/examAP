package it.units.project.expression;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class ServerExe {
    private final int port;
    private final String quitCommand;
    private final Function<String, String> stringProcessing;
    private final ExecutorService executorService;

    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
    private int requestCount = 0;

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
                    System.out.println("New client : " + socket.getInetAddress());
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


                                //TODO SISTEMARE STAT REQUESTS, PROBABILMENTE SENZA LA CLASSE SERVERSTATISTICS

                                if (result != null) {
                                    if (command.startsWith("STAT_")) {
                                        switch (command) {
                                            case "STAT_REQS":
                                                result = "OK;" + requestCount;
                                                break;
                                            case "STAT_AVG_TIME":
                                                double sum = 0.0;
                                                for (Long requestTime : responseTimes) {
                                                    sum += requestTime;
                                                }
                                                result ="OK;" + sum / responseTimes.size();
                                                break;
                                            case "STAT_MAX_TIME":
                                                result = "OK;" + Collections.max(responseTimes);
                                                break;
                                        }
                                    }
                                    bw.write(result + System.lineSeparator());
                                    bw.flush();
                                    synchronized (this){
                                        requestCount++;
                                        responseTimes.add(responseTime);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.printf("ERR; IO error: %s", e);
                        }
                    });
                } catch (IOException e) {
                    System.err.printf("ERR; Cannot connect due to %s", e);
                }
            }
        } finally {
            executorService.shutdown();
        }
    }




    public static void main(String[] args) throws IOException {
        Function<String, String> stringProcessor = new StringProcessor();
        ServerExe server = new ServerExe(8080, "BYE", stringProcessor, 1);
        server.start();
    } //C'è UN PROBLEMA CON TELNET
}