package client;

import java.util.Scanner;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ClientMain {
    volatile static boolean isReceivingFile = false;

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
            System.out.println("'Chat [mensagem]' para mensagens.");
            System.out.println("'Arquivo [nome]' para solicitar um arquivo.");
            System.out.println("'Sair' para encerrar a conexão.");

            // Inicia as streams
            DataInputStream dataInput = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());

            // Thread para receber mensagens
            new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = dataInput.readUTF();

                        if (serverMessage.equals("OK")) {
                            String fileName = dataInput.readUTF();
                            long fileSize = dataInput.readLong();
                            String fileHash = dataInput.readUTF();

                            System.out.println("Arquivo '" + fileName + "' (" + fileSize + ") bytes encontrado.");

                            File receivedFile = FileReceiver.receiveFile(dataInput, fileName, fileSize, fileHash,
                                    "..\\client_downloads");
                            System.out.println("Arquivo recebido com sucesso: " + receivedFile.getPath());
                            continue;
                        }
                        System.out.println(serverMessage);
                    }
                } catch (Exception e) {
                    System.out.println("Deu algum erro...");
                }
            }).start();

            // Loop para enviar mensagens
            while (true) {
                String userInput = scanner.nextLine();
                dataOutput.writeUTF(userInput);
                dataOutput.flush();

                // Mensagem para sair
                if (userInput.toLowerCase().startsWith("sair")) {
                    System.out.println("Desconectado do servidor");
                    scanner.close();
                    break;
                }

                // Receber aquivo
                if (userInput.startsWith("Arquivo")) {
                    isReceivingFile = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }
}