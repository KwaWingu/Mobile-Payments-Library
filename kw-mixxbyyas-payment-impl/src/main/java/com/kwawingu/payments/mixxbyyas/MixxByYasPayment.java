/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class MixxByYasPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private MixxByYasPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(MixxByYasCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "mixx_by_yas",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(MixxByYasDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "mixx_by_yas",
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

    public MixxByYasPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new MixxByYasPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
