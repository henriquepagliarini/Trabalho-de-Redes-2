package client;

import java.util.Scanner;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;

public class ClientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("IP do servidor (ex: localhost): ");
        String host = scanner.nextLine();
        System.out.print("Porta do servidor (ex: 6666): ");
        int port = scanner.nextInt();
        scanner.nextLine();

        // Conecta ao servidor
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Conectado ao servidor: " + host + ", " + port);
            System.out.println("'Chat [mensagem]' para mensagens");
            System.out.println("'Arquivo [nome]' para solicitar um arquivo.");
            System.out.println("'Sair' para encerrar a conexão.");

            // Inicia as streams
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Thread para receber mensagens
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = input.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão com o servidor perdida.");
                }
            }).start();

            while (true) {
                String userInput = scanner.nextLine();
                output.println(userInput);

                if (userInput.toLowerCase().startsWith("sair")) {
                    System.out.println("Desconectado do servidor");
                    scanner.close();
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }
}