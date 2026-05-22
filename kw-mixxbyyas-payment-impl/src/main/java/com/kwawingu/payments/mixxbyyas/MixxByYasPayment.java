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
import org.checkerframework.checker.nullness.qual.Nullable;

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
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "customer", Map.of("firstname", "", "lastname", "", "email", ""),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(MixxByYasDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "description", payload.description(),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private @Nullable SnippeApiKey apiKey = null;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    @SuppressWarnings("nullness") // null-checked above before passing to constructor
    public MixxByYasPayment build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new MixxByYasPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
