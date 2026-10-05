package com.payflow.apigateway.filter;

import com.payflow.common.constant.Headers;
import com.payflow.common.security.JwtClaims;
import com.payflow.common.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(Headers.AUTHORIZATION);

        // No bearer token - let Spring security decide (public path vs 401)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = authHeader.substring(Headers.BEARER_PREFIX.length());

        try{
            JwtClaims claims = jwtProvider.parseToken(token);

            List<SimpleGrantedAuthority> authorities = claims.getRoles() == null ?
                    List.of()
                    : claims.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" +role))
                    .toList();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            claims.getUserId(),
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            HttpServletRequest wrapped = new HeaderInjectingRequestWrapper(request , claims);

            log.debug("JWT valid: userId={} roles={} path={}",
                    claims.getUserId(), claims.getRoles(), request.getRequestURI());

            filterChain.doFilter(wrapped , response);
        }catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT for Path ={}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired token\"}");
        }
    }

    /**
     * Injects trusted X-User-* headers for downstream services.
     * Strips any client-supplied X-User-* headers to prevent spoofing.
     */
    private static class HeaderInjectingRequestWrapper extends HttpServletRequestWrapper {

        private final JwtClaims claims;

        HeaderInjectingRequestWrapper(HttpServletRequest request, JwtClaims claims) {
            super(request);
            this.claims = claims;
        }

        @Override
        public String getHeader(String name) {
            if (name == null){
                return super.getHeader(null);
            }

            if (name.toLowerCase().startsWith("x-user-")){
                if (Headers.USER_ID.equalsIgnoreCase(name)){
                    return String.valueOf(claims.getUserId());
                }
                if (Headers.USER_EMAIL.equalsIgnoreCase(name)) {
                    return claims.getEmail();
                }
                if (Headers.USER_ROLES.equalsIgnoreCase(name)) {
                    return claims.getRoles() == null ? "" : String.join(",", claims.getRoles());
                }
                return null;
            }
            return super.getHeader(name);
        }
    }
}
