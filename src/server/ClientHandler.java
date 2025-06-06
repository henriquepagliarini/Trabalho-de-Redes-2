package server;

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientAddress;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        System.out.println("Cliente (" + clientAddress + ") conectado.");
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("sair")) {
                    System.out.println("Cliente (" + clientAddress + ") desconectou.");
                    break;
                }

                if (clientMessage.startsWith("Chat ")) {
                    receiveMessage(clientMessage);
                } else if (clientMessage.startsWith("Arquivo ")) {
                    System.out.println("ar qui vo");
                } else {
                    System.out.println("(" + clientAddress + "): " + clientMessage + " - Requisicao Invalida!");
                    sendMessage("Requisicao '" + clientMessage + "' Invalida!");
                }
            }
        } catch (IOException e) {
            System.out.println("Conexão com o cliente (" + clientAddress + ") perdida: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                ServerMain.removeClient(this);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void receiveMessage(String message) {
        System.out.println("(" + clientAddress + "): " + message.substring(5));
    }

    protected void sendMessage(String message) {
        output.println("Servidor: " + message);
    }

}
