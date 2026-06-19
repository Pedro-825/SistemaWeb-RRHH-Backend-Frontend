#!/bin/sh
# Backup semanal de la base de datos PostgreSQL
# Se ejecuta todos los domingos a las 02:00 AM

BACKUP_DIR="/backups"
DB_NAME="sangabriel_db_rrhh"
DB_USER="postgres"
DB_HOST="postgres"
DB_PASSWORD="jenner0125"
RETENTION_DAYS=30

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql"

export PGPASSWORD="$DB_PASSWORD"

pg_dump -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -F p > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Backup creado: $BACKUP_FILE"
    gzip "$BACKUP_FILE"
    echo "Comprimido: ${BACKUP_FILE}.gz"
else
    echo "ERROR: Fallo el backup de $DB_NAME"
    exit 1
fi

# Eliminar backups mas antiguos que RETENTION_DAYS
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "Backups antiguos (>${RETENTION_DAYS} dias) eliminados."

unset PGPASSWORD
