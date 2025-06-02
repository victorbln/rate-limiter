package com.vbalan.rate_limiter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.storage.RateLimitStorage;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenBucketRateLimiterTest {

  @Mock private RateLimitStorage storage;

  private TokenBucketRateLimiter rateLimiter;
  private ClientConfiguration config;
  private static final String CLIENT_ID = "test-client";

  @BeforeEach
  void setUp() {
    rateLimiter = new TokenBucketRateLimiter(storage);
    config = new ClientConfiguration(10, 5);
  }

  @Test
  void allowRequest_FirstRequest_ShouldInitializeBucketAndAllow() {
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(null);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(5L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).get("lastRefill:" + CLIENT_ID);
    verify(storage).set("tokens:" + CLIENT_ID, "5", Duration.ofMinutes(2));
    verify(storage).set(eq("lastRefill:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage).get("tokens:" + CLIENT_ID);
    verify(storage).set("tokens:" + CLIENT_ID, "4", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_WithTokensAvailable_ShouldConsumeAndAllow() {
    long now = System.currentTimeMillis();
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(now);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(3L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_WithNoTokensAvailable_ShouldDeny() {
    long now = System.currentTimeMillis();
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(now);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(0L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertFalse(result);
    verify(storage).get("tokens:" + CLIENT_ID);
    verify(storage, never()).set(eq("tokens:" + CLIENT_ID), anyString(), any(Duration.class));
  }

  @Test
  void allowRequest_WithNullTokens_ShouldDeny() {
    long now = System.currentTimeMillis();
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(now);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(null);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertFalse(result);
    verify(storage).get("tokens:" + CLIENT_ID);
    verify(storage, never()).set(eq("tokens:" + CLIENT_ID), anyString(), any(Duration.class));
  }

  @Test
  void allowRequest_TokenRefillNeeded_ShouldRefillAndAllow() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 60000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(1L).thenReturn(5L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "5", Duration.ofMinutes(2));
    verify(storage).set(eq("lastRefill:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage).set("tokens:" + CLIENT_ID, "4", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_PartialRefill_ShouldAddPartialTokens() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 30000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(2L).thenReturn(5L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "5", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "4", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_SmallTimeGap_ShouldNotRefill() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 5000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(3L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("tokens:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage, never())
        .set(eq("lastRefill:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_ExactRefillTime_ShouldAddExactTokens() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 6000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(1L).thenReturn(2L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "1", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_ZeroCurrentTokensWithRefill_ShouldRefillAndAllow() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 12000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(null).thenReturn(2L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "1", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_HighRefillRate_ShouldCapAtBurstCapacity() {
    ClientConfiguration highRateConfig = new ClientConfiguration(120, 3);
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 60000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(1L).thenReturn(3L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, highRateConfig);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "3", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_ZeroBurstCapacity_ShouldDeny() {
    ClientConfiguration zeroCapacityConfig = new ClientConfiguration(10, 0);
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(null);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, zeroCapacityConfig);

    assertFalse(result);
    verify(storage).set("tokens:" + CLIENT_ID, "0", Duration.ofMinutes(2));
    verify(storage).get("tokens:" + CLIENT_ID);
    verify(storage, times(1))
        .set(eq("tokens:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
  }

  @Test
  void allowRequest_DifferentClients_ShouldTrackSeparately() {
    String client1 = "client-1";
    String client2 = "client-2";
    when(storage.get("lastRefill:" + client1)).thenReturn(null);
    when(storage.get("lastRefill:" + client2)).thenReturn(null);
    when(storage.get("tokens:" + client1)).thenReturn(5L);
    when(storage.get("tokens:" + client2)).thenReturn(5L);

    boolean result1 = rateLimiter.allowRequest(client1, config);
    boolean result2 = rateLimiter.allowRequest(client2, config);

    assertTrue(result1);
    assertTrue(result2);
    verify(storage).set("tokens:" + client1, "5", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + client2, "5", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + client1, "4", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + client2, "4", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_ConsecutiveRequests_ShouldConsumeTokensSequentially() {
    long now = System.currentTimeMillis();
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(now);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(3L).thenReturn(2L).thenReturn(1L);

    assertTrue(rateLimiter.allowRequest(CLIENT_ID, config));
    assertTrue(rateLimiter.allowRequest(CLIENT_ID, config));
    assertTrue(rateLimiter.allowRequest(CLIENT_ID, config));

    verify(storage).set("tokens:" + CLIENT_ID, "2", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "1", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "0", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_LongTimeGap_ShouldFullyRefillBucket() {
    long now = System.currentTimeMillis();
    long lastRefillTime = now - 300000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(lastRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(0L).thenReturn(5L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set("tokens:" + CLIENT_ID, "5", Duration.ofMinutes(2));
    verify(storage).set("tokens:" + CLIENT_ID, "4", Duration.ofMinutes(2));
  }

  @Test
  void allowRequest_NegativeTimeDifference_ShouldNotRefill() {
    long now = System.currentTimeMillis();
    long futureRefillTime = now + 10000;
    when(storage.get("lastRefill:" + CLIENT_ID)).thenReturn(futureRefillTime);
    when(storage.get("tokens:" + CLIENT_ID)).thenReturn(2L);

    boolean result = rateLimiter.allowRequest(CLIENT_ID, config);

    assertTrue(result);
    verify(storage).set(eq("tokens:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage, never())
        .set(eq("lastRefill:" + CLIENT_ID), anyString(), eq(Duration.ofMinutes(2)));
    verify(storage).set("tokens:" + CLIENT_ID, "1", Duration.ofMinutes(2));
  }
}
