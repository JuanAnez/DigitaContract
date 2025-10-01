-- =====================================================
-- ACTUALIZACIÓN DEL ESQUEMA PARA OBJECT STORAGE
-- =====================================================
-- Este script modifica la tabla CONTRACTS para usar Object Storage
-- en lugar de guardar PDFs directamente en la base de datos

-- 1. Agregar columnas para Object Storage
ALTER TABLE IC_ADMIN.CONTRACTS ADD (
    SIGNED_PDF_URI        VARCHAR2(500),  -- URL del PDF en Object Storage
    SIGNED_PDF_SHA256     VARCHAR2(64),   -- Hash SHA256 para integridad
    PDF_STORAGE_TYPE      VARCHAR2(20)    -- 'MINIO', 'S3', 'ORACLE_OBJECT_STORAGE'
);

-- 2. Agregar comentarios para documentación
COMMENT ON COLUMN IC_ADMIN.CONTRACTS.SIGNED_PDF_URI IS 'URI del PDF firmado en Object Storage (MinIO/S3/Oracle)';
COMMENT ON COLUMN IC_ADMIN.CONTRACTS.SIGNED_PDF_SHA256 IS 'Hash SHA256 del PDF para verificar integridad';
COMMENT ON COLUMN IC_ADMIN.CONTRACTS.PDF_STORAGE_TYPE IS 'Tipo de Object Storage usado: MINIO, S3, ORACLE_OBJECT_STORAGE';

-- 3. Crear tabla para auditoría de Object Storage (opcional)
CREATE TABLE IC_ADMIN.OBJECT_STORAGE_AUDIT (
    ID              RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    CONTRACT_ID     NUMBER(19) NOT NULL REFERENCES IC_ADMIN.CONTRACTS(ID) ON DELETE CASCADE,
    OPERATION       VARCHAR2(20) NOT NULL,  -- 'UPLOAD', 'DOWNLOAD', 'DELETE'
    STORAGE_TYPE    VARCHAR2(20) NOT NULL,  -- 'MINIO', 'S3', 'ORACLE_OBJECT_STORAGE'
    OBJECT_URI      VARCHAR2(500) NOT NULL,
    FILE_SIZE       NUMBER(19),
    SHA256_HASH     VARCHAR2(64),
    OPERATED_BY     VARCHAR2(100),
    OPERATED_AT     TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    SUCCESS         CHAR(1) DEFAULT 'Y' CHECK (SUCCESS IN ('Y', 'N')),
    ERROR_MESSAGE   VARCHAR2(1000)
);

-- 4. Crear índices para performance
CREATE INDEX IDX_CONTRACTS_PDF_URI ON IC_ADMIN.CONTRACTS(SIGNED_PDF_URI);
CREATE INDEX IDX_CONTRACTS_STORAGE_TYPE ON IC_ADMIN.CONTRACTS(PDF_STORAGE_TYPE);
CREATE INDEX IDX_OBJ_AUDIT_CONTRACT ON IC_ADMIN.OBJECT_STORAGE_AUDIT(CONTRACT_ID);
CREATE INDEX IDX_OBJ_AUDIT_OPERATION ON IC_ADMIN.OBJECT_STORAGE_AUDIT(OPERATION, OPERATED_AT);

-- 5. Crear trigger para auditoría automática
CREATE OR REPLACE TRIGGER TRG_CONTRACTS_OBJ_AUDIT
    AFTER UPDATE OF SIGNED_PDF_URI, SIGNED_PDF_SHA256, PDF_STORAGE_TYPE
    ON IC_ADMIN.CONTRACTS
    FOR EACH ROW
BEGIN
    -- Solo auditar si hay cambios en los campos de Object Storage
    IF (:NEW.SIGNED_PDF_URI IS NOT NULL AND 
        (:OLD.SIGNED_PDF_URI IS NULL OR :OLD.SIGNED_PDF_URI != :NEW.SIGNED_PDF_URI)) THEN
        
        INSERT INTO IC_ADMIN.OBJECT_STORAGE_AUDIT (
            CONTRACT_ID, OPERATION, STORAGE_TYPE, OBJECT_URI, 
            SHA256_HASH, OPERATED_BY, SUCCESS
        ) VALUES (
            :NEW.ID, 'UPLOAD', :NEW.PDF_STORAGE_TYPE, :NEW.SIGNED_PDF_URI,
            :NEW.SIGNED_PDF_SHA256, USER, 'Y'
        );
    END IF;
END;
/

