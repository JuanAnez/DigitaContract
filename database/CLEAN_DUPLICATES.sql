-- ======================================================================
--  LIMPIAR CONTRATOS DUPLICADOS
--  OBJETIVO : Eliminar contratos duplicados y mantener solo el más reciente
--  FECHA    : 2025-09-29
-- ======================================================================

-- 1. Verificar contratos duplicados
SELECT 
    CONTRACT_UID,
    COUNT(*) as DUPLICATE_COUNT,
    MIN(CREATED_AT) as FIRST_CREATED,
    MAX(CREATED_AT) as LAST_CREATED
FROM IC_ADMIN.CONTRACTS 
GROUP BY CONTRACT_UID
HAVING COUNT(*) > 1
ORDER BY CONTRACT_UID;

-- 2. Ver todos los contratos con CBRO-I-12345
SELECT 
    ID,
    CONTRACT_UID,
    CONTRACT_TYPE,
    STATUS,
    CREATED_AT,
    UPDATED_AT
FROM IC_ADMIN.CONTRACTS 
WHERE CONTRACT_UID = 'CBRO-I-12345'
ORDER BY CREATED_AT DESC;

-- 3. Eliminar contratos duplicados (mantener solo el más reciente)
DELETE FROM IC_ADMIN.CONTRACTS 
WHERE ID NOT IN (
    SELECT MAX(ID) 
    FROM IC_ADMIN.CONTRACTS 
    GROUP BY CONTRACT_UID
);

-- 4. Verificar que solo queda un contrato por UID
SELECT 
    CONTRACT_UID,
    COUNT(*) as COUNT
FROM IC_ADMIN.CONTRACTS 
GROUP BY CONTRACT_UID
ORDER BY CONTRACT_UID;

-- 5. Verificar el estado final
SELECT 
    ID,
    CONTRACT_UID,
    CONTRACT_TYPE,
    STATUS,
    CREATED_AT
FROM IC_ADMIN.CONTRACTS 
WHERE CONTRACT_UID = 'CBRO-I-12345'
ORDER BY CREATED_AT DESC;

COMMIT;
