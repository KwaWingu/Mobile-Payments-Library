/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import java.util.Objects;

public final class CardCollectPayload {
  private final long amount;
  private final String email;
  private final String reference;
  private final String description;

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
    private String email;
    private String reference;
    private String description;

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
      Objects.requireNonNull(email, "email cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new CardCollectPayload(this);
    }
  }
}
