package org.example;

import org.example.logger.ServerLog;
import org.example.thread.ClientHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final Integer PORT = 8080;
    public static final String HOST = "localhost\n";
    public static ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
    public static ServerLog LOGGER = ServerLog.getInstance();

    public Server(Integer port) {
    }

    public static void main(String[] args) {
        try (FileWriter fileWriter = new FileWriter("src/main/resources/settings.txt", false)) {
            fileWriter.write("host: " + HOST);
            fileWriter.write("port: " + String.valueOf(PORT));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Scanner sc = new Scanner(System.in)) {

            LOGGER.log("INFO", "Привет! Сервер активен," +
                    " Ожидание консольных команд или подключения пользователя.." +
                    " \n Чтобы выключить сервер - напишите exit");

            Thread readThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Thread.sleep(1000);
                        if (sc.hasNextLine()) {
                            System.out.println("Сервер нашел консольные команды!");
                            Thread.sleep(1000);
                            String serverCommand = sc.nextLine();
                            if (serverCommand.equalsIgnoreCase("exit")) {
                                System.out.println("Сервер инициализирует выход");
                                executorService.shutdown();
                                serverSocket.close();
                                LOGGER.log("INFO", "EXIT");
                                break;
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        return;
                    }
                }
            });
            readThread.start();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Есть подключение");
                executorService.execute(new ClientHandler(clientSocket, LOGGER));
                System.out.println("Подключение установлено");
            }
            System.out.println("Пытаемся выйти");
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
