/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MpesaPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private MpesaPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(MpesaCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "network", "mpesa",
            "amount", payload.amount(),
            "currency", "TZS",
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(MpesaDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "network", "mpesa",
            "amount", payload.amount(),
            "currency", "TZS",
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private @Nullable SnippeApiKey apiKey = null;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    @SuppressWarnings("nullness") // null-checked above before passing to constructor
    public MpesaPayment build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new MpesaPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
