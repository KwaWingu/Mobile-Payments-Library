/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Lifecycle status returned by Snippe for a payment or payout. {@link #UNKNOWN} is a forward-compat
 * fallback so a new server-side status never breaks parsing.
 */
public enum PaymentStatus {
  PENDING,
  PROCESSING,
  COMPLETED,
  FAILED,
  REVERSED,
  CANCELLED,
  VOIDED,
  EXPIRED,
  UNKNOWN;

  public static PaymentStatus from(@Nullable String raw) {
    if (raw == null) {
      return UNKNOWN;
    }
    try {
      return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }
}
