package com.example.kitchen.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimiterFilterTest {

    private RateLimiterFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimiterFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getServletPath()).thenReturn("/v1/auth/login");
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void request_underLimit_allowsThrough() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void request_overLimit_returns429() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void request_overLimit_doesNotCallFilterChain() throws Exception {
        for (int i = 0; i < 6; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_nonAuthPath_returnsTrue() {
        when(request.getServletPath()).thenReturn("/v1/items");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_loginPath_returnsFalse() {
        when(request.getServletPath()).thenReturn("/v1/auth/login");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void shouldNotFilter_signupPath_returnsFalse() {
        when(request.getServletPath()).thenReturn("/v1/auth/signup");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void multipleIps_trackedSeparately() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        HttpServletRequest request2 = mock(HttpServletRequest.class);
        when(request2.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request2.getServletPath()).thenReturn("/v1/auth/login");

        filter.doFilterInternal(request2, response, filterChain);

        verify(filterChain, times(6)).doFilter(any(), any());
    }
}