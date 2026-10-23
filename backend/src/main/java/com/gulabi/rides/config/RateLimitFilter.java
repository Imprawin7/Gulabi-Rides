package com.gulabi.rides.config;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.*; import java.util.concurrent.ConcurrentHashMap;

/** Slows brute-force attempts: at most N POSTs per minute to /api/auth/** from one client address (in memory, per instance). */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
  @Value("${app.auth-rate-limit-per-minute:10}") private int limit = 10;

  @Override protected boolean shouldNotFilter(HttpServletRequest r) { return !("POST".equals(r.getMethod()) && r.getRequestURI().startsWith("/api/auth/")); }

  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    long now = System.currentTimeMillis();
    if (hits.size() > 10_000) hits.clear(); // crude bound on memory
    Deque<Long> d = hits.computeIfAbsent(clientIp(req), k -> new ArrayDeque<>());
    boolean blocked;
    synchronized (d) {
      while (!d.isEmpty() && now - d.peekFirst() > 60_000) d.pollFirst();
      blocked = d.size() >= limit;
      if (!blocked) d.addLast(now);
    }
    if (blocked) {
      res.setStatus(429); res.setHeader("Retry-After", "60"); res.setContentType("application/json");
      res.getWriter().write("{\"message\":\"Too many attempts. Please try again in a minute.\"}");
      return;
    }
    chain.doFilter(req, res);
  }

  /** Behind nginx the last X-Forwarded-For entry is the address nginx saw; earlier ones can be forged by the client. */
  private static String clientIp(HttpServletRequest r) {
    String xff = r.getHeader("X-Forwarded-For");
    if (xff == null || xff.isBlank()) return r.getRemoteAddr();
    String[] parts = xff.split(","); return parts[parts.length - 1].trim();
  }
}
