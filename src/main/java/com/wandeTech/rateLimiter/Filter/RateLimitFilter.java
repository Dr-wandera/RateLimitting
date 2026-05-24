package com.wandeTech.rateLimiter.Filter;

import com.wandeTech.rateLimiter.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitingService rateLimitingService;
    private long numToken=1;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /// we get client by IP address
        String clientIp=getClientIp(request);
        Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);

        var probe=tokenBucket.tryConsumeAndReturnRemaining(numToken);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", "1");
            filterChain.doFilter(request, response);
        }
        else {
          var waitToRefill=  probe.getNanosToWaitForRefill()/1_000_000_000;
          response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
          response.addHeader("X-Rate-Limit-Retry-After-seconds", String.valueOf(waitToRefill));
          response.setContentType("application/json");

          String jsonResponse = """
                  
                  {
                        "status":"%s,
                        "error":"TOO_MANY_REQUESTS"
                        "message":"You have exhausted your API request Quota"
                        "retryAfterSeconds":"%s"
                  
                  }
                  """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(), waitToRefill);

          response.getWriter().write(jsonResponse);

        }
    }

    private  String getClientIp(HttpServletRequest request) {

        /// Check for x-forwarded for
        String ip = request.getHeader("x-forwarded-for");

        if(ip == null || ip.isEmpty()) {
            return request.getRemoteAddr();
        }

        return  ip.split(",")[0].trim();
    }

}
