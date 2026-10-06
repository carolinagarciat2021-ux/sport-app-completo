# Tienda Sport — Backend (Spring Boot)

Backend migrado de Java plano a **Spring Boot + Spring Security + JWT**, pensado
para desplegarse en la nube (Railway o Render) con HTTPS real.

## Variables de entorno necesarias en producción

| Variable | Para qué sirve | Ejemplo |
|---|---|---|
| `MYSQLHOST` | Host de tu MySQL en la nube | `containers-us-west-1.railway.app` |
| `MYSQLPORT` | Puerto de MySQL | `6543` |
| `MYSQLDATABASE` | Nombre de la base de datos | `venta_ropa_deportiva` |
| `MYSQLUSER` | Usuario de MySQL | `root` |
| `MYSQLPASSWORD` | Contraseña de MySQL | *(la que te dé el proveedor)* |
| `JWT_SECRET` | Clave para firmar los tokens — **genera una propia, larga y aleatoria** | *(ver instrucciones de despliegue)* |
| `JWT_EXPIRATION_MS` | Duración del token en milisegundos (opcional) | `14400000` (4 horas) |
| `ALLOWED_ORIGINS` | URL(s) de tu frontend publicado, separadas por coma | `https://tiendasport.vercel.app` |
| `PORT` | Railway/Render la asignan automáticamente — no la definas tú | *(automática)* |

En tu computador, para desarrollo local, **no necesitas definir ninguna**: el
proyecto ya trae valores por defecto que apuntan a tu MySQL Workbench local
(`localhost:3306`) y a `http://localhost:3000` como origen permitido.

## Cómo correr el proyecto localmente

```bash
mvn spring-boot:run
```

## Cómo construir el .jar manualmente

```bash
mvn clean package
java -jar target/tiendasport-backend.jar
```

## Despliegue

Ver la guía paso a paso que te dio Claude para Railway/Render — este proyecto
ya incluye el `Dockerfile` necesario para ambos.
