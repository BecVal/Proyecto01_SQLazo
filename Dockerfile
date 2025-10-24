FROM openjdk:17-slim
WORKDIR /app
COPY src ./src
RUN apt-get update && apt-get install -y findutils
RUN mkdir out && javac -d out $(find src -name "*.java")
CMD ["java", "-cp", "out", "mx.unam.ciencias.myp.pumabank.Main"]
