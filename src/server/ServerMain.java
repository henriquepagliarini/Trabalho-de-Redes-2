package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Scanner;
import java.io.IOException;

public class ServerMain {
    private static final int PORT = 6666;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        // Inicializa o servidor
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT + ". Aguardando conexões...");

            // Thread para enviar mensagens para todos os clientes
            new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        System.out.print("Chat: ");
                        String command = scanner.nextLine();
                        sendMessageToAllClients(command);
                    }
                }
            }).start();

            // Espera a conexão de um cliente
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.run();
            }
        } catch (IOException e) {
            System.err.println("Erro ao abrir o servidor: " + e.getMessage());
        }
    }

    private static void sendMessageToAllClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    protected static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}