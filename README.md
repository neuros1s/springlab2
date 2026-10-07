# Spring Laboratory Work 4

Turgynbek Amirbek, IS2413. Variant 8 (class-list position 20, cyclic allocation).

Requires Java 25 and Maven 3.6.3 or newer. Spring Boot 3.5.16 manages dependency versions, including spring-boot-starter-aop. Build from this directory:

```sh
mvn clean verify
java -jar target/spring-lab-01-0.0.1-SNAPSHOT.jar
```

The service simulates a catalog; it does not delete database records. Default port: 8080.

| Request | Purpose |
| --- | --- |
| GET /api/lab4/item/5 | Logged and timed, not audited |
| GET /api/lab4/items?limit=5 | Audited slow successful operation |
| DELETE /api/lab4/item/0 | Exception, audit failure, HTTP 400 |
| DELETE /api/lab4/item/5 | External audited removal |
| GET /api/lab4/proxy | Real proxy type and superclass |
| GET /api/lab4/remove-twice/5 | Self-invocation bypasses advice for remove |
| GET /api/lab4/remove-twice-fixed/5 | Two calls through CatalogService proxy |
| GET /api/lab4/remove-twice-fixed/0 | Nested exception and trace cleanup |

Windows PowerShell:

```powershell
curl.exe "http://localhost:8080/api/lab4/items?limit=5"
curl.exe -X DELETE "http://localhost:8080/api/lab4/item/0"
curl.exe "http://localhost:8080/api/lab4/proxy"
curl.exe "http://localhost:8080/api/lab4/remove-twice/5"
curl.exe "http://localhost:8080/api/lab4/remove-twice-fixed/5"
```

Mac/Linux use curl instead of curl.exe.

Trace has order 0, audit 1, logging 2, timing 3. The added trace wraps the required three aspects without changing their relative ordering. Depth is per thread; After performs cleanup on success and failure. All five advice types are represented.

Lab4EvidenceTest exercises the actual Spring context, proxy beans and MVC mappings through MockMvc. It checks outcomes before writing evidence/results.json. Captured logs are real test output, not predicted examples. No external HTTP listener is used during those tests; run the JAR for the live classroom demonstration.

After a successful `mvn clean verify`, `python scripts/build_report.py` creates the report from evidence. It needs python-docx, Pillow and DejaVu fonts. The GitHub workflow provisions these tools and converts the report to PDF using LibreOffice.

The upload must place `pom.xml`, `src`, `scripts` and `.github` at repository root. The workflow triggers on pushes to lab04. It has read-only repository permissions and uploads output as a workflow artifact; it does not alter branches or merge pull requests.
