package org.example.thread;

import org.example.logger.ClientLog;
import org.example.logger.ServerLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final ClientLog clientLog;
    private static Socket clientDialog;
    ServerLog LOGGER;

    public ClientHandler(Socket client, ServerLog LOGGER) {
        ClientHandler.clientDialog = client;
        this.LOGGER = LOGGER;
        clientLog = ClientLog.getInstance();
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(clientDialog.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientDialog.getInputStream()))) {
            System.out.println("Запись и чтение для приема и вывода создана");

            final String name = in.readLine() + "[" + clientDialog.getPort() + "]";
            LOGGER.log("Новый пользователь ", String.format(": %s", name));

            while (!clientDialog.isClosed()) {
                final String msg = in.readLine();
                System.out.println("Прочтите сообщение от " + name + "=>> " + msg);
                if (msg.equalsIgnoreCase("exit")) {
                    LOGGER.log("Выход из чата", String.format(": %s", name));
                    out.println("Сервер ожидает - " + msg + " - ОК");
                    Thread.sleep(1000);
                    break;
                }

                System.out.println("Сервер готов к записи....");
                String msgAndUser = name + " =>> " + msg;
                out.println(">>>" + msgAndUser + " - ОК");
                clientLog.log(name, msg);
                System.out.println("Сервер записал сообщение");
            }
            LOGGER.log("Закрытие канала с пользователем", String.format(": [%s]- выполнено", name));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
