-- Actualizar contrato existente con información del PDF firmado
-- Este script actualiza el contrato CBRO-I-12345 con la información del PDF ya guardado

UPDATE IC_ADMIN.CONTRACTS 
SET 
    SIGNED_PDF_PATH = '/tmp/contracts/contract_CBRO-I-12345_8dbeeb72-4591-4d06-a761-a7a77bf4202d.pdf',
    PDF_GENERATED_AT = SYSTIMESTAMP,
    UPDATED_AT = SYSTIMESTAMP
WHERE CONTRACT_UID = 'CBRO-I-12345';

-- Verificar la actualización
SELECT 
    CONTRACT_UID, 
    SIGNED_PDF_PATH, 
    PDF_GENERATED_AT,
    STATUS,
    STATUS_MESSAGE
FROM IC_ADMIN.CONTRACTS 
WHERE CONTRACT_UID = 'CBRO-I-12345';
