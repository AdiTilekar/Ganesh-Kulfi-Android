package com.ganeshkulfi.app.data.model

/**
 * Retailer-specific pricing model
 * Allows different prices for different retailers based on their tier or custom pricing
 */
data class RetailerPricing(
    val id: String = "",
    val retailerId: String = "",
    val flavorId: String = "",
    val customPrice: Double? = null,  // If set, overrides tier pricing
    val discount: Double = 0.0,       // Percentage discount (0-100)
    val minimumQuantity: Int = 0,     // Minimum order quantity for this price
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Pricing tiers for categorizing retailers
 */
enum class PricingTier(
    val displayName: String,
    val discountPercentage: Double,
    val description: String
) {
    VIP(
        displayName = "VIP Tier",
        discountPercentage = 25.0,
        description = "Premium retailers with highest volume - 25% discount"
    ),
    PREMIUM(
        displayName = "Premium Tier",
        discountPercentage = 15.0,
        description = "High volume retailers - 15% discount"
    ),
    REGULAR(
        displayName = "Regular Tier",
        discountPercentage = 10.0,
        description = "Standard retailers - 10% discount"
    ),
    WHOLESALE(
        displayName = "Wholesale",
        discountPercentage = 5.0,
        description = "Bulk buyers - 5% discount"
    ),
    RETAIL(
        displayName = "Retail",
        discountPercentage = 0.0,
        description = "Small retailers - Standard price"
    ),
    CUSTOM(
        displayName = "Custom Pricing",
        discountPercentage = 0.0,
        description = "Custom negotiated prices"
    );

    /**
     * Calculate discounted price based on base price
     */
    fun calculatePrice(basePrice: Double): Double {
        return basePrice * (1 - discountPercentage / 100)
    }
}

/**
 * Price information for a specific retailer and flavor
 */
data class PriceInfo(
    val basePrice: Double,
    val retailerPrice: Double,
    val discount: Double,
    val pricingTier: PricingTier,
    val isCustomPrice: Boolean = false,
    val minimumQuantity: Int = 0,
    val savings: Double = basePrice - retailerPrice
) {
    val discountPercentage: Double
        get() = if (basePrice > 0) ((basePrice - retailerPrice) / basePrice) * 100 else 0.0
}

/**
 * Bulk pricing rules - quantity-based discounts
 */
data class BulkPricingRule(
    val id: String = "",
    val flavorId: String = "",
    val minimumQuantity: Int = 0,
    val discountPercentage: Double = 0.0,
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Default bulk pricing rules
         */
        fun getDefaultRules(): List<BulkPricingRule> = listOf(
            BulkPricingRule(
                id = "bulk_001",
                flavorId = "all",  // Applies to all flavors
                minimumQuantity = 100,
                discountPercentage = 5.0
            ),
            BulkPricingRule(
                id = "bulk_002",
                flavorId = "all",
                minimumQuantity = 200,
                discountPercentage = 10.0
            ),
            BulkPricingRule(
                id = "bulk_003",
                flavorId = "all",
                minimumQuantity = 500,
                discountPercentage = 15.0
            ),
            BulkPricingRule(
                id = "bulk_004",
                flavorId = "all",
                minimumQuantity = 1000,
                discountPercentage = 20.0
            )
        )
    }
}
