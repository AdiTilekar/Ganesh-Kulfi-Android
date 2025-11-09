package com.ganeshkulfi.app.data.repository

import android.content.SharedPreferences
// import com.ganeshkulfi.app.data.auth.GoogleSignInHelper  // Uncomment when Firebase is ready
import com.ganeshkulfi.app.data.model.User
import com.ganeshkulfi.app.data.model.UserRole
import com.ganeshkulfi.app.data.model.PricingTier
import com.ganeshkulfi.app.data.model.UserRoleMapper
// import com.google.firebase.auth.FirebaseUser  // Uncomment when Firebase is ready
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
    // private val googleSignInHelper: GoogleSignInHelper  // Uncomment when Firebase is ready
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUserFlow: Flow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        try {
            var userId = sharedPreferences.getString(KEY_USER_ID, null)

            if (userId == null) {
                // No persisted user id — but maybe credentials were stored for re-login.
                val storedEmail = sharedPreferences.getString(KEY_STORED_EMAIL, null)
                if (storedEmail != null) {
                    // Reconstruct a minimal user from stored prefs and persist an id so future loads succeed.
                    val email = storedEmail
                    val name = sharedPreferences.getString(KEY_NAME, "") ?: ""
                    val phone = sharedPreferences.getString(KEY_PHONE, "") ?: ""
                    val roleString = sharedPreferences.getString(KEY_ROLE, UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
                    val role = try {
                        UserRole.valueOf(roleString)
                    } catch (e: IllegalArgumentException) {
                        UserRole.CUSTOMER
                    }

                    val retailerId = sharedPreferences.getString(KEY_RETAILER_ID, null)
                    val shopName = sharedPreferences.getString(KEY_SHOP_NAME, null)
                    val pricingTierString = sharedPreferences.getString(KEY_PRICING_TIER, null)
                    val pricingTier = pricingTierString?.let {
                        try {
                            PricingTier.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

                    // Generate and persist a user id so subsequent loadCurrentUser calls succeed.
                    userId = "user_${System.currentTimeMillis()}"
                    with(sharedPreferences.edit()) {
                        putString(KEY_USER_ID, userId)
                        putString(KEY_EMAIL, email)
                        putString(KEY_NAME, name)
                        putString(KEY_PHONE, phone)
                        putString(KEY_ROLE, role.name)
                        apply()
                    }

                    _currentUser.value = User(
                        id = userId,
                        email = email,
                        name = name,
                        phone = phone,
                        role = role,
                        retailerId = retailerId,
                        shopName = shopName,
                        pricingTier = pricingTier
                    )
                }
            } else {
                val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
                val name = sharedPreferences.getString(KEY_NAME, "") ?: ""
                val phone = sharedPreferences.getString(KEY_PHONE, "") ?: ""
                val roleString = sharedPreferences.getString(KEY_ROLE, UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
                val role = try {
                    UserRole.valueOf(roleString)
                } catch (e: IllegalArgumentException) {
                    UserRole.CUSTOMER
                }

                // Load retailer-specific data if role is RETAILER
                val retailerId = sharedPreferences.getString(KEY_RETAILER_ID, null)
                val shopName = sharedPreferences.getString(KEY_SHOP_NAME, null)
                val pricingTierString = sharedPreferences.getString(KEY_PRICING_TIER, null)
                val pricingTier = pricingTierString?.let {
                    try {
                        PricingTier.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }

                _currentUser.value = User(
                    id = userId,
                    email = email,
                    name = name,
                    phone = phone,
                    role = role,
                    retailerId = retailerId,
                    shopName = shopName,
                    pricingTier = pricingTier
                )
            }
        } catch (e: Exception) {
            // Clear corrupted data and start fresh
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            _currentUser.value = null
        }
    }

    fun isUserLoggedIn(): Boolean = _currentUser.value != null

    suspend fun signUp(email: String, password: String, name: String, phone: String): Result<User> {
        return try {
            // Simple validation
            if (email.isBlank() || password.length < 6) {
                throw Exception("Invalid email or password too short")
            }

            // Check if user already exists
            val existingEmail = sharedPreferences.getString(KEY_STORED_EMAIL, null)
            if (existingEmail == email) {
                throw Exception("User already exists")
            }

            // Create user
            val userId = "user_${System.currentTimeMillis()}"
            val user = User(
                id = userId,
                email = email,
                name = name,
                phone = phone,
                role = UserRole.CUSTOMER
            )

            // Save user data
            with(sharedPreferences.edit()) {
                putString(KEY_USER_ID, userId)
                putString(KEY_EMAIL, email)
                putString(KEY_NAME, name)
                putString(KEY_PHONE, phone)
                putString(KEY_ROLE, user.role.name)
                putString(KEY_STORED_EMAIL, email)
                putString(KEY_PASSWORD, password) // In production, use proper encryption
                apply()
            }

            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign up a retailer account and persist retailer-specific session data.
     * This will store credentials and retailer metadata so the created account
     * can sign in as a RETAILER.
     * 
     * NOTE: This changes the current user session to the newly created retailer.
     * Use registerRetailerCredentials() if you want to create credentials without logging in.
     */
    suspend fun signUpRetailer(
        email: String,
        password: String,
        name: String,
        phone: String,
        retailerId: String,
        shopName: String,
        pricingTier: PricingTier
    ): Result<User> {
        return try {
            if (email.isBlank() || password.length < 6) {
                throw Exception("Invalid email or password too short")
            }

            val existingEmail = sharedPreferences.getString(KEY_STORED_EMAIL, null)
            if (existingEmail == email) {
                throw Exception("User already exists")
            }

            // Create user with RETAILER role
            val userId = "user_${System.currentTimeMillis()}"
            val user = User(
                id = userId,
                email = email,
                name = name,
                phone = phone,
                role = UserRole.RETAILER,
                retailerId = retailerId,
                shopName = shopName,
                pricingTier = pricingTier
            )

            // Save session and credentials
            with(sharedPreferences.edit()) {
                putString(KEY_USER_ID, userId)
                putString(KEY_EMAIL, email)
                putString(KEY_NAME, name)
                putString(KEY_PHONE, phone)
                putString(KEY_ROLE, user.role.name)

                putString(KEY_RETAILER_ID, retailerId)
                putString(KEY_SHOP_NAME, shopName)
                putString(KEY_PRICING_TIER, pricingTier.name)

                putString(KEY_STORED_EMAIL, email)
                putString(KEY_PASSWORD, password) // In production, encrypt
                apply()
            }

            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register retailer credentials WITHOUT changing the current user session.
     * Use this when admin creates a retailer account - the admin stays logged in.
     */
    suspend fun registerRetailerCredentials(
        email: String,
        password: String,
        name: String,
        phone: String,
        retailerId: String,
        shopName: String,
        pricingTier: PricingTier
    ): Result<Unit> {
        return try {
            if (email.isBlank() || password.length < 6) {
                throw Exception("Invalid email or password too short")
            }

            // Store credentials in a separate key pattern to avoid conflict with current user
            val credentialKey = "retailer_cred_$email"
            
            with(sharedPreferences.edit()) {
                // Store retailer credentials for future sign-in
                putString("${credentialKey}_email", email)
                putString("${credentialKey}_password", password) // In production, encrypt
                putString("${credentialKey}_name", name)
                putString("${credentialKey}_phone", phone)
                putString("${credentialKey}_retailer_id", retailerId)
                putString("${credentialKey}_shop_name", shopName)
                putString("${credentialKey}_pricing_tier", pricingTier.name)
                apply()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            // Check for hardcoded admin credentials first
            if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                val adminUser = User(
                    id = "admin_001",
                    email = ADMIN_EMAIL,
                    name = "Admin",
                    phone = "9876543210",
                    role = UserRole.ADMIN
                )
                
                // Save admin session
                with(sharedPreferences.edit()) {
                    putString(KEY_USER_ID, adminUser.id)
                    putString(KEY_EMAIL, adminUser.email)
                    putString(KEY_NAME, adminUser.name)
                    putString(KEY_PHONE, adminUser.phone)
                    putString(KEY_ROLE, adminUser.role.name)
                    apply()
                }
                
                _currentUser.value = adminUser
                return Result.success(adminUser)
            }
            
            // Check for hardcoded test retailer credentials
            if (email == RETAILER_EMAIL && password == RETAILER_PASSWORD) {
                val retailerUser = User(
                    id = "user_ret_001",
                    email = RETAILER_EMAIL,
                    name = "Rajesh Kumar",
                    phone = "9876543210",
                    role = UserRole.RETAILER,
                    retailerId = "ret_001",
                    shopName = "Kumar Sweet Shop",
                    pricingTier = PricingTier.VIP
                )
                
                // Save retailer session
                with(sharedPreferences.edit()) {
                    putString(KEY_USER_ID, retailerUser.id)
                    putString(KEY_EMAIL, retailerUser.email)
                    putString(KEY_NAME, retailerUser.name)
                    putString(KEY_PHONE, retailerUser.phone)
                    putString(KEY_ROLE, retailerUser.role.name)
                    putString(KEY_RETAILER_ID, retailerUser.retailerId)
                    putString(KEY_SHOP_NAME, retailerUser.shopName)
                    putString(KEY_PRICING_TIER, retailerUser.pricingTier?.name)
                    apply()
                }
                
                _currentUser.value = retailerUser
                return Result.success(retailerUser)
            }
            
            // Check for retailer credentials registered via admin
            val credentialKey = "retailer_cred_$email"
            val storedRetailerEmail = sharedPreferences.getString("${credentialKey}_email", null)
            val storedRetailerPassword = sharedPreferences.getString("${credentialKey}_password", null)
            
            if (storedRetailerEmail == email && storedRetailerPassword == password) {
                // Load retailer data and create session
                val name = sharedPreferences.getString("${credentialKey}_name", "") ?: ""
                val phone = sharedPreferences.getString("${credentialKey}_phone", "") ?: ""
                val retailerId = sharedPreferences.getString("${credentialKey}_retailer_id", null)
                val shopName = sharedPreferences.getString("${credentialKey}_shop_name", null)
                val pricingTierString = sharedPreferences.getString("${credentialKey}_pricing_tier", null)
                val pricingTier = pricingTierString?.let {
                    try {
                        PricingTier.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                
                val userId = "user_${System.currentTimeMillis()}"
                val retailerUser = User(
                    id = userId,
                    email = email,
                    name = name,
                    phone = phone,
                    role = UserRole.RETAILER,
                    retailerId = retailerId,
                    shopName = shopName,
                    pricingTier = pricingTier
                )
                
                // Save current session
                with(sharedPreferences.edit()) {
                    putString(KEY_USER_ID, userId)
                    putString(KEY_EMAIL, email)
                    putString(KEY_NAME, name)
                    putString(KEY_PHONE, phone)
                    putString(KEY_ROLE, UserRole.RETAILER.name)
                    putString(KEY_RETAILER_ID, retailerId)
                    putString(KEY_SHOP_NAME, shopName)
                    putString(KEY_PRICING_TIER, pricingTier?.name)
                    apply()
                }
                
                _currentUser.value = retailerUser
                return Result.success(retailerUser)
            }
            
            // Check regular user credentials
            val storedEmail = sharedPreferences.getString(KEY_STORED_EMAIL, null)
            val storedPassword = sharedPreferences.getString(KEY_PASSWORD, null)

            if (storedEmail == email && storedPassword == password) {
                loadCurrentUser()
                val user = _currentUser.value ?: throw Exception("Failed to load user")
                Result.success(user)
            } else {
                throw Exception("Invalid email or password")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        // Sign out from Google (Uncomment when Firebase is ready)
        // googleSignInHelper.signOut()
        
        with(sharedPreferences.edit()) {
            remove(KEY_USER_ID)
            remove(KEY_EMAIL)
            remove(KEY_NAME)
            remove(KEY_PHONE)
            remove(KEY_ROLE)
            remove(KEY_IS_GUEST)
            remove(KEY_RETAILER_ID)
            remove(KEY_SHOP_NAME)
            remove(KEY_PRICING_TIER)
            // Keep stored credentials for re-login (unless guest)
            apply()
        }
        _currentUser.value = null
    }
    
    /* ============================================================
     * OAuth Methods - Uncomment when Firebase is connected
     * ============================================================
     
    /**
     * OAuth Sign-In with Google
     * Automatically detects role based on email
     */
    suspend fun signInWithGoogle(firebaseUser: FirebaseUser): Result<User> {
        return try {
            val email = firebaseUser.email ?: throw Exception("Email not found")
            val name = firebaseUser.displayName ?: "User"
            val photoUrl = firebaseUser.photoUrl?.toString()
            
            // Detect role based on email
            val role = UserRoleMapper.getUserRole(email)
            
            // Create user based on role
            val user = when (role) {
                UserRole.ADMIN -> {
                    User(
                        id = firebaseUser.uid,
                        email = email,
                        name = name,
                        phone = "",  // Can be updated later
                        role = UserRole.ADMIN
                    )
                }
                UserRole.RETAILER -> {
                    val retailerInfo = UserRoleMapper.getRetailerInfo(email)
                        ?: throw Exception("Retailer information not found for: $email")
                    
                    User(
                        id = firebaseUser.uid,
                        email = email,
                        name = retailerInfo.name,
                        phone = retailerInfo.phone,
                        role = UserRole.RETAILER,
                        retailerId = retailerInfo.id,
                        shopName = retailerInfo.shopName,
                        pricingTier = retailerInfo.pricingTier
                    )
                }
                UserRole.CUSTOMER -> {
                    User(
                        id = firebaseUser.uid,
                        email = email,
                        name = name,
                        phone = "",
                        role = UserRole.CUSTOMER
                    )
                }
            }
            
            // Save user session
            with(sharedPreferences.edit()) {
                putString(KEY_USER_ID, user.id)
                putString(KEY_EMAIL, user.email)
                putString(KEY_NAME, user.name)
                putString(KEY_PHONE, user.phone)
                putString(KEY_ROLE, user.role.name)
                
                // Save retailer-specific data
                if (user.role == UserRole.RETAILER) {
                    putString(KEY_RETAILER_ID, user.retailerId)
                    putString(KEY_SHOP_NAME, user.shopName)
                    putString(KEY_PRICING_TIER, user.pricingTier?.name)
                }
                
                apply()
            }
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Initiates Google Sign-In flow
     * Returns IntentSender to launch Google Sign-In UI
     */
    suspend fun initiateGoogleSignIn(): android.content.IntentSender? {
        return googleSignInHelper.signIn()
    }
    
    /**
     * Handles Google Sign-In result
     */
    suspend fun handleGoogleSignInResult(intent: android.content.Intent): Result<User> {
        return try {
            val firebaseUserResult = googleSignInHelper.handleSignInResult(intent)
            
            if (firebaseUserResult.isSuccess) {
                val firebaseUser = firebaseUserResult.getOrThrow()
                signInWithGoogle(firebaseUser)
            } else {
                Result.failure(firebaseUserResult.exceptionOrNull() ?: Exception("Sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    ============================================================ */

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val user = _currentUser.value ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val storedEmail = sharedPreferences.getString(KEY_STORED_EMAIL, null)
            if (storedEmail == email) {
                // In a real app, send email or SMS
                Result.success(Unit)
            } else {
                throw Exception("Email not found")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun continueAsGuest(): Result<User> {
        return try {
            val guestUser = User(
                id = "guest_${System.currentTimeMillis()}",
                email = "guest@ganeshkulfi.com",
                name = "Guest User",
                phone = "",
                role = UserRole.CUSTOMER
            )

            // Save guest status (temporary, won't persist on app restart)
            with(sharedPreferences.edit()) {
                putString(KEY_USER_ID, guestUser.id)
                putString(KEY_EMAIL, guestUser.email)
                putString(KEY_NAME, guestUser.name)
                putString(KEY_PHONE, guestUser.phone)
                putString(KEY_ROLE, guestUser.role.name)
                putBoolean(KEY_IS_GUEST, true)
                apply()
            }

            _currentUser.value = guestUser
            Result.success(guestUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isGuestUser(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_GUEST, false)
    }

    companion object {
        // Admin credentials (hardcoded for demo)
        private const val ADMIN_EMAIL = "admin@ganeshkulfi.com"
        private const val ADMIN_PASSWORD = "admin123"
        
        // Test Retailer credentials (hardcoded for demo)
        private const val RETAILER_EMAIL = "retailer@test.com"
        private const val RETAILER_PASSWORD = "retailer123"
        
        // SharedPreferences keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_STORED_EMAIL = "stored_email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_RETAILER_ID = "retailer_id"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_PRICING_TIER = "pricing_tier"
    }
}
