package com.ganeshkulfi.backend.data.dto

import com.ganeshkulfi.backend.data.models.Product
import com.ganeshkulfi.backend.data.models.ProductCategory
import kotlinx.serialization.Serializable

/**
 * Product Response DTO
 */
@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val category: String,
    val imageUrl: String?,
    val isAvailable: Boolean,
    val isSeasonal: Boolean,
    val stockQuantity: Int,
    val minOrderQuantity: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromProduct(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id,
                name = product.name,
                description = product.description,
                basePrice = product.basePrice,
                category = product.category.name,
                imageUrl = product.imageUrl,
                isAvailable = product.isAvailable,
                isSeasonal = product.isSeasonal,
                stockQuantity = product.stockQuantity,
                minOrderQuantity = product.minOrderQuantity,
                createdAt = product.createdAt.toString(),
                updatedAt = product.updatedAt.toString()
            )
        }
    }
}

/**
 * Create Product Request DTO
 */
@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String?,
    val basePrice: Double,
    val category: String,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val isSeasonal: Boolean = false,
    val stockQuantity: Int = 0,
    val minOrderQuantity: Int = 1
)

/**
 * Update Product Request DTO
 */
@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val basePrice: Double? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean? = null,
    val isSeasonal: Boolean? = null,
    val stockQuantity: Int? = null,
    val minOrderQuantity: Int? = null
)

/**
 * Products List Response
 */
@Serializable
data class ProductsListResponse(
    val products: List<ProductResponse>,
    val total: Int,
    val category: String? = null
)
