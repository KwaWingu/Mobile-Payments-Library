/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Raised when Snippe returns an error. Carries the structured {@code error_code} (e.g. {@code
 * PAY_004}) and HTTP status so callers can branch on a stable code instead of substring-matching
 * the message.
 */
public final class SnippeApiException extends IOException {
  private static final long serialVersionUID = 1L;

  private final @Nullable String errorCode;
  private final int httpStatus;

  public SnippeApiException(String message, @Nullable String errorCode, int httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  /** Snippe business error code (e.g. {@code PAY_004}), or null for transport-level HTTP errors. */
  public @Nullable String errorCode() {
    return errorCode;
  }

  /** HTTP status code, or 0 if the error was reported in a 2xx body. */
  public int httpStatus() {
    return httpStatus;
  }
}
