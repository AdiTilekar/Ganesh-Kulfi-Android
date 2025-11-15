# PostgreSQL Migration - Quick Reference

## 🚀 Quick Start

### 1. Install PostgreSQL
```bash
# Download: https://www.postgresql.org/download/
```

### 2. Setup Database
```bash
psql -U postgres -f "backend/setup-postgresql.sql"
```

### 3. Build & Run
```bash
cd backend
../gradlew clean buildFatJar
java -jar build/libs/ganeshkulfi-backend-all.jar
```

### 4. Test
```bash
curl http://localhost:8080/api/health
```

---

## 🔄 Key SQL Changes

### Data Types
```sql
# MySQL → PostgreSQL

CHAR(36)                    → UUID
INT                         → INTEGER  
BIGINT AUTO_INCREMENT       → BIGSERIAL
DECIMAL(10,2)              → NUMERIC(10,2)
TIMESTAMP                   → TIMESTAMPTZ
ENUM('A','B')              → CREATE TYPE name AS ENUM('A','B')

UUID()                      → uuid_generate_v4()
CURRENT_TIMESTAMP           → NOW()
ON DUPLICATE KEY UPDATE     → ON CONFLICT ... DO NOTHING
```

### Table Syntax
```sql
# MySQL
CREATE TABLE t (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (col)
) ENGINE=InnoDB CHARSET=utf8mb4;

# PostgreSQL
CREATE TABLE t (
    id BIGSERIAL PRIMARY KEY,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_name ON t(col);
CREATE TRIGGER ... EXECUTE FUNCTION update_updated_at_column();
```

---

## 📁 Files Changed

### Configuration
- ✅ `application.conf` - Port 5432, new user/password
- ✅ `build.gradle.kts` - Removed MySQL, kept PostgreSQL
- ✅ `DatabaseConfig.kt` - PostgreSQL-only logic

### Migrations
- ✅ `V1__init.sql` - Users with UUID, ENUMs, triggers
- ✅ `V2__products.sql` - Products with NUMERIC, triggers
- ✅ `V3__update_products_android_match.sql` - No changes
- ✅ `V4__inventory_transactions.sql` - BIGSERIAL, UUID actor

### New Files
- ✅ `setup-postgresql.sql` - Database creation script
- ✅ `POSTGRESQL_MIGRATION.md` - Complete guide

---

## 🔌 Connection Info

```properties
Host: localhost
Port: 5432
Database: ganeshkulfi_db
User: ganeshkulfi_user
Password: Ganesh@123
```

---

## ✅ Verification Commands

```bash
# 1. Check PostgreSQL running
pg_isready -h localhost -p 5432

# 2. Connect to database
psql -U ganeshkulfi_user -d ganeshkulfi_db -h localhost

# 3. List tables
\dt

# 4. Check migrations
SELECT * FROM flyway_schema_history;

# 5. Count records
SELECT COUNT(*) FROM app_user;    -- Should be 2
SELECT COUNT(*) FROM product;     -- Should be 13
SELECT COUNT(*) FROM inventory_tx; -- Should be 13
```

---

## 🆘 Troubleshooting

### Database doesn't exist
```bash
psql -U postgres -c "CREATE DATABASE ganeshkulfi_db;"
```

### User doesn't exist
```bash
psql -U postgres -c "CREATE USER ganeshkulfi_user WITH PASSWORD 'Ganesh@123';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ganeshkulfi_db TO ganeshkulfi_user;"
```

### PostgreSQL not running
```bash
# Windows: Services → postgresql-x64-14 → Start
# Mac: brew services start postgresql@14
# Linux: sudo systemctl start postgresql
```

### Can't connect
```bash
# Check if PostgreSQL is listening
sudo netstat -plnt | grep 5432

# Check pg_hba.conf allows local connections
# Windows: C:\Program Files\PostgreSQL\14\data\pg_hba.conf
# Add: host all all 127.0.0.1/32 md5
```

---

## 📊 What Was Migrated

| Component | Status |
|-----------|--------|
| User table (UUID, ENUMs, triggers) | ✅ |
| Product table (NUMERIC, triggers) | ✅ |
| Inventory table (BIGSERIAL) | ✅ |
| Admin user (admin@ganeshkulfi.com) | ✅ |
| Retailer user (retailer@test.com) | ✅ |
| 13 kulfi flavors | ✅ |
| Initial stock ledger | ✅ |
| Flyway migrations | ✅ |
| Dependencies | ✅ |
| Configuration | ✅ |

---

**Status:** ✅ Migration Complete  
**Ready to deploy:** Yes  
**Tested:** All endpoints working
