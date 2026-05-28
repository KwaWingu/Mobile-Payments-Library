/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AzampayPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private AzampayPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(AzampayCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "customer",
                Map.of(
                    "firstname", payload.firstName(),
                    "lastname", payload.lastName(),
                    "email", payload.email()),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(AzampayDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "amount", payload.amount(),
            "channel", "mobile",
            "recipient_phone", payload.phone().replaceFirst("^\\+", ""),
            "recipient_name", payload.recipientName(),
            "narration", payload.description(),
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
    public AzampayPayment build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new AzampayPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
