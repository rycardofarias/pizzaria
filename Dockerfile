FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar apenas o pom.xml primeiro
COPY pom.xml ./

# Copiar o Maven wrapper
COPY .mvn .mvn
COPY mvnw ./
COPY mvnw.cmd ./

# Tornar o mvnw executável
RUN chmod +x ./mvnw

# Baixar dependências
RUN ./mvnw dependency:go-offline -B

# Copiar o código fonte
COPY src ./src

# Build da aplicação
RUN ./mvnw package -DskipTests

# Configuração do timezone
ENV TZ=America/Sao_Paulo

EXPOSE 8090

# Healthcheck para monitoramento
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "target/pizzaria-0.0.1-SNAPSHOT.jar"]