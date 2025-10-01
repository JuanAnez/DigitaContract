-- ======================================================================
--  VERIFICAR CONTRATOS EN LA BASE DE DATOS
--  OBJETIVO : Verificar que los contratos se están guardando correctamente
--  FECHA    : 2025-09-29
-- ======================================================================

-- Verificar todos los contratos
SELECT 
    ID,
    CONTRACT_UID,
    CONTRACT_TYPE,
    ACCOUNT_TYPE,
    BAN_NUMBER,
    SUBSCRIBER_NUMBER,
    STATUS,
    STATUS_MESSAGE,
    CREATED_AT,
    UPDATED_AT
FROM IC_ADMIN.CONTRACTS 
ORDER BY CREATED_AT DESC;

-- Verificar contratos por UID específico
SELECT 
    ID,
    CONTRACT_UID,
    CONTRACT_TYPE,
    STATUS,
    STATUS_MESSAGE,
    CREATED_AT
FROM IC_ADMIN.CONTRACTS 
WHERE CONTRACT_UID = 'CBRO-I-12345'
ORDER BY CREATED_AT DESC;

-- Contar contratos por estado
SELECT 
    STATUS,
    COUNT(*) as COUNT
FROM IC_ADMIN.CONTRACTS 
GROUP BY STATUS
ORDER BY STATUS;

-- Verificar partes del contrato
SELECT 
    cp.ID,
    cp.CONTRACT_ID,
    cp.ROLE,
    cp.FULL_NAME,
    cp.EMAIL,
    cp.PHONE,
    cp.CREATED_AT
FROM IC_ADMIN.CONTRACT_PARTIES cp
JOIN IC_ADMIN.CONTRACTS c ON cp.CONTRACT_ID = c.ID
WHERE c.CONTRACT_UID = 'CBRO-I-12345'
ORDER BY cp.CREATED_AT DESC;

-- Verificar log de auditoría
SELECT 
    al.ID,
    al.CONTRACT_ID,
    al.ACTOR_USER_ID,
    al.ACTION,
    al.STATUS,
    al.MESSAGE,
    al.AT
FROM IC_ADMIN.CONTRACT_AUDIT_LOG al
JOIN IC_ADMIN.CONTRACTS c ON al.CONTRACT_ID = c.ID
WHERE c.CONTRACT_UID = 'CBRO-I-12345'
ORDER BY al.AT DESC;

-- Verificar eventos de entrega
SELECT 
    de.ID,
    de.CONTRACT_ID,
    de.CHANNEL,
    de.ADDRESS,
    de.STATUS,
    de.PROVIDER_MSG_ID,
    de.ERROR_MESSAGE,
    de.SENT_AT
FROM IC_ADMIN.DELIVERY_EVENTS de
JOIN IC_ADMIN.CONTRACTS c ON de.CONTRACT_ID = c.ID
WHERE c.CONTRACT_UID = 'CBRO-I-12345'
ORDER BY de.SENT_AT DESC;
