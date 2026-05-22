/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import java.util.Map;
import java.util.Objects;

public final class SnippeApiKey {
  private final String value;

  private SnippeApiKey(String value) {
    this.value = value;
  }

  public static SnippeApiKey fromEnvironment() {
    String key = System.getenv("SNIPPE_API_KEY");
    Objects.requireNonNull(key, "SNIPPE_API_KEY environment variable is not set");
    return new SnippeApiKey(key);
  }

  public void insertAuthorizationHeader(Map<String, String> headers) {
    headers.put("Authorization", "Bearer " + value);
  }
}
