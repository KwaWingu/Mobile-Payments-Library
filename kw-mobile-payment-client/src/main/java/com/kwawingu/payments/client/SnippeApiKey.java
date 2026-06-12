/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import java.util.Map;

public final class SnippeApiKey {
  private final String value;

  private SnippeApiKey(String value) {
    this.value = value;
  }

  public static SnippeApiKey fromEnvironment() {
    String key = System.getenv("SNIPPE_API_KEY");
    if (key == null) {
      throw new IllegalStateException("SNIPPE_API_KEY environment variable is not set");
    }
    return of(key);
  }

  public static SnippeApiKey of(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("API key cannot be null or blank");
    }
    return new SnippeApiKey(value);
  }

  public void insertAuthorizationHeader(Map<String, String> headers) {
    headers.put("Authorization", "Bearer " + value);
  }

  /** Never expose the secret in logs or stack traces. */
  @Override
  public String toString() {
    return "SnippeApiKey{value=***redacted***}";
  }
}
