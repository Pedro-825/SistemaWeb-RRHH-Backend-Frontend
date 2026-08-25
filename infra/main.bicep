@description('Short lowercase prefix used for Azure resource names.')
param namePrefix string

@description('Azure region for all resources.')
param location string = resourceGroup().location

@description('Azure region for Static Web Apps.')
param staticWebAppLocation string = 'eastus2'

@description('PostgreSQL administrator login.')
param postgresAdminLogin string

@secure()
@description('PostgreSQL administrator password.')
param postgresAdminPassword string

@secure()
@description('JWT signing secret used by the backend.')
param jwtSecret string

@description('SMTP username used by the backend.')
param mailUsername string

@secure()
@description('SMTP password or Gmail app password used by the backend.')
param mailPassword string

@description('PostgreSQL database name.')
param databaseName string = 'sangabriel_db_rrhh'

@description('App Service Plan SKU. B1 is simple for demos; lower it only after testing.')
param appServiceSkuName string = 'B1'

@description('PostgreSQL SKU. B_Standard_B1ms is a small burstable tier for demos.')
param postgresSkuName string = 'Standard_B1ms'

var suffix = uniqueString(resourceGroup().id, namePrefix)
var normalizedPrefix = toLower(replace(namePrefix, '-', ''))
var appServicePlanName = '${normalizedPrefix}-plan-${suffix}'
var backendAppName = '${normalizedPrefix}-api-${suffix}'
var postgresServerName = '${normalizedPrefix}-pg-${suffix}'
var staticWebAppName = '${normalizedPrefix}-web-${suffix}'
var storageAccountName = take('${normalizedPrefix}st${suffix}', 24)
var containerName = 'evidencias'

resource appServicePlan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: appServicePlanName
  location: location
  kind: 'linux'
  sku: {
    name: appServiceSkuName
  }
  properties: {
    reserved: true
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowBlobPublicAccess: false
    minimumTlsVersion: 'TLS1_2'
  }
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2023-05-01' = {
  name: 'default'
  parent: storageAccount
}

resource evidenciaContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  name: containerName
  parent: blobService
  properties: {
    publicAccess: 'Blob'
  }
}

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-06-01-preview' = {
  name: postgresServerName
  location: location
  sku: {
    name: postgresSkuName
    tier: 'Burstable'
  }
  properties: {
    version: '16'
    administratorLogin: postgresAdminLogin
    administratorLoginPassword: postgresAdminPassword
    storage: {
      storageSizeGB: 32
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
    network: {
      publicNetworkAccess: 'Enabled'
    }
  }
}

resource postgresDb 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-06-01-preview' = {
  name: databaseName
  parent: postgresServer
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

resource allowAzureServices 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-06-01-preview' = {
  name: 'allow-azure-services'
  parent: postgresServer
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

resource staticWebApp 'Microsoft.Web/staticSites@2023-12-01' = {
  name: staticWebAppName
  location: staticWebAppLocation
  sku: {
    name: 'Free'
    tier: 'Free'
  }
  properties: {}
}

var frontendUrl = 'https://${staticWebApp.properties.defaultHostname}'
var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'

resource backendApp 'Microsoft.Web/sites@2023-12-01' = {
  name: backendAppName
  location: location
  kind: 'app,linux'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|21-java21'
      alwaysOn: true
      ftpsState: 'Disabled'
      appSettings: [
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'prod'
        }
        {
          name: 'DB_URL'
          value: 'jdbc:postgresql://${postgresServer.properties.fullyQualifiedDomainName}:5432/${databaseName}?sslmode=require'
        }
        {
          name: 'DB_USERNAME'
          value: postgresAdminLogin
        }
        {
          name: 'DB_PASSWORD'
          value: postgresAdminPassword
        }
        {
          name: 'JWT_SECRET'
          value: jwtSecret
        }
        {
          name: 'MAIL_USERNAME'
          value: mailUsername
        }
        {
          name: 'MAIL_PASSWORD'
          value: mailPassword
        }
        {
          name: 'FRONTEND_URL'
          value: frontendUrl
        }
        {
          name: 'UPLOAD_DIR'
          value: '/home/uploads/evidencias'
        }
        {
          name: 'AZURE_STORAGE_CONNECTION_STRING'
          value: storageConnectionString
        }
        {
          name: 'AZURE_STORAGE_CONTAINER'
          value: containerName
        }
        {
          name: 'WEBSITES_PORT'
          value: '8080'
        }
      ]
    }
  }
  dependsOn: [
    postgresDb
    allowAzureServices
    evidenciaContainer
  ]
}

output backendAppName string = backendApp.name
output backendUrl string = 'https://${backendApp.properties.defaultHostName}'
output staticWebAppName string = staticWebApp.name
output frontendUrl string = frontendUrl
output postgresServerName string = postgresServer.name
