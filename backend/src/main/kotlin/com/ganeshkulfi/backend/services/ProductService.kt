package com.ganeshkulfi.backend.services

import com.ganeshkulfi.backend.data.dto.CreateProductRequest
import com.ganeshkulfi.backend.data.dto.ProductResponse
import com.ganeshkulfi.backend.data.dto.UpdateProductRequest
import com.ganeshkulfi.backend.data.models.ProductCategory
import com.ganeshkulfi.backend.data.repository.ProductRepository

/**
 * Product Service
 * Business logic for product operations
 */
class ProductService(private val productRepository: ProductRepository) {
    
    /**
     * Get all products
     */
    fun getAllProducts(): Result<List<ProductResponse>> {
        val products = productRepository.findAll()
        return Result.success(products.map { ProductResponse.fromProduct(it) })
    }
    
    /**
     * Get available products only
     */
    fun getAvailableProducts(): Result<List<ProductResponse>> {
        val products = productRepository.findAvailable()
        return Result.success(products.map { ProductResponse.fromProduct(it) })
    }
    
    /**
     * Get seasonal products
     */
    fun getSeasonalProducts(): Result<List<ProductResponse>> {
        val products = productRepository.findSeasonal()
        return Result.success(products.map { ProductResponse.fromProduct(it) })
    }
    
    /**
     * Get product by ID
     */
    fun getProductById(id: String): Result<ProductResponse> {
        val product = productRepository.findById(id)
            ?: return Result.failure(IllegalArgumentException("Product not found"))
        
        return Result.success(ProductResponse.fromProduct(product))
    }
    
    /**
     * Get products by category
     */
    fun getProductsByCategory(categoryName: String): Result<List<ProductResponse>> {
        val category = try {
            ProductCategory.valueOf(categoryName.uppercase())
        } catch (e: IllegalArgumentException) {
            return Result.failure(IllegalArgumentException("Invalid category: $categoryName"))
        }
        
        val products = productRepository.findByCategory(category)
        return Result.success(products.map { ProductResponse.fromProduct(it) })
    }
    
    /**
     * Create new product (Admin only)
     */
    fun createProduct(request: CreateProductRequest): Result<ProductResponse> {
        // Validate product name
        if (request.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name is required"))
        }
        
        // Check if name already exists
        if (productRepository.nameExists(request.name)) {
            return Result.failure(IllegalArgumentException("Product with this name already exists"))
        }
        
        // Validate price
        if (request.basePrice <= 0) {
            return Result.failure(IllegalArgumentException("Price must be greater than zero"))
        }
        
        // Validate category
        val category = try {
            ProductCategory.valueOf(request.category.uppercase())
        } catch (e: IllegalArgumentException) {
            return Result.failure(IllegalArgumentException("Invalid category: ${request.category}"))
        }
        
        // Create product
        val product = productRepository.create(
            name = request.name,
            description = request.description,
            basePrice = request.basePrice,
            category = category,
            imageUrl = request.imageUrl,
            isAvailable = request.isAvailable,
            isSeasonal = request.isSeasonal,
            stockQuantity = request.stockQuantity,
            minOrderQuantity = request.minOrderQuantity
        ) ?: return Result.failure(IllegalStateException("Failed to create product"))
        
        return Result.success(ProductResponse.fromProduct(product))
    }
    
    /**
     * Update product (Admin only)
     */
    fun updateProduct(id: String, request: UpdateProductRequest): Result<ProductResponse> {
        // Check if product exists
        val existingProduct = productRepository.findById(id)
            ?: return Result.failure(IllegalArgumentException("Product not found"))
        
        // Build updates map
        val updates = mutableMapOf<String, Any?>()
        
        request.name?.let {
            if (it.isBlank()) {
                return Result.failure(IllegalArgumentException("Product name cannot be empty"))
            }
            // Check if new name already exists (excluding current product)
            if (it != existingProduct.name && productRepository.nameExists(it)) {
                return Result.failure(IllegalArgumentException("Product with this name already exists"))
            }
            updates["name"] = it
        }
        
        request.description?.let { updates["description"] = it }
        
        request.basePrice?.let {
            if (it <= 0) {
                return Result.failure(IllegalArgumentException("Price must be greater than zero"))
            }
            updates["basePrice"] = it
        }
        
        request.category?.let { categoryStr ->
            val category = try {
                ProductCategory.valueOf(categoryStr.uppercase())
            } catch (e: IllegalArgumentException) {
                return Result.failure(IllegalArgumentException("Invalid category: $categoryStr"))
            }
            updates["category"] = category
        }
        
        request.imageUrl?.let { updates["imageUrl"] = it }
        request.isAvailable?.let { updates["isAvailable"] = it }
        request.isSeasonal?.let { updates["isSeasonal"] = it }
        request.stockQuantity?.let { updates["stockQuantity"] = it }
        request.minOrderQuantity?.let { updates["minOrderQuantity"] = it }
        
        // Update product
        val updatedProduct = productRepository.update(id, updates)
            ?: return Result.failure(IllegalStateException("Failed to update product"))
        
        return Result.success(ProductResponse.fromProduct(updatedProduct))
    }
    
    /**
     * Delete product (Admin only)
     */
    fun deleteProduct(id: String): Result<Unit> {
        val deleted = productRepository.delete(id)
        return if (deleted) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Product not found"))
        }
    }
    
    /**
     * Update stock quantity
     */
    fun updateStock(id: String, quantity: Int): Result<Unit> {
        if (quantity < 0) {
            return Result.failure(IllegalArgumentException("Stock quantity cannot be negative"))
        }
        
        val updated = productRepository.updateStock(id, quantity)
        return if (updated) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Product not found"))
        }
    }
}
