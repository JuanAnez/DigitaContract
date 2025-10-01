#!/bin/bash

echo "=== CONFIGURANDO MINIO PARA DESARROLLO ==="
echo ""

# Crear directorio para datos de MinIO
mkdir -p ./minio-data

# Verificar si MinIO ya está corriendo
if docker ps | grep -q "minio"; then
    echo "⚠️  MinIO ya está corriendo. Deteniendo instancia anterior..."
    docker stop minio 2>/dev/null || true
    docker rm minio 2>/dev/null || true
fi

echo "🚀 Iniciando MinIO..."
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=password123" \
  -v $(pwd)/minio-data:/data \
  minio/minio server /data --console-address ":9001"

echo ""
echo "⏳ Esperando que MinIO se inicie..."
sleep 5

# Verificar que MinIO esté corriendo
if docker ps | grep -q "minio"; then
    echo "✅ MinIO iniciado correctamente!"
    echo ""
    echo "📋 INFORMACIÓN DE ACCESO:"
    echo "   🌐 Console: http://localhost:9001"
    echo "   👤 Usuario: admin"
    echo "   🔑 Password: password123"
    echo "   🔗 API Endpoint: http://localhost:9000"
    echo ""
    echo "📦 BUCKETS RECOMENDADOS:"
    echo "   - contracts-signed-pdfs"
    echo "   - contracts-drafts"
    echo "   - contracts-templates"
    echo ""
    echo "🔧 CONFIGURACIÓN PARA LA APLICACIÓN:"
    echo "   MINIO_ENDPOINT=http://localhost:9000"
    echo "   MINIO_ACCESS_KEY=admin"
    echo "   MINIO_SECRET_KEY=password123"
    echo "   MINIO_BUCKET_NAME=contracts-signed-pdfs"
else
    echo "❌ Error iniciando MinIO"
    exit 1
fi

echo ""
echo "🎉 MinIO configurado exitosamente!"
echo "   Puedes acceder a la consola en: http://localhost:9001"
