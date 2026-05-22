/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CardCollectPayload {
  private final long amount;
  private final String email;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private CardCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.email = builder.email;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() {
    return amount;
  }

  public String email() {
    return email;
  }

  public String reference() {
    return reference;
  }

  public String description() {
    return description;
  }

  public static class Builder {
    private long amount;
    private @Nullable String email = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

    public Builder setAmount(long amount) {
      this.amount = amount;
      return this;
    }

    public Builder setEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder setReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public CardCollectPayload build() {
      if (email == null) throw new NullPointerException("email cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new CardCollectPayload(this);
    }
  }
}
