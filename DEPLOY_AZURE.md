# Despliegue automatico en Azure

Esta ruta crea/actualiza la infraestructura con Bicep y despliega el proyecto con GitHub Actions, sin convertir los controllers Spring Boot a Azure Functions.

## Arquitectura

- Backend Spring Boot: Azure App Service.
- Base de datos: Azure Database for PostgreSQL Flexible Server.
- Frontend React/Vite: Azure Static Web Apps.
- Evidencias: Azure Storage Blob.
- App movil Expo: apunta a la URL publica del backend.

## 1. Que crea el workflow

El workflow `.github/workflows/provision-and-deploy-azure.yml` crea automaticamente:

```text
Resource Group
Azure App Service Plan Linux
Azure App Service Java 21
Azure Database for PostgreSQL Flexible Server
Azure Static Web Apps
Azure Storage Account
Blob container evidencias
```

Luego compila y despliega:

```text
backend/RRHH -> App Service
frontend/rrhh-frontend -> Static Web Apps
```

## 2. Conexion inicial GitHub + Azure

Esto se hace una sola vez para que GitHub pueda crear recursos en tu Azure:

1. En Microsoft Entra ID crea una App Registration para GitHub Actions.
2. Agrega una Federated Credential para este repositorio y la rama `main`.
3. Dale rol `Contributor` a esa app sobre tu suscripcion o sobre el Resource Group.
4. Copia `clientId`, `tenantId` y `subscriptionId`.

En GitHub > Settings > Secrets and variables > Actions > Secrets agrega:

```text
AZURE_CLIENT_ID
AZURE_TENANT_ID
AZURE_SUBSCRIPTION_ID
POSTGRES_ADMIN_PASSWORD
JWT_SECRET
MAIL_PASSWORD
```

En GitHub > Settings > Secrets and variables > Actions > Variables agrega:

```text
AZURE_RESOURCE_GROUP=rg-rrhh-san-gabriel
AZURE_LOCATION=eastus
AZURE_STATIC_WEB_APP_LOCATION=eastus2
AZURE_NAME_PREFIX=rrhhsg
POSTGRES_ADMIN_LOGIN=rrhhadmin
MAIL_USERNAME=rrhh.sangabrielhospital@gmail.com
APP_MOBILE_DOWNLOAD_URL=https://drive.google.com/uc?export=download&id=<id-del-apk-vigente>
```

## 3. Desplegar

Haz push a `main` o ejecuta manualmente este workflow:

```text
Provision and deploy Azure
```

El flujo hace:

```text
1. Inicia sesion en Azure con OIDC.
2. Crea o actualiza el Resource Group.
3. Ejecuta infra/main.bicep.
4. Lee las URLs generadas.
5. Compila el backend Spring Boot.
6. Despliega el JAR en App Service.
7. Compila y despliega el frontend.
8. Configura VITE_API_URL con la URL del backend.
```

## 4. App movil

El enlace que reciben los empleados por correo sale de la variable de App Service `APP_MOBILE_DOWNLOAD_URL`.
Si reemplazas el APK en Google Drive conservando el mismo archivo, el link no cambia. Si subes un archivo nuevo, copia el nuevo ID de Drive y actualiza esa variable en Azure.

Cuando el backend ya este en Azure, toma la URL del output `backend_url` del job `provision` y ejecuta:

```powershell
cd mobile
$env:EXPO_PUBLIC_API_URL="https://<nombre-de-tu-app-service>.azurewebsites.net"
npm start
```

Luego abre Expo Go y escanea el QR.

## 5. Orden recomendado de prueba

1. El workflow termina en verde.
2. App Service abre sin errores.
3. Flyway crea/migra tablas en PostgreSQL.
4. Frontend abre y puede hacer login.
5. App movil registra dispositivo por DNI + OTP.
6. App movil marca entrada con huella.
7. App movil marca salida con huella.

## 6. Nota de costo

La plantilla usa `B1` para App Service y `Standard_B1ms` para PostgreSQL porque son tiers pequenos para demo. Si quieres bajar costo despues de validar, ajusta estos parametros en `infra/main.bicep`:

```text
appServiceSkuName
postgresSkuName
```
