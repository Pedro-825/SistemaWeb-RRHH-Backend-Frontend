# 🏥 Sistema Web de Recursos Humanos - Hospital San Gabriel

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-JS-blue?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue?style=for-the-badge&logo=postgresql)

## 📖 Descripción

Sistema web desarrollado para la gestión integral de Recursos Humanos del **Hospital San Gabriel**. La plataforma permite centralizar y digitalizar los procesos administrativos relacionados con empleados, recursos humanos y gerencia, reemplazando procedimientos manuales por una solución moderna, segura y accesible mediante roles de usuario.

Este proyecto fue desarrollado como parte de una aplicación práctica universitaria, aplicando principios de ingeniería de software, arquitectura multicapa y desarrollo Full Stack.

> **Nombre de la Base de Datos:** `sangabriel_db_rrhh` (PostgreSQL)

## 🎯 Objetivos

*   ✅ Centralizar la información del personal.
*   ⚡ Optimizar los procesos de gestión de recursos humanos.
*   📂 Mejorar la accesibilidad y organización de los datos de empleados.
*   🔒 Implementar un sistema seguro basado en autenticación y autorización por roles.
*   📊 Facilitar la generación y consulta de información administrativa.

## 🚀 Funcionalidades

### 👥 Gestión de Empleados
*   Registro de nuevos empleados.
*   Consulta de información del personal.
*   Actualización de datos de empleados.
*   Búsqueda y filtrado avanzado de registros.

### 🏢 Gestión de Recursos Humanos
*   Administración de información laboral.
*   Control y seguimiento de datos del personal.
*   Organización centralizada de registros.

### 🔐 Seguridad y Acceso
*   Autenticación mediante **JWT** (JSON Web Token).
*   Control de acceso basado en roles (RBAC).
*   Protección de rutas y recursos mediante **Spring Security**.

### 📄 Reportes y Exportación
*   Generación de archivos **PDF**.
*   Exportación de información a **Excel**.

## 🏗️ Arquitectura

El sistema fue desarrollado bajo una arquitectura cliente-servidor desacoplada:

### Frontend
*   ⚛️ **React JS**: Desarrollo de la interfaz de usuario.
*   🟨 **JavaScript**: Lenguaje principal.
*   🌐 **Axios**: Cliente HTTP para comunicación con la API.
*   🧭 **React Router**: Manejo de navegación y rutas.

### Backend
*   ☕ **Spring Boot 3.5**: Framework para el desarrollo de la API REST.
*   🟦 **Java 21**: Lenguaje principal del backend.
*   🛡️ **Spring Security**: Gestión de seguridad y autorización.
*   🔑 **JWT**: Autenticación stateless basada en tokens.
*   🗄️ **Hibernate (JPA)**: Mapeo objeto-relacional para persistencia de datos.
*   🔄 **Flyway**: Control de versiones y migraciones de la base de datos.

### Base de Datos
*   🐘 **PostgreSQL**: Sistema gestor de base de datos relacional.

### Control de Versiones
*   🌿 **Git & GitHub**: Control de versiones y colaboración.

## 🛠️ Tecnologías Utilizadas

| Tecnología      | Descripción                              |
| --------------- | ---------------------------------------- |
| **React JS**    | Desarrollo de la interfaz de usuario     |
| **Spring Boot** | Desarrollo de la API REST                |
| **Java 21**     | Lenguaje principal del backend           |
| **Spring Security** | Seguridad y autorización             |
| **JWT**         | Autenticación basada en tokens           |
| **Hibernate (JPA)** | Persistencia de datos               |
| **PostgreSQL**  | Sistema gestor de base de datos          |
| **Flyway**      | Control de versiones de la base de datos |
| **Git & GitHub**| Control de versiones y colaboración      |
| **PDF & Excel** | Generación y exportación de reportes     |

## 🎓 Contexto Académico

Proyecto desarrollado en el ámbito universitario con fines académicos, orientado a la aplicación práctica de conocimientos en:
*   Ingeniería de Sistemas
*   Arquitectura de Software
*   Desarrollo Web Full Stack
*   Gestión de Bases de Datos

---
*Desarrollado para el Hospital San Gabriel*
