/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class AirtelPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private AirtelPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(AirtelCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "airtel_money",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(AirtelDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "airtel_money",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public AirtelPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new AirtelPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
