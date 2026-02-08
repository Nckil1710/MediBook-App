# Appointment Booking Backend

## JDK requirement (fix for build errors)

This project **must be built with JDK 17 or JDK 21**. Using JDK 25 (or other very new versions) can cause:

```text
java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

This comes from a known compatibility issue between Lombok and newer JDKs.

### What to do

1. **IntelliJ IDEA**
   - **File → Project Structure → Project**: set **Project SDK** to **17** or **21** (not 25).
   - **File → Settings → Build, Execution, Deployment → Build Tools → Maven → Importing**: set **JDK for importer** to the same **17** or **21**.
   - Re-import Maven project and rebuild.

2. **Eclipse**
   - Use **Project → Properties → Java Compiler** and set compliance to **17**.
   - Ensure the project uses a JDK 17 or 21 runtime.

3. **Command line**
   - Use Java 17 or 21 when running Maven, for example:
     - Windows: `set JAVA_HOME=C:\Program Files\Java\jdk-17`
     - Then: `mvn clean compile`

After switching to JDK 17 or 21, the build should succeed.

---

## Slots and doctors

- **Doctors** are linked to a **service** (e.g. Dental, General Consultation). Each doctor has a **weekday slot count** (4–6) and **weekend slot count** (2–3).
- On startup, **fixed slots** are generated for the next 60 days (configurable via `app.slot-generation.days-ahead`): for each doctor, weekdays get 4–6 time slots per day and weekends 2–3, so the calendar is always populated.
- **APIs:** list doctors with `GET /api/doctors` or `GET /api/doctors/by-service/{serviceId}`; get slots with `GET /api/slots/available?doctorId=&date=`.

### Upgrading from the previous version (slots by service)

If you see **"Field 'service_id' doesn't have a default value"** on startup, the `slots` table still has the old `service_id` column and its foreign key. Run this **once** in MySQL (drop the FK first, then the column):

```sql
USE appointment;
ALTER TABLE slots DROP FOREIGN KEY FKfjpw9eyume5svnso2l1cyqq2g;
ALTER TABLE slots DROP COLUMN service_id;
```

If the constraint name is different, run `SHOW CREATE TABLE slots;` and use the actual foreign key name in the first statement. Then restart the application.
