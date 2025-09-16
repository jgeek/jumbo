package com.jumbo.adapter.in.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Profile("!prod")
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final int MAX_PAYLOAD = 10_000;
    private static final Set<String> SKIP_PREFIX_CT = Set.of("multipart/", "image/", "video/", "audio/");
    private static final Set<String> SKIP_EQUAL_CT = Set.of("application/octet-stream");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip common noise endpoints; adjust as needed
        return uri.startsWith("/actuator")
                || uri.contains("swagger")
                || uri.contains("api-docs")
                || uri.startsWith("/webjars")
                || uri.startsWith("/static")
                || uri.startsWith("/health");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();

        ContentCachingRequestWrapper req = request instanceof ContentCachingRequestWrapper
                ? (ContentCachingRequestWrapper) request
                : new ContentCachingRequestWrapper(request, MAX_PAYLOAD);

        ContentCachingResponseWrapper res = response instanceof ContentCachingResponseWrapper
                ? (ContentCachingResponseWrapper) response
                : new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(req, res);
        } finally {
            long durationMs = System.currentTimeMillis() - start;

            String method = request.getMethod();
            String uri = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            int status = res.getStatus();

            Map<String, String> reqHeaders = headersMap(request);
            Map<String, String> resHeaders = headersMap(res);

            String reqCt = request.getContentType();
            String resCt = res.getContentType();

            String reqBody = isTextLike(reqCt) ? bodyString(req.getContentAsByteArray(), request.getCharacterEncoding()) : "";
            String resBody = isTextLike(resCt) ? bodyString(res.getContentAsByteArray(), res.getCharacterEncoding()) : "";

            // Log in a structured JSON format
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("HTTP", method);
            logMap.put("uri", uri);
            logMap.put("status", status);
            logMap.put("durationMs", durationMs);
            logMap.put("reqHeaders", reqHeaders);
            logMap.put("reqBodySize", req.getContentAsByteArray().length);
            logMap.put("resHeaders", resHeaders);
            logMap.put("resBodySize", res.getContentAsByteArray().length);

            if (StringUtils.hasText(reqBody)) {
                logMap.put("reqBody", reqBody);
            }

            if (StringUtils.hasText(resBody)) {
                logMap.put("resBody", resBody);
            }

            try {
                log.info(objectMapper.writeValueAsString(logMap));
            } catch (Exception e) {
                log.error("Failed to serialize log data to JSON", e);
            }

            // Important: write cached response body back to the real response
            res.copyBodyToResponse();
        }
    }

    private Map<String, String> headersMap(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            map.put(name, maskHeader(name, request.getHeader(name)));
        }
        return map;
    }

    private Map<String, String> headersMap(HttpServletResponse response) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String name : response.getHeaderNames()) {
            map.put(name, maskHeader(name, response.getHeader(name)));
        }
        return map;
    }

    private String maskHeader(String name, String value) {
        if (value == null) return null;
        String n = name.toLowerCase(Locale.ROOT);
        if (n.equals(HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT))
                || n.equals("proxy-authorization")
                || n.equals("x-api-key")
                || n.equals("set-cookie")
                || n.equals(HttpHeaders.COOKIE.toLowerCase(Locale.ROOT))) {
            return "***";
        }
        return value;
    }

    private boolean isTextLike(String contentType) {
        if (!StringUtils.hasText(contentType)) return false;
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (SKIP_EQUAL_CT.contains(ct)) return false;
        for (String p : SKIP_PREFIX_CT) {
            if (ct.startsWith(p)) return false;
        }
        return ct.startsWith("text/")
                || ct.contains("json")
                || ct.contains("xml")
                || ct.contains("x-www-form-urlencoded")
                || ct.contains("javascript");
    }

    private String bodyString(byte[] content, String charsetName) {
        if (content == null || content.length == 0) return "";
        int len = Math.min(content.length, MAX_PAYLOAD);
        Charset cs = StringUtils.hasText(charsetName) ? Charset.forName(charsetName) : StandardCharsets.UTF_8;
        String s = new String(content, 0, len, cs);
        if (content.length > MAX_PAYLOAD) {
            return s + "...(truncated)";
        }
        return s;
    }
}