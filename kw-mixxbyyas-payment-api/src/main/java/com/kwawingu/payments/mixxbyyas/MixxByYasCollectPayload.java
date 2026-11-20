/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import com.kwawingu.payments.client.PayloadValidation;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MixxByYasCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String webhookUrl;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private MixxByYasCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.email = builder.email;
    this.webhookUrl = builder.webhookUrl;
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

  public String firstName() {
    return firstName;
  }

  public String lastName() {
    return lastName;
  }

  public String email() {
    return email;
  }

  /** Where Snippe sends the payment's status changes. */
  public String webhookUrl() {
    return webhookUrl;
  }

  public static class Builder {
    private long amount;
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;
    private @Nullable String firstName = null;
    private @Nullable String lastName = null;
    private @Nullable String email = null;
    private @Nullable String webhookUrl = null;

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

    public Builder setFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder setLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder setEmail(String email) {
      this.email = email;
      return this;
    }

    /**
     * Required. The https address on your server that Snippe calls when the payment completes or
     * fails.
     */
    public Builder setWebhookUrl(String webhookUrl) {
      this.webhookUrl = webhookUrl;
      return this;
    }

    public MixxByYasCollectPayload build() {
      this.phone = PayloadValidation.normalizePhone(phone);
      this.reference = PayloadValidation.requireReference(reference);
      this.email = PayloadValidation.requireEmail(email);
      this.webhookUrl = PayloadValidation.requireWebhookUrl(webhookUrl);
      PayloadValidation.requireNonBlank(firstName, "firstName");
      PayloadValidation.requireNonBlank(lastName, "lastName");
      PayloadValidation.requireNonBlank(description, "description");
      PayloadValidation.requirePositiveAmount(amount);
      return new MixxByYasCollectPayload(this);
    }
  }
}
