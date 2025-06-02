package com.vbalan.rate_limiter.service;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.storage.RateLimitStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlidingWindowRateLimiterTest {

  @Mock
  private RateLimitStorage storage;

  private SlidingWindowRateLimiter rateLimiter;
  private ClientConfiguration config;
  private static final String CLIENT_ID = "test-client";

  @BeforeEach
  void setUp() {
    rateLimiter = new SlidingWindowRateLimiter(storage);
    config = new ClientConfiguration(3, 10);
  }

  @Test
  void allowRequest_FirstRequest_ShouldAllow() {
    when(storage.getString(anyString())).thenReturn(null);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).getString("sliding_requests:" + CLIENT_ID);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_WithinLimit_ShouldAllow() {
    long now = System.currentTimeMillis();
    String existingTimestamps = String.valueOf(now - 30000); // 30 seconds ago
    when(storage.getString(anyString())).thenReturn(existingTimestamps);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_AtLimit_ShouldDeny() {
    long now = System.currentTimeMillis();
    String existingTimestamps = (now - 30000) + "," + (now - 20000) + "," + (now - 10000);
    when(storage.getString(anyString())).thenReturn(existingTimestamps);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertFalse(result);
    verify(storage).set("sliding_requests:" + CLIENT_ID,existingTimestamps, Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_OldTimestampsOutsideWindow_ShouldAllowAndCleanup() {
    long now = System.currentTimeMillis();
    long oldTimestamp = now - 70000;
    long recentTimestamp = now - 30000;
    String existingTimestamps = oldTimestamp + "," + recentTimestamp;
    when(storage.getString(anyString())).thenReturn(existingTimestamps);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID), argThat(arg ->
        !arg.contains(String.valueOf(oldTimestamp)) &&
        arg.contains(String.valueOf(recentTimestamp))), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_EmptyTimestamps_ShouldAllow() {
    when(storage.getString(anyString())).thenReturn("");

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_InvalidTimestampFormat_ShouldIgnoreAndAllow() {
    String invalidTimestamps = "invalid,123abc,456";
    when(storage.getString(anyString())).thenReturn(invalidTimestamps);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_AllTimestampsExpired_ShouldDeleteKey() {
    long now = System.currentTimeMillis();
    long expiredTimestamp = now - 70000;
    String expiredTimestamps = String.valueOf(expiredTimestamp);
    when(storage.getString(anyString())).thenReturn(expiredTimestamps);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("sliding_requests:" + CLIENT_ID),
        argThat(arg -> !arg.contains(String.valueOf(expiredTimestamp))),
        eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_HighRequestLimit_ShouldAllow() {
    ClientConfiguration highLimitConfig = new ClientConfiguration(100, 10);
    when(storage.getString(anyString())).thenReturn(null);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, highLimitConfig);

    assertTrue(result);
  }

  @Test
  void allowRequest_ZeroRequestLimit_ShouldDeny() {
    ClientConfiguration zeroLimitConfig = new ClientConfiguration(0, 10);
    when(storage.getString(anyString())).thenReturn(null);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, zeroLimitConfig);

    assertFalse(result);
    verify(storage).delete("sliding_requests:" + CLIENT_ID);
  }

  @Test
  void allowRequest_DifferentClients_ShouldTrackSeparately() {
    String clientId1 = "client-1";
    String clientId2 = "client-2";
    when(storage.getString("sliding_requests:" + clientId1)).thenReturn(null);
    when(storage.getString("sliding_requests:" + clientId2)).thenReturn(null);

    boolean result1 = rateLimiter.allowRequest(clientId1, config);
    boolean result2 = rateLimiter.allowRequest(clientId2, config);

    assertTrue(result1);
    assertTrue(result2);
    verify(storage).getString("sliding_requests:" + clientId1);
    verify(storage).getString("sliding_requests:" + clientId2);
    verify(storage).set(eq("sliding_requests:" + clientId1), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage).set(eq("sliding_requests:" + clientId2), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_MultipleRequestsInQuickSuccession_ShouldRespectLimit() {
    long baseTime = System.currentTimeMillis();
    String existingRequests = (baseTime - 50000) + "," + (baseTime - 40000) + "," + (baseTime - 30000);
    when(storage.getString("sliding_requests:" + CLIENT_ID)).thenReturn(existingRequests);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertFalse(result);
    verify(storage).getString("sliding_requests:" + CLIENT_ID);
    verify(storage).set("sliding_requests:" + CLIENT_ID, existingRequests, Duration.ofMinutes(2));
  }
}