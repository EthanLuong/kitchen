package com.example.kitchen.configuration;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NullMarked
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket(){
        return Bucket.builder().addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(5))).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, x -> createNewBucket());

        if(bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else{
            log.warn("Rate limit exceeded for IP {}", ip);
            response.setStatus(429);
            response.getWriter().write("Too many requests");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();
        return !path.contains("/auth");
    }
    }
