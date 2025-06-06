package client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileReceiver {
    public static File receiveFile(DataInputStream dataInput, String fileName, long fileSize, String expectedHash,
            String saveDir) throws IOException {
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(dir, fileName);

        try (FileOutputStream fileOutput = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize) {
                int toRead = (int) Math.min(buffer.length, fileSize - totalBytesRead);
                int bytesRead = dataInput.read(buffer, 0, toRead);

                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
                fileOutput.write(buffer, 0, bytesRead);
            }
        }

        String hash = calculateSHA256(outputFile);
        if (!hash.equals(expectedHash)) {
            throw new IOException("Hash incorreto.");
        }

        return outputFile;
    }

    private static String calculateSHA256(File file) throws IOException {
        try (DigestInputStream dis = new DigestInputStream(
                new FileInputStream(file),
                MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[4096];

            while (dis.read(buffer) != -1) {
                // apenas lê
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
