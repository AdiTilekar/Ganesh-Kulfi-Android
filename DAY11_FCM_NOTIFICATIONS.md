# Day 11: FCM Push Notifications & Order Timeline

## ✅ Implementation Complete

### Backend Features Implemented

#### 1. FCM Token System
- **Database Changes** (V10 Migration):
  - Added `fcm_token` column to `app_user` table
  - Added `fcm_token_updated_at` timestamp
  - Created indexes for fast lookups

- **API Endpoints**:
  - `POST /api/fcm/update-token` - Save FCM token on login
  - `DELETE /api/fcm/token` - Remove token on logout

- **Service Layer**:
  - `FCMNotificationService` - Handles all push notification logic
  - Sends notifications via Firebase Cloud Messaging
  - Supports custom data payloads

#### 2. Order Timeline System
- **Database Changes** (V10 Migration):
  - Created `order_timeline` table to track all status changes
  - Automatic trigger creates timeline entry when order status changes
  - Tracks who made the change and when
  - Stores notification sent status

- **Repository**: `OrderTimelineRepository`
  - Create timeline entries
  - Get timeline for specific order
  - Mark notifications as sent
  - Delete timeline entries

#### 3. Factory Order Status Updates
- **API Endpoints** (Admin/Factory Only):
  - `POST /api/orders/{orderId}/confirm` - Confirm order
  - `POST /api/orders/{orderId}/pack` - Mark as packed
  - `POST /api/orders/{orderId}/out-for-delivery` - Out for delivery
  - `POST /api/orders/{orderId}/deliver` - Mark as delivered
  - `GET /api/orders/{orderId}/timeline` - Get order timeline history

- **Auto-Notifications**:
  - Each status update automatically sends FCM push notification to retailer
  - Creates timeline entry with message
  - Background notification sending (non-blocking)

#### 4. Order Status Flow
```
PENDING → CONFIRMED → PACKED → OUT_FOR_DELIVERY → DELIVERED
         ↓
     CANCELLED (can be cancelled anytime before DELIVERED)
```

### Database Schema

#### `app_user` Table (Updated)
```sql
ALTER TABLE app_user
ADD COLUMN fcm_token VARCHAR(255),
ADD COLUMN fcm_token_updated_at TIMESTAMP;
```

#### `order_timeline` Table (New)
```sql
CREATE TABLE order_timeline (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(id),
    status VARCHAR(50) NOT NULL,
    message TEXT,
    created_by UUID REFERENCES app_user(id),
    created_by_role VARCHAR(20),
    notification_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### API Documentation

#### Update FCM Token
```http
POST /api/fcm/update-token
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "fcmToken": "device_fcm_token_here"
}

Response:
{
  "success": true,
  "message": "FCM token updated successfully",
  "data": null
}
```

#### Confirm Order (Admin Only)
```http
POST /api/orders/{orderId}/confirm
Authorization: Bearer {admin_jwt_token}

Response:
{
  "success": true,
  "message": "Order status updated to CONFIRMED successfully",
  "data": null
}

// Automatically sends push notification to retailer:
// Title: "Order Confirmed"
// Body: "Your order #12345678 has been confirmed by factory"
```

#### Get Order Timeline
```http
GET /api/orders/{orderId}/timeline
Authorization: Bearer {jwt_token}

Response:
{
  "success": true,
  "message": "Timeline retrieved successfully",
  "timeline": [
    {
      "id": "uuid",
      "orderId": "order_uuid",
      "status": "CONFIRMED",
      "message": "Order has been confirmed by factory",
      "createdBy": "admin_uuid",
      "createdByRole": "ADMIN",
      "notificationSent": true,
      "createdAt": "2025-11-15T10:30:00Z"
    }
  ]
}
```

### FCM Configuration

#### Environment Variable
Set your Firebase Cloud Messaging server key:
```bash
export FCM_SERVER_KEY="your_firebase_server_key_here"
```

Get it from: Firebase Console → Project Settings → Cloud Messaging → Server Key

### Android App Integration (Next Step)

To complete Day 11, implement in Android app:

1. **Add Firebase SDK**:
   ```kotlin
   // app/build.gradle.kts
   implementation("com.google.firebase:firebase-messaging:23.4.0")
   ```

2. **Request FCM Permission** (Android 13+):
   ```kotlin
   val permission = android.Manifest.permission.POST_NOTIFICATIONS
   requestPermissions(arrayOf(permission), REQUEST_CODE)
   ```

3. **Get FCM Token**:
   ```kotlin
   FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
       if (task.isSuccessful) {
           val token = task.result
           // Send to backend via /api/fcm/update-token
       }
   }
   ```

4. **Handle Notifications**:
   ```kotlin
   class MyFirebaseMessagingService : FirebaseMessagingService() {
       override fun onMessageReceived(remoteMessage: RemoteMessage) {
           // Handle notification
           val title = remoteMessage.notification?.title
           val body = remoteMessage.notification?.body
           val orderId = remoteMessage.data["orderId"]
           val status = remoteMessage.data["status"]
           
           // Show notification or update UI
       }
   }
   ```

5. **Update Order Details Screen**:
   - Fetch and display order timeline
   - Show status history with timestamps
   - Real-time updates when receiving push notifications

### Testing

#### 1. Update FCM Token
```bash
curl -X POST http://localhost:8080/api/fcm/update-token \
  -H "Authorization: Bearer {retailer_jwt}" \
  -H "Content-Type: application/json" \
  -d '{"fcmToken": "test_token_123"}'
