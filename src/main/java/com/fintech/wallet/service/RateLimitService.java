package com.fintech.wallet.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {

    private final ConcurrentMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final Bandwidth bandwidth;

    public RateLimitService() {
        this.bandwidth = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
    }

    public boolean tryConsume(String key) {
        Bucket bucket = bucketCache.computeIfAbsent(key, k -> newBucket());
        return bucket.tryConsume(1);
    }

    public boolean tryConsume(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return tryConsume(clientIp);
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // The header may contain multiple IPs, the first one is the client IP
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}