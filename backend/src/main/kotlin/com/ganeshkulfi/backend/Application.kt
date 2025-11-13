package com.ganeshkulfi.backend

import com.ganeshkulfi.backend.data.repository.UserRepository
import com.ganeshkulfi.backend.data.repository.ProductRepository
import com.ganeshkulfi.backend.plugins.*
import com.ganeshkulfi.backend.routes.authRoutes
import com.ganeshkulfi.backend.routes.healthRoutes
import com.ganeshkulfi.backend.routes.userRoutes
import com.ganeshkulfi.backend.routes.productRoutes
import com.ganeshkulfi.backend.services.JWTService
import com.ganeshkulfi.backend.services.PasswordService
import com.ganeshkulfi.backend.services.UserService
import com.ganeshkulfi.backend.services.ProductService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

/**
 * Ganesh Kulfi Backend Application
 * Day 1: Basic setup with Ktor + PostgreSQL + Flyway
 * Day 2: JWT Authentication + User Management
 * Day 3: Product Management + 13 Kulfi Flavors
 */

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // Print startup banner
    printBanner()
    
    // Initialize database connection and run migrations
    log.info("🔧 Initializing database connection...")
    DatabaseConfig.init(environment)
    
    // Initialize services
    log.info("🔧 Initializing services...")
    val jwtService = JWTService(environment.config)
    val passwordService = PasswordService()
    val userRepository = UserRepository()
    val userService = UserService(userRepository, passwordService, jwtService)
    val productRepository = ProductRepository()
    val productService = ProductService(productRepository)
    
    // Configure plugins
    log.info("🔧 Configuring Ktor plugins...")
    configureSerialization()
    configureCORS()
    configureLogging()
    configureAuthentication(jwtService)
    
    // Configure routes
    log.info("🔧 Configuring API routes...")
    routing {
        healthRoutes()
        authRoutes(userService)
        userRoutes(userService)
        productRoutes(productService)
    }
    
    log.info("✅ Ganesh Kulfi Backend is ready!")
    log.info("📍 Server running at: http://localhost:8080")
    log.info("🏥 Health check: http://localhost:8080/api/health")
    log.info("🔐 Auth endpoints: http://localhost:8080/api/auth/*")
    log.info("👥 User endpoints: http://localhost:8080/api/users/*")
    log.info("🍦 Product endpoints: http://localhost:8080/api/products/*")
}

fun printBanner() {
    println("""
        
    ╔═══════════════════════════════════════════════╗
    ║                                               ║
    ║        🍦 GANESH KULFI BACKEND 🍦             ║
    ║                                               ║
    ║   Authentic Taste of Tradition - Now Digital ║
    ║                                               ║
    ╚═══════════════════════════════════════════════╝
    
    Day 1: Ktor + PostgreSQL + Flyway + Exposed
    Day 2: JWT Authentication + User Management
    Day 3: Product Management + 13 Kulfi Flavors
    Version: 0.0.3-SNAPSHOT
    
    """.trimIndent())
}
