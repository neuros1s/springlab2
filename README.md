# Spring Labs 2 3 4 and 5

Student: Turgynbek Amirbek, IS2413. Class-list position: 20. Lab 5 variant: 8.

Lab 5 adds a book catalogue API, an HTML page, static resources, Post/Redirect/Get,
and response field names selected through Accept-Language. It continues the
verified Lab 4 project in the same Spring Boot application.

## Run

Use JDK 25 and Maven 3.6.3 or newer:

    mvn clean verify
    java -jar target/spring-lab-01-0.0.1-SNAPSHOT.jar

Open http://localhost:8080/books. The root URL http://localhost:8080/ serves the
welcome page. Each application restart restores the three seed books because
the repository is in memory.

## Lab 5 endpoints

| Request | Result |
| --- | --- |
| GET /api/books?author=Bloch&limit=1 | 200 and a filtered JSON list; default limit is 10 |
| GET /api/books/{id} | 200 or 404 |
| POST /api/books with JSON | 201, created book and Location |
| PUT /api/books/{id} with JSON | 200 replacement or 404; the path id wins |
| DELETE /api/books/{id} | 204 with no body or 404 |
| GET /books?author=Bloch | HTML rendered from the model |
| POST /books with form fields | 302 redirect and a one-time flash message |
| GET /css/app.css | Static stylesheet |
| GET / | Static welcome page |

The author filter matches a fragment without case sensitivity. A new resource
always receives a new id; a client id cannot overwrite another book. PUT never
inserts an absent id. Negative limits and invalid book data give 400.

Use requests-lab05.http for all demonstration requests.

## Variant 8

GET /api/books reads Accept-Language through RequestHeader. English/default
responses contain id, title, author and year. Russian responses contain id,
название, автор and год. Regional tags and q preferences are handled by
Locale.lookup. Unsupported languages fall back to English. Content-Language
reports the selected language; Vary: Accept-Language declares cache variation.

    curl -i -H "Accept-Language: en" "http://localhost:8080/api/books?limit=1"
    curl -i -H "Accept-Language: ru" "http://localhost:8080/api/books?limit=1"

## Required failure cases

| Request | Status and cause |
| --- | --- |
| GET /api/books/abc | 400: long argument conversion failed |
| GET /api/nothing | 404: no API mapping or static resource |
| DELETE /api/books | 405: collection URL has no DELETE mapping |
| POST /api/books with Content-Type text/plain | 415: consumes JSON condition fails |
| GET /api/books with Accept application/xml | 406: produces JSON condition fails |

## Automated verification and report

The lab05 workflow runs Maven tests on Java 25, starts the real runnable JAR,
sends HTTP requests, and drives Chromium through catalogue, filter, form POST
and refresh. It checks CSS, the flash lifecycle, duplicate prevention, all REST
statuses and language headers. Screenshots come from that running application.

The successful run publishes two artifacts:
- Lab05-Report-and-Evidence: DOCX, PDF, browser screenshots, responses and server log.
- Lab05-Application: runnable JAR.

Local evidence generation, after a successful Maven verification:

    python -m pip install python-docx pillow playwright
    python -m playwright install --with-deps chromium
    python scripts/capture_lab05.py
    python scripts/build_lab05_report.py

The capture script uses port 8080 and requires it to be free. Use
python scripts/capture_lab05.py --port 8081 to use another port. PDF conversion
requires LibreOffice. The report script refuses to build a final report when
the live checks did not pass.

A live demonstration and explanations remain part of the assignment.

## Earlier labs

The prior code remains available:
- /api/lab2/notify, /api/lab2/custom, /api/lab2/scopes and /api/lab2/lifecycle
- /api/lab3/config with default, dev and prod configuration
- /api/lab4/item/{id}, /api/lab4/items, /api/lab4/proxy and removal experiments

requests.http and scripts/build_report.py belong to Lab 4. Lab 5 evidence is
stored separately under evidence/lab05.
