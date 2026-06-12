/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Shared, provider-agnostic validation for payment payloads. Centralizes the rules that every
 * mobile-money provider must enforce so they cannot drift between the four provider modules.
 */
public final class PayloadValidation {
  private PayloadValidation() {}

  /** Snippe caps idempotency keys (we send the business reference as the key) at 30 chars. */
  public static final int MAX_REFERENCE_LENGTH = 30;

  // Tanzanian MSISDN in any common form: +255XXXXXXXXX, 255XXXXXXXXX, or 0XXXXXXXXX.
  private static final Pattern TZ_PHONE = Pattern.compile("^(?:\\+?255|0)(\\d{9})$");
  private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  /**
   * Validates a Tanzanian phone number and normalizes it to the {@code 255XXXXXXXXX} form Snippe
   * expects.
   */
  public static String normalizePhone(@Nullable String phone) {
    if (phone == null) throw new NullPointerException("phone cannot be null");
    Matcher m = TZ_PHONE.matcher(phone.trim());
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "phone must be a valid Tanzanian MSISDN (e.g. 255712345678), got: " + phone);
    }
    return "255" + m.group(1);
  }

  public static String requireEmail(@Nullable String email) {
    if (email == null) throw new NullPointerException("email cannot be null");
    String trimmed = email.trim();
    if (!EMAIL.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("email is not valid: " + email);
    }
    return trimmed;
  }

  public static String requireReference(@Nullable String reference) {
    if (reference == null) throw new NullPointerException("reference cannot be null");
    if (reference.isBlank()) throw new IllegalArgumentException("reference cannot be blank");
    if (reference.length() > MAX_REFERENCE_LENGTH) {
      throw new IllegalArgumentException(
          "reference must be ≤" + MAX_REFERENCE_LENGTH + " chars, got: " + reference.length());
    }
    return reference;
  }

  public static String requireNonBlank(@Nullable String value, String field) {
    if (value == null) throw new NullPointerException(field + " cannot be null");
    if (value.isBlank()) throw new IllegalArgumentException(field + " cannot be blank");
    return value;
  }

  public static long requirePositiveAmount(long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive, got: " + amount);
    }
    return amount;
  }
}
