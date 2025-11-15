# Day 10 Testing Guide - Order Cancellation Features

## 🚀 Quick Start

### 1. Start the Backend Server

```powershell
cd "e:\Ganesh Kulfi web\KulfiDelightAndroid\backend"
java -jar build/libs/ganeshkulfi-backend-all.jar
```

**Wait for**: `✅ Ganesh Kulfi Backend is ready!`

The V9 migration will apply automatically on first run.

---

## 🧪 Testing Methods

### Method 1: Use Postman/Thunder Client (Recommended)

#### Step 1: Login to Get JWT Token

**POST** `http://localhost:8080/api/auth/login`

**Body (Retailer)**:
```json
{
  "email": "retailer@test.com",
  "password": "password123"
}
```

**Body (Admin)**:
```json
{
  "email": "admin@test.com",
  "password": "password123"
}
```

**Copy the `token` from response** - you'll need it for all other requests.

---

#### Step 2: Create a Test Order (Retailer)

**POST** `http://localhost:8080/api/orders`

**Headers**:
```
Authorization: Bearer YOUR_RETAILER_TOKEN
Content-Type: application/json
```

**Body**:
```json
{
  "items": [
    {
      "productId": "PRODUCT_UUID_HERE",
      "quantity": 10
    }
  ],
  "notes": "Test order for cancellation"
}
```

**Copy the `orderId` from response**.

---

#### Step 3: Test Retailer Cancellation

**PATCH** `http://localhost:8080/api/retailer/orders/{orderId}/cancel`

**Headers**:
```
Authorization: Bearer YOUR_RETAILER_TOKEN
Content-Type: application/json
```

**Body** (optional):
```json
{
  "reason": "Customer changed mind"
}
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "orderId": "...",
    "orderNumber": "ORD-20250115-0001",
    "status": "CANCELLED",
    "message": "Order cancelled successfully",
    "cancelledAt": "2025-01-15T10:30:00Z",
    "cancelledBy": "retailer-uuid"
  }
}
```

**Test Cases**:
- ✅ Cancel PENDING order → Should succeed
- ❌ Cancel CONFIRMED order → Should fail (400: Only PENDING orders can be cancelled)
- ❌ Cancel other retailer's order → Should fail (403: Unauthorized)
- ✅ Cancel already cancelled order → Should succeed (idempotent)

---

#### Step 4: Test Admin Cancellation

**PATCH** `http://localhost:8080/api/admin/orders/{orderId}/cancel`

**Headers**:
```
Authorization: Bearer YOUR_ADMIN_TOKEN
Content-Type: application/json
```

**Body** (required):
```json
{
  "reason": "Stock unavailable due to supplier issue"
}
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Order cancelled by admin",
  "data": {
    "orderId": "...",
    "orderNumber": "ORD-20250115-0002",
    "status": "CANCELLED_ADMIN",
    "message": "Order cancelled by admin",
    "cancelledAt": "2025-01-15T11:00:00Z",
    "cancelledBy": "admin-uuid"
  }
}
```

**Test Cases**:
- ✅ Cancel PENDING order → Should succeed
- ✅ Cancel CONFIRMED order → Should succeed (admin override)
- ✅ Cancel REJECTED order → Should succeed
- ❌ Cancel without reason → Should fail (400: Reason is required)
- ❌ Non-admin user → Should fail (403: Admin access required)

---

#### Step 5: Test Admin Dashboard

**GET** `http://localhost:8080/api/admin/orders?status=CANCELLED&page=1&page_size=20`

**Headers**:
```
Authorization: Bearer YOUR_ADMIN_TOKEN
```

**Query Parameters**:
- `status` - Filter by status (PENDING, CONFIRMED, REJECTED, CANCELLED, CANCELLED_ADMIN)
- `retailer_id` - Filter by retailer UUID
- `date_from` - ISO date (2025-01-01)
- `date_to` - ISO date (2025-01-31)
- `page` - Page number (default: 1)
- `page_size` - Items per page (default: 50)

