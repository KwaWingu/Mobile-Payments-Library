/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class MixxByYasCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private MixxByYasCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() {
    return amount;
  }

  public String phone() {
    return phone;
  }

  public String reference() {
    return reference;
  }

  public String description() {
    return description;
  }

  public static class Builder {
    private long amount;
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

    public Builder setAmount(long amount) {
      this.amount = amount;
      return this;
    }

    public Builder setPhone(String phone) {
      this.phone = phone;
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

    public MixxByYasCollectPayload build() {
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MixxByYasCollectPayload(this);
    }
  }
}
