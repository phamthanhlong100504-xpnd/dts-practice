# TODO - DTS Practice Service

> **Muc tieu:** Hoan thanh service thi thu sat hach lai xe, tich hop JWT auth voi dts-identity.
> **Tech stack:** Java 21, Spring Boot 3.3.2, PostgreSQL 16, Flyway, Docker Compose
> **Code hien tai:** Da co day du project skeleton (entities, repositories, services, controllers, security, configs). Xem file `src/` de biet chi tiet.

---

## BUOC 0: Kiem tra moi truong

```bash
java -version           # Phai la Java 21+
docker --version        # Phai co Docker
mvn --version           # Phai co Maven 3.9+
```

---

## BUOC 1: Khoi dong Identity Service (can thiet de lay JWT token)

Identity service nam o `C:\Users\Me\Documents\dts-identity`.

```bash
cd C:\Users\Me\Documents\dts-identity

# Khoi dong infrastructure (Postgres, Redis, Kafka)
docker compose -f docker-compose.infra.yml up -d

# Build & run identity service
mvn spring-boot:run
```

Identity service chay o `http://localhost:8081`. Swagger: `http://localhost:8081/swagger-ui.html`

Dang ky 1 user qua Swagger (POST `/api/v1/auth/register`), sau do dang nhap (POST `/api/v1/auth/login`) de lay `accessToken`.

---

## BUOC 2: Sua Exam entity de ho tro PostgreSQL INTEGER[]

Trong file `src/main/java/com/dts/practice/entity/Exam.java`, field `questionIds` hien tai dung `columnDefinition = "INTEGER[]"` nhung Hibernate 6 can annotation dac biet de map array.

**Cach sua:** Them dependency `hypersistence-utils` vao `pom.xml`:

```xml
<!-- Them vao <dependencies> trong pom.xml -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.7.5</version>
</dependency>
```

Sau do sua entity `Exam.java`, doi field `questionIds` thanh:

```java
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;

@Type(ListArrayType.class)
@Column(name = "question_ids", columnDefinition = "INTEGER[]")
private List<Integer> questionIds;
```

**Hoac cach thay the don gian hon:** Thay `INTEGER[]` bang `TEXT` (luu JSON array). Sua trong `V1__init_practice_schema.sql`:

```sql
question_ids    TEXT NOT NULL,  -- Luu JSON array, vd: "[1,5,23,45]"
```

Va trong `Exam.java`:

```java
@Column(name = "question_ids", columnDefinition = "TEXT")
private String questionIds;  // Luu JSON string, parse bang ObjectMapper khi can
```

Chon 1 trong 2 cach tren. **Khuyen nghi cach 2 (TEXT/JSON) vi don gian hon, khong can them dependency.**

> **IMPORTANT:** Sau khi chon cach sua, phai dong bo:
> - `V1__init_practice_schema.sql` (thay `INTEGER[]` bang `TEXT` neu chon cach 2)
> - `Exam.java` entity
> - `ExamService.java` (neu chon cach 2: can parse `questionIds` tu JSON string sang `List<Integer>` o nhung cho su dung `exam.getQuestionIds()`)

---

## BUOC 3: Khoi dong PostgreSQL cho Practice Service

```bash
cd C:\Users\Me\Documents\dts-practice
docker compose -f docker-compose.infra.yml up -d
```

PostgreSQL se chay o port **5433** (khong xung dot voi identity-service o port 5432).

Database name: `dts_practice`, user/pass: `postgres/postgres`.

---

## BUOC 4: Tao Flyway migration de seed 600 cau hoi

Du lieu cau hoi nam o `C:\Users\Me\Documents\dts-dataset\driving-license\sql\seed-questions.sql`.

Tao file `src/main/resources/db/migration/V2__seed_questions.sql`:

```sql
-- V2: Seed 600 cau hoi tu dts-dataset

-- Xoa het cau hoi cu (neu co)
DELETE FROM questions;

-- Insert 600 cau hoi. Copy toan bo noi dung tu file:
-- C:\Users\Me\Documents\dts-dataset\driving-license\sql\seed-questions.sql
-- BO DONG BEGIN; VA COMMIT; (Flyway tu quan ly transaction)
--
-- Hoac dung lenh sau de auto-generate:
```

**Cach nhanh:** Chay lenh nay trong terminal Git Bash:

```bash
cd C:\Users\Me\Documents\dts-practice

# Tao V2 migration tu seed data co san (bo BEGIN/COMMIT)
echo "-- V2: Seed 600 cau hoi" > src/main/resources/db/migration/V2__seed_questions.sql
tail -n +3 C:/Users/Me/Documents/dts-dataset/driving-license/sql/seed-questions.sql | head -n -2 >> src/main/resources/db/migration/V2__seed_questions.sql
```

---

## BUOC 5: Build va chay Practice Service

```bash
cd C:\Users\Me\Documents\dts-practice
mvn clean compile -DskipTests
mvn spring-boot:run
```

Service chay o `http://localhost:8082`. Swagger: `http://localhost:8082/swagger-ui.html`

Flyway se tu dong chay `V1__init_practice_schema.sql` va `V2__seed_questions.sql`.

---

## BUOC 6: Test API

### 6a. Test khong can auth (public endpoints)

```bash
# Home
curl http://localhost:8082/

# Health check
curl http://localhost:8082/actuator/health
```

### 6b. Test voi JWT token (lay tu identity service o Buoc 1)

Dat token vao bien:
```bash
TOKEN="<accessToken lay tu identity-service>"
```

**Lay danh sach cau hoi:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/questions/chapter/1
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/questions/critical
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/questions/stats
```

**Bat dau bai thi moi:**
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"examType":"A1","totalQuestions":25}' \
  http://localhost:8082/api/v1/exams
```

Luu lai `examId` tu response.

**Xem bai thi:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/exams/<examId>
```

**Nop cau tra loi (lap lai cho tung cau):**
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"questionId":"1","selectedAnswer":"B"}' \
  http://localhost:8082/api/v1/exams/<examId>/answers
```

**Ket thuc bai thi:**
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/exams/<examId>/finish
```

**Xem ket qua:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/exams/<examId>/result
```

**Xem lich su thi:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/v1/exams/history
```

---

## BUOC 7: SUA LOI THUONG GAP

### Loi 1: "JWT validation failed" khi goi API
- Kiem tra `JWT_ACCESS_SECRET` trong `.env` co **giong het** secret ben identity-service khong
- Kiem tra `JWT_ISSUER` co dung la `dts-identity-service` khong
- Token het han (15 phut) → dang nhap lai

### Loi 2: "Question not found" khi nop bai
- Kiem tra `V2__seed_questions.sql` da duoc chay chua (Flyway se log trong DB)
- Kiem tra database: `docker exec -it dts-practice-postgres psql -U postgres -d dts_practice -c "SELECT count(*) FROM questions;"`

### Loi 3: "Table not found" khi start
- Flyway chua chay migration. Kiem tra:
  ```bash
  docker exec -it dts-practice-postgres psql -U postgres -d dts_practice -c "SELECT * FROM flyway_schema_history;"
  ```

### Loi 4: Service khong start duoc
- Xem log: `mvn spring-boot:run` se in ra loi chi tiet
- Kiem tra PostgreSQL da chay: `docker ps | grep practice-postgres`
- Kiem tra port 5433 khong bi chiem

---

## BUOC 8 (OPTIONAL): Dong goi Docker image

```bash
cd C:\Users\Me\Documents\dts-practice
docker compose build
docker compose up -d
```

Practice service se chay trong Docker o `http://localhost:8082`.

---

## KIEN TRUC TONG QUAN

```
[Client] --> [dts-practice:8082] --> [dts-identity:8081] (validate JWT)
                 |
                 +--> PostgreSQL (dts_practice)
                         - questions (600 cau hoi)
                         - exams (bai thi cua user)
                         - exam_answers (cau tra loi tung cau)

JWT flow:
1. User dang nhap qua identity-service → nhan accessToken
2. User goi practice-service kem accessToken trong header
3. Practice-service verify token bang JWT secret (giong identity)
4. Trich xuat userId, roles tu token claims
```

---

## CONG VIEC CON LAI (CHUA CODE)

| Viec | Mo ta |
|------|-------|
| **Exam time limit** | Them truong `duration_minutes` vao Exam, auto-finish khi het gio |
| **Leaderboard** | API GET `/api/v1/exams/leaderboard` — xep hang diem cao |
| **Practice mode** | Thi theo chuong, khong tinh diem, co giai thich ngay |
| **User sync** | Khi user update profile o identity-service → Kafka event → practice sync |
| **Test coverage** | Viet unit test + integration test |
