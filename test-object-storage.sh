#!/bin/bash

echo "=== PROBANDO INTEGRACIÓN CON OBJECT STORAGE ==="
echo ""

# Verificar que MinIO esté corriendo
echo "🔍 Verificando MinIO..."
if ! curl -s http://localhost:9000 > /dev/null; then
    echo "❌ MinIO no está corriendo. Ejecutando setup..."
    ./setup-minio.sh
    sleep 3
fi

if curl -s http://localhost:9000 > /dev/null; then
    echo "✅ MinIO está corriendo"
else
    echo "❌ Error: MinIO no está disponible"
    exit 1
fi

echo ""
echo "🔍 Verificando backend..."
if ! curl -s http://localhost:7001/contract/api/login-ws > /dev/null; then
    echo "❌ Backend no está corriendo. Por favor inicia el backend primero."
    exit 1
fi

echo "✅ Backend está corriendo"

echo ""
echo "=== PROBANDO FLUJO COMPLETO ==="
echo ""

# Obtener token de autenticación
echo "🔐 Obteniendo token de autenticación..."
TOKEN=$(curl -s -X POST http://localhost:7001/contract/api/login-ws \
  -H "Content-Type: application/json" \
  -d '{"username":"train_ic_user","password":"claro123"}' | jq -r '.data')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Error obteniendo token de autenticación"
    exit 1
fi

echo "✅ Token obtenido: ${TOKEN:0:20}..."

echo ""
echo "📋 PROBANDO ENDPOINTS:"
echo ""

# Probar búsqueda SIF
echo "1. 🔍 Probando búsqueda SIF..."
SIF_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:7001/contract/api/search/sif/CBRO-I-12345)

if echo "$SIF_RESPONSE" | jq -e '.data.source' > /dev/null 2>&1; then
    echo "   ✅ SIF funcionando - Source: $(echo "$SIF_RESPONSE" | jq -r '.data.source')"
else
    echo "   ❌ Error en búsqueda SIF"
fi

# Probar búsqueda COPS
echo "2. 🔍 Probando búsqueda COPS..."
COPS_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:7001/contract/api/search/cops/CBRO-I-12346)

if echo "$COPS_RESPONSE" | jq -e '.data.source' > /dev/null 2>&1; then
    echo "   ✅ COPS funcionando - Source: $(echo "$COPS_RESPONSE" | jq -r '.data.source')"
else
    echo "   ❌ Error en búsqueda COPS"
fi

echo ""
echo "📦 VERIFICANDO MINIO:"
echo ""

# Verificar buckets en MinIO
echo "🔍 Verificando buckets en MinIO..."
echo "   Puedes verificar manualmente en: http://localhost:9001"
echo "   Usuario: admin"
echo "   Password: password123"

echo ""
echo "🎯 PRÓXIMOS PASOS:"
echo ""
echo "1. 📊 Ejecutar script de base de datos:"
echo "   sqlplus ic_user/ic_111_us@10.13.28.153:1530/ORADV12C @database/UPDATE_SCHEMA_OBJECT_STORAGE.sql"
echo ""
echo "2. 🚀 Reiniciar el backend para cargar nuevas dependencias"
echo ""
echo "3. 🧪 Probar creación de contratos con Object Storage:"
echo "   - Crear contrato desde frontend"
echo "   - Verificar que se guarde en MinIO"
echo "   - Verificar que se actualice la base de datos"
echo ""
echo "4. 📱 Verificar en MinIO Console:"
echo "   - Ir a http://localhost:9001"
echo "   - Buscar bucket 'contracts-signed-pdfs'"
echo "   - Verificar que se suban los PDFs"
echo ""

echo "🎉 Configuración de Object Storage completada!"
echo "   MinIO: http://localhost:9001"
echo "   Backend: http://localhost:7001"
