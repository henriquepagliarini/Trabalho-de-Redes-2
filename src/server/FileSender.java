package server;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSender {
    public static boolean sendFile(File file, DataOutputStream dataOutput) {
        try (BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInput.read(buffer)) != -1) {
                dataOutput.write(buffer, 0, bytesRead);
            }
            dataOutput.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao enviar o arquivo: " + e.getMessage());
            return false;
        }
    }

    public static String calculateSHA256(File file) throws IOException {
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file),
                MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[4096];

            while (dis.read(buffer) != -1) {

            }

            MessageDigest messageDigest = dis.getMessageDigest();
            byte[] hashBytes = messageDigest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 não disponível.", e);
        }
    }
}
