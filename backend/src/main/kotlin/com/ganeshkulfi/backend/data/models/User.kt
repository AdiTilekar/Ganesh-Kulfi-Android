package com.ganeshkulfi.backend.data.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

/**
 * User Roles for Role-Based Access Control
 */
enum class UserRole {
    ADMIN,      // Factory owner - full access
    RETAILER,   // Bulk buyer - can place orders
    CUSTOMER,   // End user - can buy products
    GUEST       // Temporary user
}

/**
 * Pricing Tiers for Retailers
 */
enum class PricingTier(val discountPercentage: Double) {
    VIP(25.0),        // 25% discount
    PREMIUM(15.0),    // 15% discount
    REGULAR(10.0),    // 10% discount
    RETAIL(5.0)       // 5% discount
}

/**
 * Users Table - Exposed ORM Table Definition
 * Maps to app_user table in PostgreSQL
 */
object Users : UUIDTable("app_user") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val phone = varchar("phone", 20).nullable()
    val role = enumerationByName("role", 50, UserRole::class).default(UserRole.CUSTOMER)
    
    // Retailer-specific fields
    val retailerId = varchar("retailer_id", 50).nullable().uniqueIndex()
    val shopName = varchar("shop_name", 200).nullable()
    val tier = enumerationByName("tier", 50, PricingTier::class).nullable()
    
    // Timestamps
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

/**
 * User Domain Model
 * Represents a user in the system
 */
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val retailerId: String? = null,
    val shopName: String? = null,
    val tier: PricingTier? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