**Expected Response**:
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [
      {
        "order": { /* Full order details */ },
        "items": [ /* Order items */ ],
        "itemCount": 3
      }
    ],
    "totalCount": 15,
    "totalPages": 1,
    "currentPage": 1,
    "pageSize": 20
  }
}
```

---

#### Step 6: Get Order with Full History (Admin)

**GET** `http://localhost:8080/api/admin/orders/{orderId}`

**Headers**:
```
Authorization: Bearer YOUR_ADMIN_TOKEN
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Order details retrieved successfully",
  "data": {
    "order": { /* Order details */ },
    "items": [ /* Order items */ ],
    "statusHistory": []
  }
}
```

---

### Method 2: Use the Built-in API Test UI

1. **Open in browser**: `http://localhost:8080/api-test-ui.html` (if exists in backend folder)
2. Or use the backend's `api-test-ui.html` file

---

### Method 3: Test from Android Studio (Retrofit)

#### Add to your Android API interface:

```kotlin
// In your OrderApiService.kt
@PATCH("retailer/orders/{orderId}/cancel")
suspend fun cancelOrder(
    @Path("orderId") orderId: String,
    @Body request: CancelOrderRequest
): ApiResponse<CancelOrderResponse>

// In AdminApiService.kt
@PATCH("admin/orders/{orderId}/cancel")
suspend fun adminCancelOrder(
    @Path("orderId") orderId: String,
    @Body request: AdminCancelRequest
): ApiResponse<CancelOrderResponse>

@GET("admin/orders")
suspend fun getAdminOrders(
    @Query("status") status: String? = null,
    @Query("retailer_id") retailerId: String? = null,
    @Query("page") page: Int = 1,
    @Query("page_size") pageSize: Int = 20
): ApiResponse<AdminDashboardResponse>
```

#### Data classes for Android:

```kotlin
data class CancelOrderRequest(
    val reason: String? = null
)

data class AdminCancelRequest(
    val reason: String
)

data class CancelOrderResponse(
    val orderId: String,
    val orderNumber: String,
    val status: String,
    val message: String,
    val cancelledAt: String,
    val cancelledBy: String
)
```

#### Test in Android:

```kotlin
// In your ViewModel or Repository
suspend fun cancelOrder(orderId: String, reason: String?) {
    try {
        val response = apiService.cancelOrder(
            orderId,
            CancelOrderRequest(reason)
        )
        if (response.success) {
            // Show success message
            Log.d("Order", "Cancelled: ${response.data.orderNumber}")
        }
    } catch (e: Exception) {
        // Handle error
        Log.e("Order", "Cancel failed: ${e.message}")
    }
}
```

---

## 🗄️ Database Verification

### Check if V9 migration applied:

```sql
-- Connect to PostgreSQL
psql -U postgres -d ganeshkulfi

-- Check migration status
SELECT * FROM flyway_schema_history 
WHERE script = 'V9__order_status_history.sql';

-- Check table exists
\dt order_status_history

-- Check trigger exists
SELECT tgname FROM pg_trigger 
WHERE tgname = 'trigger_record_order_status_change';

-- View status history
SELECT * FROM order_status_history 
ORDER BY created_at DESC 
LIMIT 10;

-- Check cancelled orders
SELECT order_number, status, cancelled_at, cancellation_reason
FROM orders
WHERE status IN ('CANCELLED', 'CANCELLED_ADMIN');
```

---

## 📋 Test Scenarios Checklist

### Retailer Cancellation Tests
- [ ] Retailer can cancel their own PENDING order
- [ ] Retailer cannot cancel CONFIRMED order
- [ ] Retailer cannot cancel REJECTED order
- [ ] Retailer cannot cancel another retailer's order
- [ ] Cancelling already cancelled order is idempotent
- [ ] Optional reason field works (null or provided)
- [ ] Status changes to CANCELLED
- [ ] cancelled_at timestamp is set
- [ ] cancelled_by contains retailer UUID

