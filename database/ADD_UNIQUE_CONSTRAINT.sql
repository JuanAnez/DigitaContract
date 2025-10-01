-- ======================================================================
--  AGREGAR RESTRICCIÓN ÚNICA PARA CONTRACT_UID
--  OBJETIVO : Prevenir contratos duplicados en el futuro
--  FECHA    : 2025-09-29
-- ======================================================================

-- 1. Verificar si ya existe la restricción
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME,
    COLUMN_NAME
FROM USER_CONSTRAINTS 
WHERE TABLE_NAME = 'CONTRACTS' 
AND CONSTRAINT_TYPE = 'U'
AND COLUMN_NAME = 'CONTRACT_UID';

-- 2. Agregar restricción única si no existe
ALTER TABLE IC_ADMIN.CONTRACTS 
ADD CONSTRAINT UQ_CONTRACT_UID UNIQUE (CONTRACT_UID);

-- 3. Verificar que la restricción se creó correctamente
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME,
    COLUMN_NAME
FROM USER_CONSTRAINTS 
WHERE TABLE_NAME = 'CONTRACTS' 
AND CONSTRAINT_TYPE = 'U';

-- 4. Crear índice para mejorar el rendimiento
CREATE INDEX IX_CONTRACTS_UID ON IC_ADMIN.CONTRACTS (CONTRACT_UID);

COMMIT;