-- 6. Crear vista para consultas de contratos con Object Storage
CREATE OR REPLACE VIEW IC_ADMIN.V_CONTRACTS_STORAGE AS
SELECT 
    c.ID,
    c.CONTRACT_UID,
    c.CONTRACT_TYPE,
    c.STATUS,
    c.CREATED_AT,
    c.SIGNED_PDF_URI,
    c.SIGNED_PDF_SHA256,
    c.PDF_STORAGE_TYPE,
    CASE 
        WHEN c.SIGNED_PDF_URI IS NOT NULL THEN 'PDF_AVAILABLE'
        ELSE 'NO_PDF'
    END AS PDF_STATUS,
    CASE 
        WHEN c.PDF_STORAGE_TYPE = 'MINIO' THEN 'http://localhost:9000'
        WHEN c.PDF_STORAGE_TYPE = 'S3' THEN 'https://s3.amazonaws.com'
        WHEN c.PDF_STORAGE_TYPE = 'ORACLE_OBJECT_STORAGE' THEN 'https://objectstorage.oraclecloud.com'
        ELSE NULL
    END AS STORAGE_ENDPOINT
FROM IC_ADMIN.CONTRACTS c;

-- 7. Crear función para generar URL firmada (placeholder)
CREATE OR REPLACE FUNCTION IC_ADMIN.GENERATE_SIGNED_URL(
    p_contract_id RAW,
    p_expiration_minutes NUMBER DEFAULT 60
) RETURN VARCHAR2
IS
    v_uri VARCHAR2(500);
    v_signed_url VARCHAR2(1000);
BEGIN
    -- Obtener URI del contrato
    SELECT SIGNED_PDF_URI INTO v_uri
    FROM IC_ADMIN.CONTRACTS
    WHERE ID = p_contract_id;
    
    IF v_uri IS NULL THEN
        RETURN NULL;
    END IF;
    
    -- En una implementación real, aquí se generaría la URL firmada
    -- Por ahora, retornamos la URI original
    v_signed_url := v_uri || '?expires=' || (SYSTIMESTAMP + INTERVAL p_expiration_minutes MINUTE);
    
    RETURN v_signed_url;
END;
/

-- 8. Crear procedimiento para limpiar PDFs antiguos (opcional)
CREATE OR REPLACE PROCEDURE IC_ADMIN.CLEANUP_OLD_PDFS(
    p_days_old NUMBER DEFAULT 365
)
IS
BEGIN
    -- Marcar contratos antiguos para limpieza
    UPDATE IC_ADMIN.CONTRACTS 
    SET SIGNED_PDF_URI = NULL,
        SIGNED_PDF_SHA256 = NULL,
        PDF_STORAGE_TYPE = NULL
    WHERE CREATED_AT < (SYSTIMESTAMP - INTERVAL p_days_old DAY)
    AND STATUS = 'ARCHIVED';
    
    COMMIT;
END;
/

-- 9. Insertar datos de configuración
INSERT INTO IC_ADMIN.ICC_LOV (LOV_KEY, LOV_DESCRIPTION, LOV_VALUE, CREATED_AT) 
VALUES ('OBJECT_STORAGE_TYPE', 'Tipo de Object Storage por defecto', 'MINIO', SYSTIMESTAMP)
ON DUPLICATE KEY UPDATE LOV_VALUE = 'MINIO';

INSERT INTO IC_ADMIN.ICC_LOV (LOV_KEY, LOV_DESCRIPTION, LOV_VALUE, CREATED_AT) 
VALUES ('MINIO_ENDPOINT', 'Endpoint de MinIO', 'http://localhost:9000', SYSTIMESTAMP)
ON DUPLICATE KEY UPDATE LOV_VALUE = 'http://localhost:9000';

INSERT INTO IC_ADMIN.ICC_LOV (LOV_KEY, LOV_DESCRIPTION, LOV_VALUE, CREATED_AT) 
VALUES ('MINIO_BUCKET', 'Bucket por defecto para PDFs', 'contracts-signed-pdfs', SYSTIMESTAMP)
ON DUPLICATE KEY UPDATE LOV_VALUE = 'contracts-signed-pdfs';

COMMIT;

-- 10. Verificar la instalación
SELECT 'SCHEMA_UPDATE_COMPLETED' AS STATUS FROM DUAL;

-- Mostrar información de la configuración
SELECT 
    'CONTRACTS_TABLE_UPDATED' AS STATUS,
    COUNT(*) AS TOTAL_CONTRACTS
FROM IC_ADMIN.CONTRACTS;

SELECT 
    'OBJECT_STORAGE_CONFIG' AS CONFIG_TYPE,
    LOV_KEY,
    LOV_VALUE
FROM IC_ADMIN.ICC_LOV 
WHERE LOV_KEY IN ('OBJECT_STORAGE_TYPE', 'MINIO_ENDPOINT', 'MINIO_BUCKET');