```

#### 2. Confirm Order (Admin)
```bash
curl -X POST http://localhost:8080/api/orders/{order_id}/confirm \
  -H "Authorization: Bearer {admin_jwt}"
```

#### 3. Get Timeline
```bash
curl http://localhost:8080/api/orders/{order_id}/timeline \
  -H "Authorization: Bearer {jwt_token}"
```

### Notification Payloads

#### Order Confirmed
```json
{
  "notification": {
    "title": "Order Confirmed",
    "body": "Your order #12345678 has been confirmed by factory"
  },
  "data": {
    "type": "order_status_update",
    "orderId": "uuid",
    "orderNumber": "12345678",
    "status": "CONFIRMED"
  }
}
```

#### Order Packed
```json
{
  "notification": {
    "title": "Order Packed",
    "body": "Your order #12345678 has been packed and is ready for dispatch"
  },
  "data": {
    "type": "order_status_update",
    "orderId": "uuid",
    "orderNumber": "12345678",
    "status": "PACKED"
  }
}
```

#### Out for Delivery
```json
{
  "notification": {
    "title": "Out for Delivery",
    "body": "Your order #12345678 is out for delivery"
  },
  "data": {
    "type": "order_status_update",
    "orderId": "uuid",
    "orderNumber": "12345678",
    "status": "OUT_FOR_DELIVERY"
  }
}
```

#### Delivered
```json
{
  "notification": {
    "title": "Order Delivered",
    "body": "Your order #12345678 has been delivered successfully"
  },
  "data": {
    "type": "order_status_update",
    "orderId": "uuid",
    "orderNumber": "12345678",
    "status": "DELIVERED"
  }
}
```

### Architecture

```
┌─────────────────┐
│  Android App    │
│  (Retailer)     │
└────────┬────────┘
         │ 1. Login & Send FCM Token
         │ POST /api/fcm/update-token
         ▼
┌─────────────────┐
│   Backend API   │
│   (Ktor)        │
└────────┬────────┘
         │ 2. Store FCM Token in DB
         ▼
┌─────────────────┐
│  PostgreSQL DB  │
│  app_user table │
└─────────────────┘

┌─────────────────┐
│  Admin Portal   │
│  (Factory)      │
└────────┬────────┘
         │ 3. Update Order Status
         │ POST /api/orders/{id}/confirm
         ▼
┌─────────────────┐
│   Backend API   │
│ - Update Status │
│ - Create Timeline
│ - Send FCM      │
└────────┬────────┘
         │ 4. Push Notification
         ▼
┌─────────────────┐
│   Firebase FCM  │
└────────┬────────┘
         │ 5. Deliver Notification
         ▼
┌─────────────────┐
│  Retailer App   │
│  (Receives Push)│
└─────────────────┘
```

### Key Features

✅ **Real-time Notifications**: Retailers get instant updates on order status  
✅ **Order Timeline**: Complete history of status changes  
✅ **Auto-Trigger**: Timeline entries created automatically on status change  
✅ **Background Processing**: Notifications sent asynchronously  
✅ **Security**: Only admins can update order status  
✅ **Audit Trail**: Track who changed status and when  
✅ **Flexible**: Supports additional custom data in notifications  

### Version
**Backend Version**: 0.0.11-SNAPSHOT  
**Migration**: V10__fcm_notifications.sql applied

### Next Steps

1. Set up Firebase project and get Server Key
2. Add Firebase SDK to Android app
3. Implement FCM token registration on login
4. Handle push notifications in app
5. Display order timeline in Order Details screen
6. Test end-to-end notification flow

---

**Day 11 Complete! 🎉**
Backend fully supports push notifications and order timeline tracking.
