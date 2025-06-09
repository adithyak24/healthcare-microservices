# Rate Limiting Test Guide

## Overview
The API Gateway now has in-memory rate limiting implemented with the following limits:

- **Auth Service** (`/auth/**`): 5 requests per minute
- **Patient Service** (`/api/patients/**`): 10 requests per minute  
- **Billing Service** (`/api/billing/**`): 8 requests per minute

## Testing Rate Limiting

### Method 1: Using Browser/Frontend
1. Try logging in multiple times quickly (more than 5 times in a minute)
2. You should see rate limiting kick in after the 5th attempt

### Method 2: Using Postman/curl
Test the patient service endpoint:

```bash
# Test 1: Make 5 requests quickly - should work
curl -X GET "http://localhost:4004/api/patients/test" -H "Content-Type: application/json"

# Test 2: Make the 11th request - should be rate limited
# (After making 10 successful requests)
curl -X GET "http://localhost:4004/api/patients/test" -H "Content-Type: application/json"
```

### Expected Behavior

#### Successful Request:
- Status: 200 (or whatever the backend returns)
- Headers include:
  - `X-Rate-Limit-Remaining: X` (where X decreases with each request)
  - `X-Rate-Limit-Current: Y` (where Y increases with each request)

#### Rate Limited Request:
- Status: **429 Too Many Requests**
- Headers include:
  - `X-Rate-Limit-Exceeded: true`
  - `X-Rate-Limit-Max: 10` (or whatever the limit is for that service)
  - `X-Rate-Limit-Window: 60 seconds`

## Rate Limiting Configuration

The limits are set in `application.yml`:

```yaml
filters:
  - name: InMemoryRateLimiting
    args:
      max-requests: 10  # Number of requests allowed
      window-size: 60   # Time window in seconds
```

## How It Works

1. **Per-IP Tracking**: Each IP address gets its own rate limit counter
2. **Sliding Window**: After 60 seconds, the counter resets
3. **In-Memory Storage**: Uses ConcurrentHashMap for thread-safe operations
4. **Automatic Cleanup**: Old entries are cleaned up automatically

## Testing Bloom Filter Protection

With rate limiting in place, you can now:

1. **Test legitimate login attempts**: Should work normally
2. **Test brute force attempts**: Should be blocked after 5 attempts
3. **Test Bloom filter**: Try logging in with previously failed passwords - the Bloom filter should block them immediately, and rate limiting should block excessive attempts

## Production Considerations

For production use, consider:

1. **Redis-based storage**: For distributed deployments
2. **Higher limits**: Current limits are set low for easy testing
3. **Different limits per user type**: Premium users might get higher limits
4. **Rate limiting bypass**: For admin or system accounts 