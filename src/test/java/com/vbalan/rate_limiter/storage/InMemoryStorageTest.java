package com.vbalan.rate_limiter.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

class InMemoryStorageTest {

  private InMemoryStorage storage;

  @BeforeEach
  void setUp() {
    storage = new InMemoryStorage();
  }

  @Test
  void set_AndGet_ShouldStoreAndRetrieveValue() {
    String key = "test-key";
    String value = "test-value";

    storage.set(key, value, Duration.ofMinutes(1));
    String retrievedValue = storage.getString(key);

    assertEquals(value, retrievedValue);
  }

  @Test
  void set_AndGetAsLong_ShouldStoreAndRetrieveNumericValue() {
    String key = "numeric-key";
    String value = "12345";

    storage.set(key, value, Duration.ofMinutes(1));
    Long retrievedValue = storage.get(key);

    assertEquals(12345L, retrievedValue);
  }

  @Test
  void get_NonExistentKey_ShouldReturnNull() {
    String result = storage.getString("non-existent-key");
    Long numericResult = storage.get("non-existent-key");

    assertNull(result);
    assertNull(numericResult);
  }

  @Test
  void get_InvalidNumericValue_ShouldThrowNumberFormatException() {
    String key = "invalid-numeric";
    storage.set(key, "not-a-number", Duration.ofMinutes(1));

    assertThrows(NumberFormatException.class, () -> storage.get(key));
  }

  @Test
  void set_WithoutDuration_ShouldNotExpire() {
    String key = "permanent-key";
    String value = "permanent-value";

    storage.set(key, value, null);
    String retrievedValue = storage.getString(key);

    assertEquals(value, retrievedValue);
  }

  @Test
  void expire_ExistingKey_ShouldSetExpiration() {
    String key = "expire-test";
    String value = "test-value";
    storage.set(key, value, null);

    storage.expire(key, Duration.ofMillis(100));

    await().atMost(200, TimeUnit.MILLISECONDS)
        .until(() -> storage.getString(key) == null);

    assertNull(storage.getString(key));
  }

  @Test
  void set_WithShortDuration_ShouldExpire() {
    String key = "short-duration";
    String value = "expires-soon";

    storage.set(key, value, Duration.ofMillis(100));

    assertEquals(value, storage.getString(key));

    await().atMost(200, TimeUnit.MILLISECONDS)
        .until(() -> storage.getString(key) == null);

    assertNull(storage.getString(key));
  }

  @Test
  void delete_ExistingKey_ShouldRemoveValue() {
    String key = "delete-test";
    String value = "to-be-deleted";
    storage.set(key, value, Duration.ofMinutes(1));

    storage.delete(key);

    assertNull(storage.getString(key));
  }

  @Test
  void delete_NonExistentKey_ShouldNotThrowException() {
    assertDoesNotThrow(() -> storage.delete("non-existent"));
  }

  @Test
  void delete_ExpiredKey_ShouldNotThrowException() {
    String key = "expired-key";
    storage.set(key, "value", Duration.ofMillis(1));

    await().atMost(200, TimeUnit.MILLISECONDS)
        .until(() -> storage.getString(key) == null);

    assertDoesNotThrow(() -> storage.delete(key));
  }

  @Test
  void multipleKeys_ShouldTrackSeparately() {
    String key1 = "key1";
    String key2 = "key2";
    String value1 = "value1";
    String value2 = "value2";

    storage.set(key1, value1, Duration.ofMinutes(1));
    storage.set(key2, value2, Duration.ofMinutes(1));

    assertEquals(value1, storage.getString(key1));
    assertEquals(value2, storage.getString(key2));
  }

  @Test
  void multipleKeys_OneExpires_OtherShouldRemain() {
    String key1 = "short-lived";
    String key2 = "long-lived";
    String value1 = "expires-fast";
    String value2 = "stays-longer";

    storage.set(key1, value1, Duration.ofMillis(50));
    storage.set(key2, value2, Duration.ofMinutes(1));

    await().atMost(200, TimeUnit.MILLISECONDS)
        .until(() -> storage.getString(key1) == null);

    assertNull(storage.getString(key1));
    assertEquals(value2, storage.getString(key2));
  }

  @Test
  void overwriteKey_ShouldUpdateValue() {
    String key = "overwrite-test";
    String originalValue = "original";
    String newValue = "updated";

    storage.set(key, originalValue, Duration.ofMinutes(1));
    storage.set(key, newValue, Duration.ofMinutes(1));

    assertEquals(newValue, storage.getString(key));
  }

  @Test
  void overwriteKey_WithDifferentExpiration_ShouldUpdateExpiration() {
    String key = "expiration-update";
    String value = "test-value";

    storage.set(key, value, Duration.ofMillis(50));
    storage.set(key, value, Duration.ofMinutes(1));

    await().pollDelay(100, TimeUnit.MILLISECONDS).until(() -> true);

    assertEquals(value, storage.getString(key));
  }

  @Test
  void expiredKey_AutomaticCleanup_ShouldRemoveFromStorage() {
    String key = "cleanup-test";
    String value = "will-be-cleaned";
    storage.set(key, value, Duration.ofMillis(50));

    await().atMost(200, TimeUnit.MILLISECONDS)
        .until(() -> storage.getString(key) == null);

    assertNull(storage.getString(key));
  }

  @Test
  void concurrentAccess_ShouldBeThreadSafe() {
    String key = "concurrent-test";
    int threadCount = 10;
    Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      threads[i] = new Thread(() -> {
        storage.set(key + threadId, "value" + threadId, Duration.ofMinutes(1));
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      assertDoesNotThrow(() -> thread.join());
    }

    for (int i = 0; i < threadCount; i++) {
      assertEquals("value" + i, storage.getString(key + i));
    }
  }
}