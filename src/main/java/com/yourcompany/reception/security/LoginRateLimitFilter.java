package com.yourcompany.reception.security;

import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, per-instance IP rate limit. Use a shared limiter before scaling to multiple instances. */
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final Map<String, long[]> attempts = new LinkedHashMap<>();
    synchronized boolean allow(String ip, long now) {
        attempts.entrySet().removeIf(e -> now - e.getValue()[0] >= 60000);
        long[] bucket = attempts.get(ip);
        if (bucket == null) {
            if (attempts.size() >= 10000) return false;
            bucket = new long[]{now, 0}; attempts.put(ip, bucket);
        }
        return ++bucket[1] <= 10;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("POST".equals(request.getMethod()) && ("/login".equals(path) || "/register".equals(path)) && !allow(request.getRemoteAddr(), System.currentTimeMillis())) {
            response.setHeader("Retry-After", "60"); response.sendError(429); return;
        }
        chain.doFilter(request, response);
    }
}
