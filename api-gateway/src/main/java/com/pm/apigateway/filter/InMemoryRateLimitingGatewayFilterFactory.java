package com.pm.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimitingGatewayFilterFactory extends AbstractGatewayFilterFactory<InMemoryRateLimitingGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryRateLimitingGatewayFilterFactory.class);
    
    // Simple in-memory storage for rate limiting
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitStore = new ConcurrentHashMap<>();

    public InMemoryRateLimitingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = getClientIp(exchange);
            String key = "rate_limit:" + clientIp;
            
            logger.debug("In-memory rate limiting check for IP: {}", clientIp);

            long currentTime = Instant.now().getEpochSecond();
            long windowStart = currentTime - config.getWindowSize();
            
            // Clean up old entries periodically
            cleanupOldEntries(windowStart);
            
            RateLimitEntry entry = rateLimitStore.computeIfAbsent(key, k -> new RateLimitEntry());
            
            // Reset counter if window has passed
            if (entry.getWindowStart() < windowStart) {
                entry.reset(currentTime);
            }
            
            int currentCount = entry.incrementAndGet();
            
            if (currentCount > config.getMaxRequests()) {
                logger.warn("Rate limit exceeded for IP: {}. Current count: {}, Max: {}", 
                           clientIp, currentCount, config.getMaxRequests());
                
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("X-Rate-Limit-Exceeded", "true");
                exchange.getResponse().getHeaders().add("X-Rate-Limit-Max", String.valueOf(config.getMaxRequests()));
                exchange.getResponse().getHeaders().add("X-Rate-Limit-Window", config.getWindowSize() + " seconds");
                return exchange.getResponse().setComplete();
            }
            
            logger.debug("Request allowed for IP: {}. Count: {}/{}", 
                       clientIp, currentCount, config.getMaxRequests());
            
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", 
                String.valueOf(config.getMaxRequests() - currentCount));
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Current", String.valueOf(currentCount));
            
            return chain.filter(exchange);
        };
    }

    private void cleanupOldEntries(long windowStart) {
        // Remove entries older than current window (simple cleanup)
        rateLimitStore.entrySet().removeIf(entry -> entry.getValue().getWindowStart() < windowStart);
    }

    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        // Try to get real IP from headers first (in case of proxies)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to remote address
        return exchange.getRequest().getRemoteAddress() != null 
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }

    private static class RateLimitEntry {
        private volatile long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        public void reset(long newWindowStart) {
            this.windowStart = newWindowStart;
            this.count.set(0);
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public long getWindowStart() {
            return windowStart;
        }
    }

    public static class Config {
        private int maxRequests = 100; // Default: 100 requests
        private int windowSize = 60;   // Default: per 60 seconds (1 minute)

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }
    }
} 