### Admin Cancellation Tests
- [ ] Admin can cancel PENDING order
- [ ] Admin can cancel CONFIRMED order
- [ ] Admin can cancel REJECTED order
- [ ] Admin can cancel any retailer's order
- [ ] Reason is required (fails without it)
- [ ] Status changes to CANCELLED_ADMIN
- [ ] cancelled_at timestamp is set
- [ ] cancelled_by contains admin UUID
- [ ] Reason is stored in cancellation_reason field

### Admin Dashboard Tests
- [ ] Get all orders without filters
- [ ] Filter by status works (PENDING, CONFIRMED, CANCELLED, etc.)
- [ ] Filter by retailer_id works
- [ ] Date range filtering works (date_from, date_to)
- [ ] Pagination works (page, page_size)
- [ ] Response includes order count
- [ ] Each order shows item count

### Status History Tests
- [ ] Status history table contains initial records for existing orders
- [ ] Trigger records status changes automatically
- [ ] History includes who made the change (changed_by)
- [ ] History includes user role (ADMIN, RETAILER)
- [ ] History includes reason for change
- [ ] History is ordered chronologically

### Authorization Tests
- [ ] Retailer endpoints require retailer JWT token
- [ ] Admin endpoints require admin JWT token
- [ ] Non-admin cannot access admin endpoints (403)
- [ ] Expired tokens are rejected (401)

---

## 🐛 Common Issues & Solutions

### Issue 1: "Order not found"
**Solution**: Use `GET /api/orders` to list orders and copy a valid order ID.

### Issue 2: "Only PENDING orders can be cancelled"
**Solution**: Retailer can only cancel PENDING orders. Use admin endpoint for other statuses.

### Issue 3: "Unauthorized: Order belongs to different retailer"
**Solution**: Make sure you're using the retailer's JWT token who created the order.

### Issue 4: "Admin access required"
**Solution**: Use admin JWT token, not retailer token.

### Issue 5: "Reason is required"
**Solution**: Admin cancellation requires a reason in the request body.

### Issue 6: V9 Migration Failed
**Solution**: 
```sql
-- Drop the partially created table
DROP TABLE IF EXISTS order_status_history CASCADE;

-- Drop trigger if exists
DROP TRIGGER IF EXISTS trigger_record_order_status_change ON orders;
DROP FUNCTION IF EXISTS record_order_status_change();

-- Remove cancelled columns from orders
ALTER TABLE orders 
DROP COLUMN IF EXISTS cancelled_at,
DROP COLUMN IF EXISTS cancelled_by,
DROP COLUMN IF EXISTS cancellation_reason;

-- Delete failed migration record
DELETE FROM flyway_schema_history 
WHERE script = 'V9__order_status_history.sql';
```

Then restart the backend to re-apply migration.

---

## 📱 Android Integration Example

### Sample Order Cancellation Screen:

```kotlin
@Composable
fun OrderDetailScreen(
    orderId: String,
    orderStatus: String,
    viewModel: OrderViewModel
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    Column {
        Text("Order Status: $orderStatus")
        
        // Show cancel button only for PENDING orders
        if (orderStatus == "PENDING") {
            Button(
                onClick = { showCancelDialog = true }
            ) {
                Text("Cancel Order")
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this order?")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrder(orderId, cancelReason.ifEmpty { null })
                        showCancelDialog = false
                    }
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order")
                }
            }
        )
    }
}
```

---

## 🎯 Success Criteria

✅ **Day 10 is working correctly when**:
1. Retailer can cancel PENDING orders from their Android app
2. Admin can cancel any order from admin panel
3. Status history is recorded automatically
4. Admin dashboard shows filtered orders
5. Order detail screen shows cancellation info (who, when, why)
6. Authorization is enforced (retailer vs admin)
7. Idempotent operations (retry-safe)

---

## 📞 Need Help?

1. Check backend logs for detailed error messages
2. Verify JWT token is valid and not expired
3. Confirm database migration applied successfully
4. Test with Postman first before Android integration
5. Check `DAY10_COMPLETE.md` for detailed API documentation

**Backend Health Check**: `http://localhost:8080/api/health`
