/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CardCollectPayload {
  private final long amount;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String phone;
  private final String reference;
  private final String description;
  private final String redirectUrl;
  private final String address;
  private final String city;
  private final String state;
  private final String postcode;
  private final String country;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private CardCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.email = builder.email;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
    this.redirectUrl = builder.redirectUrl;
    this.address = builder.address;
    this.city = builder.city;
    this.state = builder.state;
    this.postcode = builder.postcode;
    this.country = builder.country;
  }

  public long amount() {
    return amount;
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

  public String phone() {
    return phone;
  }

  public String reference() {
    return reference;
  }

  public String description() {
    return description;
  }

  public String redirectUrl() {
    return redirectUrl;
  }

  public String address() {
    return address;
  }

  public String city() {
    return city;
  }

  public String state() {
    return state;
  }

  public String postcode() {
    return postcode;
  }

  public String country() {
    return country;
  }

  public static class Builder {
    private long amount;
    private @Nullable String firstName = null;
    private @Nullable String lastName = null;
    private @Nullable String email = null;
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;
    private @Nullable String redirectUrl = null;
    private @Nullable String address = null;
    private @Nullable String city = null;
    private @Nullable String state = null;
    private @Nullable String postcode = null;
    private @Nullable String country = null;

    public Builder setAmount(long amount) {
      this.amount = amount;
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

    public Builder setRedirectUrl(String redirectUrl) {
      this.redirectUrl = redirectUrl;
      return this;
    }

    public Builder setAddress(String address) {
      this.address = address;
      return this;
    }

    public Builder setCity(String city) {
      this.city = city;
      return this;
    }

    public Builder setState(String state) {
      this.state = state;
      return this;
    }

    public Builder setPostcode(String postcode) {
      this.postcode = postcode;
      return this;
    }

    public Builder setCountry(String country) {
      this.country = country;
      return this;
    }

    public CardCollectPayload build() {
      if (firstName == null) throw new NullPointerException("firstName cannot be null");
      if (lastName == null) throw new NullPointerException("lastName cannot be null");
      if (email == null) throw new NullPointerException("email cannot be null");
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (redirectUrl == null) throw new NullPointerException("redirectUrl cannot be null");
      if (address == null) throw new NullPointerException("address cannot be null");
      if (city == null) throw new NullPointerException("city cannot be null");
      if (state == null) throw new NullPointerException("state cannot be null");
      if (postcode == null) throw new NullPointerException("postcode cannot be null");
      if (country == null) throw new NullPointerException("country cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new CardCollectPayload(this);
    }
  }
}
