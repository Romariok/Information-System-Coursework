FROM haskell:latest AS parser-build
WORKDIR /parser

# Устанавливаем зависимость cryptonite, которая нужна для Crypto.Hash
RUN cabal update && cabal install cryptonite --lib

# Копируем исходники парсера (контекст сборки будет корень репозитория)
COPY parser/*.hs ./

# Собираем бинарник парсера (явно подключаем скрытые пакеты text и bytestring)
RUN ghc -O2 -threaded -rtsopts "-with-rtsopts=-N" -package cryptonite -package text -package bytestring -o parser Main.hs

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY backend/pom.xml .
COPY backend/src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /opt/app

COPY --from=build /workspace/target/GuitarMatchIS-0.0.1.jar /opt/app/app.jar
COPY --from=parser-build /parser/parser /opt/app/parser
COPY backend/docker/entrypoint.sh /opt/app/entrypoint.sh
# Конвертируем CRLF→LF (если файл пришёл из Windows) и делаем скрипт исполняемым
RUN sed -i 's/\r$//' /opt/app/entrypoint.sh && chmod +x /opt/app/entrypoint.sh

EXPOSE 5252
ENV PORT=5252

ENTRYPOINT ["/opt/app/entrypoint.sh"]


