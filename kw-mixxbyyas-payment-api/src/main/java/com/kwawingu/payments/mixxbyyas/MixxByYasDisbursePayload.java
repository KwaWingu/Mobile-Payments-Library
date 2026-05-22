/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import java.util.Objects;

public final class MixxByYasDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private MixxByYasDisbursePayload(Builder builder) {
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
    private String phone;
    private String reference;
    private String description;

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

    public MixxByYasDisbursePayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MixxByYasDisbursePayload(this);
    }
  }
}
