#!/bin/bash

echo "=== PROBANDO NUEVOS ENDPOINTS DE BÚSQUEDA ==="
echo ""

# Base URL
BASE_URL="http://localhost:7001/contract/api"

echo "1. Probando endpoint SIF..."
curl -s -X GET "$BASE_URL/search/sif/CBRO-I-12345" \
  -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Error en endpoint SIF"

echo ""
echo "2. Probando endpoint COPS..."
curl -s -X GET "$BASE_URL/search/cops/CBRO-I-12345?orderNumber=ORD-001" \
  -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Error en endpoint COPS"

echo ""
echo "3. Probando endpoint Historial..."
curl -s -X GET "$BASE_URL/search/history/CBRO-I-12345" \
  -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Error en endpoint Historial"

echo ""
echo "4. Probando búsqueda unificada..."
curl -s -X POST "$BASE_URL/search/unified" \
  -H "Content-Type: application/json" \
  -d '{
    "contractUid": "CBRO-I-12345",
    "searchType": "sif",
    "orderNumber": ""
  }' | jq '.' 2>/dev/null || echo "Error en búsqueda unificada"

echo ""
echo "=== FIN DE PRUEBAS ==="
