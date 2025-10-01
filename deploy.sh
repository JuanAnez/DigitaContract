#!/bin/bash

# Script de deployment para Contract Authentication Service
# Uso: ./deploy.sh [weblogic_home] [domain_home]

echo "=== Contract Authentication Service Deployment ==="

# Verificar parámetros
if [ $# -lt 2 ]; then
    echo "Uso: $0 <weblogic_home> <domain_home>"
    echo "Ejemplo: $0 /opt/oracle/weblogic /opt/oracle/domains/mydomain"
    exit 1
fi

WEBLOGIC_HOME=$1
DOMAIN_HOME=$2
WAR_FILE="target/contract.war"

# Verificar que el archivo WAR existe
if [ ! -f "$WAR_FILE" ]; then
    echo "Error: No se encontró el archivo $WAR_FILE"
    echo "Ejecute 'mvn clean package' primero"
    exit 1
fi

# Verificar que WebLogic está instalado
if [ ! -d "$WEBLOGIC_HOME" ]; then
    echo "Error: WebLogic no encontrado en $WEBLOGIC_HOME"
    exit 1
fi

# Verificar que el dominio existe
if [ ! -d "$DOMAIN_HOME" ]; then
    echo "Error: Dominio no encontrado en $DOMAIN_HOME"
    exit 1
fi

echo "Compilando proyecto..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Error: Falló la compilación"
    exit 1
fi

echo "Copiando WAR al dominio..."
cp $WAR_FILE $DOMAIN_HOME/autodeploy/

echo "Deployment completado!"
echo "La aplicación estará disponible en: http://localhost:7001/contract"
echo ""
echo "Endpoints disponibles:"
echo "  POST /contract/api/login-ws"
echo "  POST /contract/api/logout-ws"
echo ""
echo "Usuarios de prueba:"
echo "  train_ic_user / claro123"
echo "  train_ic_admin / claro123"
echo "  isy94545 / claro123"

