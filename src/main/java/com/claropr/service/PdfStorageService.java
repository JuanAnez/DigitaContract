package com.claropr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class PdfStorageService {

    @Value("${pdf.storage.path:/tmp/contracts}")
    private String storagePath;

    /**
     * Guarda un PDF firmado en el sistema de archivos
     * @param contractUid UID del contrato
     * @param pdfBytes Contenido del PDF
     * @return Información del archivo guardado
     */
    public PdfFileInfo saveSignedPdf(String contractUid, byte[] pdfBytes) throws IOException {
        try {
            // Crear directorio si no existe
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            // Generar nombre único para el archivo
            String fileName = "contract_" + contractUid + "_" + UUID.randomUUID().toString() + ".pdf";
            Path filePath = storageDir.resolve(fileName);

            // Guardar el archivo
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(pdfBytes);
            }

            // Calcular SHA256
            String sha256 = calculateSHA256(pdfBytes);

            // Crear información del archivo
            PdfFileInfo fileInfo = new PdfFileInfo();
            fileInfo.setFilePath(filePath.toString());
            fileInfo.setFileName(fileName);
            fileInfo.setFileSize(pdfBytes.length);
            fileInfo.setSha256(sha256);
            fileInfo.setGeneratedAt(new Timestamp(System.currentTimeMillis()));

            System.out.println("PDF guardado exitosamente:");
            System.out.println("- Ruta: " + filePath.toString());
            System.out.println("- Tamaño: " + pdfBytes.length + " bytes");
            System.out.println("- SHA256: " + sha256);

            return fileInfo;

        } catch (Exception e) {
            System.err.println("Error guardando PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error guardando PDF firmado", e);
        }
    }

    /**
     * Obtiene el contenido de un PDF firmado guardado
     * @param filePath Ruta del archivo
     * @return Contenido del PDF
     */
    public byte[] getSignedPdf(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("PDF firmado no encontrado: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    /**
     * Verifica si existe un PDF firmado
     * @param filePath Ruta del archivo
     * @return true si existe
     */
    public boolean existsSignedPdf(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Elimina un PDF firmado
     * @param filePath Ruta del archivo
     * @return true si se eliminó exitosamente
     */
    public boolean deleteSignedPdf(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error eliminando PDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calcula el SHA256 de un array de bytes
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /**
     * Clase para información del archivo PDF
     */
    public static class PdfFileInfo {
        private String filePath;
        private String fileName;
        private long fileSize;
        private String sha256;
        private Timestamp generatedAt;

        // Getters y Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }

        public Timestamp getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(Timestamp generatedAt) { this.generatedAt = generatedAt; }
    }
}


