package com.claropr.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
public class ObjectStorageService {

    @Value("${object.storage.type:MINIO}")
    private String storageType;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.access-key:admin}")
    private String minioAccessKey;

    @Value("${minio.secret-key:password123}")
    private String minioSecretKey;

    @Value("${minio.bucket-name:contracts-signed-pdfs}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            System.out.println("=== ObjectStorageService.init ===");
            System.out.println("Storage Type: " + storageType);
            System.out.println("MinIO Endpoint: " + minioEndpoint);
            System.out.println("Bucket Name: " + bucketName);

            if ("MINIO".equalsIgnoreCase(storageType)) {
                minioClient = MinioClient.builder()
                        .endpoint(minioEndpoint)
                        .credentials(minioAccessKey, minioSecretKey)
                        .build();

                // Verificar conexión y crear bucket si no existe
                ensureBucketExists();
                System.out.println("✅ MinIO client inicializado correctamente");
            }
        } catch (Exception e) {
            System.err.println("❌ Error inicializando ObjectStorageService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!bucketExists) {
            minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            System.out.println("✅ Bucket '" + bucketName + "' creado exitosamente");
        } else {
            System.out.println("✅ Bucket '" + bucketName + "' ya existe");
        }
    }

    /**
     * Sube un PDF firmado a Object Storage
     */
    public ObjectStorageResult uploadSignedPdf(String contractUid, byte[] pdfBytes) throws Exception {
        try {
            System.out.println("=== ObjectStorageService.uploadSignedPdf ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("PDF Size: " + pdfBytes.length + " bytes");

            // Generar nombre único para el archivo
            String fileName = generateFileName(contractUid);
            String objectPath = "signed-pdfs/" + fileName;

            // Calcular SHA256
            String sha256 = calculateSHA256(pdfBytes);

            // Subir a MinIO
            if ("MINIO".equalsIgnoreCase(storageType)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectPath)
                                .stream(new ByteArrayInputStream(pdfBytes), pdfBytes.length, -1)
                                .contentType("application/pdf")
                                .build()
                );

                String objectUri = minioEndpoint + "/" + bucketName + "/" + objectPath;
                
                System.out.println("✅ PDF subido exitosamente:");
                System.out.println("   URI: " + objectUri);
                System.out.println("   SHA256: " + sha256);

                return new ObjectStorageResult(objectUri, sha256, pdfBytes.length, "SUCCESS", null);
            } else {
                throw new UnsupportedOperationException("Storage type not supported: " + storageType);
            }

        } catch (Exception e) {
            System.err.println("❌ Error subiendo PDF: " + e.getMessage());
            e.printStackTrace();
            return new ObjectStorageResult(null, null, 0, "ERROR", e.getMessage());
        }
    }

    /**
     * Descarga un PDF firmado desde Object Storage
     */
    public byte[] downloadSignedPdf(String objectUri) throws Exception {
        try {
            System.out.println("=== ObjectStorageService.downloadSignedPdf ===");
            System.out.println("Object URI: " + objectUri);
            System.out.println("Storage Type: " + storageType);
            System.out.println("Bucket Name: " + bucketName);

            if ("MINIO".equalsIgnoreCase(storageType)) {
                // Extraer object path de la URI
                String objectPath = extractObjectPath(objectUri);
                System.out.println("Extracted Object Path: " + objectPath);
                
                // Verificar si el objeto existe
                try {
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectPath)
                                    .build()
                    );
                    System.out.println("✅ Objeto existe en MinIO");
                } catch (Exception e) {
                    System.err.println("❌ Objeto no existe en MinIO: " + e.getMessage());
                    throw e;
                }
                
                InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectPath)
                                .build()
                );

                // Leer todos los bytes del InputStream (compatible con Java 8)
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                int totalRead = 0;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                    totalRead += nRead;
                    System.out.println("   Leyendo chunk: " + nRead + " bytes (total: " + totalRead + ")");
                }
                buffer.flush();
                byte[] pdfBytes = buffer.toByteArray();
                inputStream.close();

                System.out.println("✅ PDF descargado exitosamente:");
                System.out.println("   Size: " + pdfBytes.length + " bytes");
                System.out.println("   First 20 bytes: " + java.util.Arrays.toString(java.util.Arrays.copyOfRange(pdfBytes, 0, Math.min(20, pdfBytes.length))));

                return pdfBytes;
            } else {
                throw new UnsupportedOperationException("Storage type not supported: " + storageType);
            }

        } catch (Exception e) {
            System.err.println("❌ Error descargando PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Verifica si un PDF existe en Object Storage
     */
    public boolean pdfExists(String objectUri) {
        try {
            if ("MINIO".equalsIgnoreCase(storageType)) {
                String objectPath = extractObjectPath(objectUri);
                
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectPath)
                                .build()
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Elimina un PDF de Object Storage
     */
    public boolean deletePdf(String objectUri) {
        try {
            if ("MINIO".equalsIgnoreCase(storageType)) {
                String objectPath = extractObjectPath(objectUri);
                
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectPath)
                                .build()
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error eliminando PDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera una URL firmada para descarga (placeholder)
     */
    public String generateSignedUrl(String objectUri, int expirationMinutes) {
        // En una implementación real, aquí se generaría una URL firmada
        // Por ahora, retornamos la URI original
        return objectUri + "?expires=" + (System.currentTimeMillis() + (expirationMinutes * 60 * 1000));
    }

    private String generateFileName(String contractUid) {
        return "contract_" + contractUid + "_" + UUID.randomUUID().toString() + ".pdf";
    }

    private String extractObjectPath(String objectUri) {
        // Extraer el path del objeto de la URI completa
        // Ejemplo: http://localhost:9000/contracts-signed-pdfs/signed-pdfs/contract_123.pdf
        // Resultado: signed-pdfs/contract_123.pdf
        String[] parts = objectUri.split("/" + bucketName + "/");
        return parts.length > 1 ? parts[1] : objectUri;
    }

    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Clase para encapsular el resultado de operaciones de Object Storage
     */
    public static class ObjectStorageResult {
        private String objectUri;
        private String sha256;
        private long fileSize;
        private String status;
        private String errorMessage;

        public ObjectStorageResult(String objectUri, String sha256, long fileSize, String status, String errorMessage) {
            this.objectUri = objectUri;
            this.sha256 = sha256;
            this.fileSize = fileSize;
            this.status = status;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getObjectUri() { return objectUri; }
        public String getSha256() { return sha256; }
        public long getFileSize() { return fileSize; }
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }
}
