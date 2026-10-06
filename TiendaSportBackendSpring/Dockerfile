# ============================================================
# Etapa 1: compilar el proyecto con Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Descarga las dependencias primero (se cachea si el pom.xml no cambió,
# así las siguientes construcciones son más rápidas)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Etapa 2: imagen final, liviana, solo con el .jar ya compilado
# ============================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/tiendasport-backend.jar app.jar

# Railway/Render inyectan la variable PORT; Spring Boot ya está configurado
# para leerla (server.port=${PORT:8080})
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
