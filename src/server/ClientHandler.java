package server;

import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientAddress;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private boolean isRunning = true;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        System.out.println("Cliente (" + clientAddress + ") conectado.");
        try {
            dataInput = new DataInputStream(clientSocket.getInputStream());
            dataOutput = new DataOutputStream(clientSocket.getOutputStream());

            // Espera mensagens do cliente
            String clientMessage;
            while (isRunning) {
                clientMessage = dataInput.readUTF();
                handleRequest(clientMessage);
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

    private void handleRequest(String request) throws IOException {
        // Requisição para sair, enviar mensagem ou pedir arquivo
        if (request.equalsIgnoreCase("sair")) {
            System.out.println("Cliente (" + clientAddress + ") desconectou.");
            isRunning = false;
        } else if (request.startsWith("Chat ")) {
            handleMessage(request);
        } else if (request.startsWith("Arquivo ")) {
            handleFileRequest(request.substring(8));
        } else {
            System.out.println("(" + clientAddress + "): " + request + " - Requisicao Invalida!");
            sendMessage("Requisicao '" + request + "' Invalida!");
        }
    }

    private void handleFileRequest(String fileName) throws IOException {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            dataOutput.writeUTF("ERRO_NOME_INVALIDO");
            return;
        }

        File file = new File("..\\server_files", fileName);
        file.getParentFile().mkdirs();

        if (file.exists() && file.isFile()) {
            // Envia as mensagens necessárias
            dataOutput.writeUTF("OK");
            dataOutput.writeUTF(file.getName());
            dataOutput.writeLong(file.length());

            // Cacula a hash
            String hash = FileSender.calculateSHA256(file);
            dataOutput.writeUTF(hash);

            // Envia o arquivo
            boolean sent = FileSender.sendFile(file, dataOutput);
            if (!sent) {
                dataOutput.writeUTF("ERRO_AO_ENVIAR");
            } else {
                System.out.println("(" + clientAddress + "): Arquivo '" + fileName + "' enviado com sucesso.");
            }
        } else {
            System.out.println("(" + clientAddress + "): Arquivo '" + fileName + "' nao encontrado.");
            dataOutput.writeUTF("ERRO_ARQUIVO_NAO_ENCONTRADO");
        }
    }

    private void handleMessage(String message) {
        System.out.println("(" + clientAddress + "): " + message.substring(5));
    }

    protected void sendMessage(String message) throws IOException {
        message = "Servidor: " + message;
        dataOutput.writeUTF(message);
    }
}
