/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import com.kwawingu.payments.client.PayloadValidation;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class HalopesaDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;
  private final String recipientName;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private HalopesaDisbursePayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
    this.recipientName = builder.recipientName;
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

  public String recipientName() {
    return recipientName;
  }

  public static class Builder {
    private long amount;
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;
    private @Nullable String recipientName = null;

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

    public Builder setRecipientName(String recipientName) {
      this.recipientName = recipientName;
      return this;
    }

    public HalopesaDisbursePayload build() {
      this.phone = PayloadValidation.normalizePhone(phone);
      this.reference = PayloadValidation.requireReference(reference);
      PayloadValidation.requireNonBlank(description, "description");
      PayloadValidation.requireNonBlank(recipientName, "recipientName");
      PayloadValidation.requirePositiveAmount(amount);
      return new HalopesaDisbursePayload(this);
    }
  }
}
