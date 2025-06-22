package org.example;

import org.example.thread.ReadMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String userName = "Аноним";

    public static String getUserName() {
        return userName;
    }


    public static void setUserName(String userName) {
        if (!userName.trim().isEmpty())
            Client.userName = userName;
    }


    public static void main(String[] args) {
        String serverHost = null;
        int serverPort = 0;

        try (BufferedReader bf = new BufferedReader(
                new FileReader("src/main/resources/settings.txt"))) {
            String setting;
            while ((setting = bf.readLine()) != null) {
                if (setting.contains("host")) {
                    String[] s = setting.split(" ");
                    serverHost = s[1];
                } else if (setting.contains("port")) {
                    String[] s = setting.split(" ");
                    serverPort = Integer.parseInt(s[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Socket socketClient = new Socket(serverHost, serverPort);
             PrintWriter printWriter = new PrintWriter(socketClient.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()))) {

            System.out.println("Клиент, подключенный к socketclient");
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nВведите свое имя, чтобы присоединиться к серверу!\n" +
                    "Затем введите сообщения для отправки пользователям или exit, чтобы выйти из канала: CLIENT");

            setUserName(scanner.nextLine());
            printWriter.println(getUserName());

            Thread.sleep(1000);
            ReadMessage send = new ReadMessage();
            send.start();

            while (true) {
                String msg = scanner.nextLine();
                printWriter.println(msg);
                if (msg.equalsIgnoreCase("exit")) {
                    Thread.sleep(100);
                    if (in.read() > -1) {
                        msgFromServer(in);
                    }
                    break;
                }
                if (in.read() > -1) {
                    msgFromServer(in);
                }
            }
            send.interrupt();
            System.out.println("Закрытие канала подключения клиента.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void msgFromServer(BufferedReader in) throws IOException {
        String msgServ = in.readLine();
    }
}
