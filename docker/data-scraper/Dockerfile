# build the scraper project
FROM gradle:8.12.1-jdk21-alpine AS builder

WORKDIR /app
COPY . .
RUN gradle clean build -x test
RUN gradle :data-scraper:run

# copy the result database and exit
FROM scratch

COPY --from=builder /app/data-scraper/build/db/sfia-sqlite.db /db/sfia-sqlite.db
COPY --from=builder /bin/echo /bin/echo
VOLUME /db
CMD ["/bin/echo"]