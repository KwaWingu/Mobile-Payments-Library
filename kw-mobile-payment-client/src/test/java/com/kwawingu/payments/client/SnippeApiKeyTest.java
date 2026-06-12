/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SnippeApiKeyTest {

  @Test
  public void ofRejectsNullOrBlank() {
    assertThrows(IllegalArgumentException.class, () -> SnippeApiKey.of(null));
    assertThrows(IllegalArgumentException.class, () -> SnippeApiKey.of(""));
    assertThrows(IllegalArgumentException.class, () -> SnippeApiKey.of("   "));
  }

  @Test
  public void insertAuthorizationHeaderUsesBearerScheme() {
    Map<String, String> headers = new HashMap<>();
    SnippeApiKey.of("secret-token-123").insertAuthorizationHeader(headers);
    assertEquals("Bearer secret-token-123", headers.get("Authorization"));
  }

  @Test
  public void toStringNeverLeaksSecret() {
    String rendered = SnippeApiKey.of("secret-token-123").toString();
    assertFalse(rendered.contains("secret-token-123"), "toString must not expose the key");
  }
}
