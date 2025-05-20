# STP Trends Backend Implementation

This document provides instructions for implementing and testing the STP (Systematic Transfer Plan) Trends feature in the backend.

## Implementation Steps

1. **Add new columns to the Transaction table**
   - `frequency` (VARCHAR) - Frequency of STP (e.g., Monthly, Weekly)
   - `nextExecutionDate` (DATE) - Next execution date of STP
   - `expiryDate` (DATE) - Expiry date of STP
   - `remainingAmount` (DECIMAL) - Remaining amount to be transferred
   - `sourceBalance` (DECIMAL) - Current balance in source fund
   - `fromScheme` (VARCHAR) - Source fund/scheme
   - `toScheme` (VARCHAR) - Target fund/scheme

2. **Create new DTOs**
   - `StpTrendDTO.java` - For monthly trend data
   - `StpSummaryDTO.java` - For the complete summary

3. **Create new service**
   - `StpService.java` (interface)
   - `StpServiceImpl.java` (implementation)

4. **Create new controller**
   - `StpController.java` - Exposes `/api/stp/summary` endpoint

5. **Add repository methods**
   - Update `TransactionRepository.java` with STP-specific query methods

## Testing

### 1. Create Sample STP Data

1. Run the `sample-stp-data.sql` script against your database:
   ```bash
   psql -U youruser -d yourdb -f sample-stp-data.sql
   ```
   Or use your database management tool to execute the script.

   **Important**: Update the `client_id` values in the script to match existing clients in your database!

### 2. Verify Backend Implementation

1. **Start the backend server**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test the API endpoint**:
   - Using curl:
     ```bash
     curl -H "Authorization: Bearer your-token" http://localhost:8080/api/stp/summary
     ```
   - Or use a tool like Postman to make a GET request to `http://localhost:8080/api/stp/summary`

3. **Check the logs**:
   - Verify that STP transactions are being found and processed
   - Look for log messages like "Found X STP transactions for user ID: Y"

### 3. Frontend Integration

1. Update the frontend API service to use the real endpoint:
   ```javascript
   getStpSummary: () => api.get('/api/stp/summary')
   ```

2. Remove any frontend data processing for STP metrics.

3. Test the STP Trends page in the frontend to ensure it displays data from the backend.

### Debugging

If you don't see any STP data in the frontend:

1. Check the backend logs to ensure STP transactions are being found.
2. Verify that the frontend is receiving data from the backend (check network requests).
3. Make sure your authentication token is valid.
4. Check that the client_ids in the sample data match clients that belong to the authenticated user.

## SQL Queries for Manual Verification

To verify that STP transactions exist in your database:

```sql
-- Check if STP transactions exist
SELECT COUNT(*) FROM transactions WHERE type = 'STP';

-- Check STP transactions for a specific user
SELECT t.* FROM transactions t
JOIN clients c ON t.client_id = c.id
WHERE c.user_id = ? AND t.type = 'STP';
```

Replace `?` with your user ID.

## Common Issues and Solutions

1. **No STP transactions found**:
   - Make sure the sample data was inserted correctly
   - Check that the client IDs belong to the authenticated user

2. **All counts show zero**:
   - Verify that the STP transactions have the correct status values
   - Check that date fields are populated correctly

3. **Auth issues**:
   - Ensure your JWT token is valid and not expired
   - Check that the security configuration allows access to the endpoint 