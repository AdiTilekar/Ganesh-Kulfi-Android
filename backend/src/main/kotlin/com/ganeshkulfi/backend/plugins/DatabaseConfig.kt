package com.ganeshkulfi.backend.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

/**
 * Database Configuration
 * Sets up database connection with HikariCP connection pool
 * and runs Flyway migrations
 * 
 * Supports: PostgreSQL (default) and MySQL
 */
object DatabaseConfig {
    
    fun init(environment: ApplicationEnvironment) {
        val config = environment.config
        
        // Read database config from application.conf
        val host = config.property("database.host").getString()
        val port = config.property("database.port").getString()
        val dbName = config.property("database.name").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()
        
        // Auto-detect database type from port (3306 = MySQL, 5432 = PostgreSQL)
        val isMySQL = port == "3306"
        val jdbcUrl = if (isMySQL) {
            "jdbc:mysql://$host:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        } else {
            "jdbc:postgresql://$host:$port/$dbName"
        }
        val driverClass = if (isMySQL) "com.mysql.cj.jdbc.Driver" else "org.postgresql.Driver"
        
        // Configure HikariCP connection pool
        val dataSource = createDataSource(jdbcUrl, user, password, maxPoolSize, driverClass)
        
        // Run Flyway migrations
        runMigrations(dataSource, isMySQL)
        
        // Connect Exposed ORM
        Database.connect(dataSource)
        
        val dbType = if (isMySQL) "MySQL" else "PostgreSQL"
        environment.log.info("✅ $dbType database connected: $jdbcUrl")
    }
    
    private fun createDataSource(
        jdbcUrl: String,
        user: String,
        password: String,
        maxPoolSize: Int,
        driverClass: String
    ): HikariDataSource {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.maximumPoolSize = maxPoolSize
            this.driverClassName = driverClass
            
            // Connection pool settings
            this.connectionTimeout = 30000 // 30 seconds
            this.idleTimeout = 600000 // 10 minutes
            this.maxLifetime = 1800000 // 30 minutes
            
            // Performance tuning
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            
            // Pool naming
            this.poolName = "GaneshKulfiHikariPool"
        }
        
        return HikariDataSource(config)
    }
    
    private fun runMigrations(dataSource: DataSource, isMySQL: Boolean) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(false)  // Allow modified migrations (V2 was updated to match Android app)
            .load()
        
        val migrationInfo = flyway.info()
        val pending = migrationInfo.pending().size
        val dbType = if (isMySQL) "MySQL" else "PostgreSQL"
        
        if (pending > 0) {
            println("⏳ Running $pending pending $dbType Flyway migration(s)...")
            flyway.migrate()
            println("✅ Flyway migrations completed successfully!")
        } else {
            println("✅ No pending Flyway migrations")
        }
    }
}
