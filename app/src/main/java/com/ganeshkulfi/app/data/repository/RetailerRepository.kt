package com.ganeshkulfi.app.data.repository

import com.ganeshkulfi.app.data.model.Retailer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetailerRepository @Inject constructor() {
    
    private val _retailers = MutableStateFlow<List<Retailer>>(Retailer.getSampleRetailers())
    val retailersFlow: Flow<List<Retailer>> = _retailers.asStateFlow()

    suspend fun getAllRetailers(): Result<List<Retailer>> {
        return try {
            Result.success(_retailers.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRetailerById(id: String): Result<Retailer> {
        return try {
            val retailer = _retailers.value.find { it.id == id }
            if (retailer != null) {
                Result.success(retailer)
            } else {
                Result.failure(Exception("Retailer not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRetailer(retailer: Retailer): Result<Retailer> {
        return try {
            val newRetailer = retailer.copy(
                id = "ret_${System.currentTimeMillis()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            _retailers.value = _retailers.value + newRetailer
            Result.success(newRetailer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRetailer(retailer: Retailer): Result<Retailer> {
        return try {
            val updatedRetailer = retailer.copy(updatedAt = System.currentTimeMillis())
            _retailers.value = _retailers.value.map {
                if (it.id == retailer.id) updatedRetailer else it
            }
            Result.success(updatedRetailer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRetailer(id: String): Result<Unit> {
        return try {
            _retailers.value = _retailers.value.filter { it.id != id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOutstanding(retailerId: String, amount: Double): Result<Unit> {
        return try {
            _retailers.value = _retailers.value.map { retailer ->
                if (retailer.id == retailerId) {
                    retailer.copy(
                        totalOutstanding = retailer.totalOutstanding + amount,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    retailer
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRetailers(): Result<List<Retailer>> {
        return try {
            Result.success(_retailers.value.filter { it.isActive })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRetailerAccount(retailer: Retailer, email: String, password: String): Result<Retailer> {
        return try {
            // Create the retailer with credentials
            val newRetailer = retailer.copy(
                id = "ret_${System.currentTimeMillis()}",
                email = email,
                password = password, // In production, this should be hashed
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Add to retailers list
            _retailers.value = _retailers.value + newRetailer
            
            // In a real Firebase implementation, you would also create the Firebase Auth user here:
            // firebaseAuth.createUserWithEmailAndPassword(email, password)
            //     .addOnSuccessListener { authResult ->
            //         // Store user data in Firestore
            //     }
            
            Result.success(newRetailer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
