/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CardPaymentImpl implements CardPayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private CardPaymentImpl(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public String checkoutUrl(CardCollectPayload payload) throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "card",
            "amount", payload.amount(),
            "currency", "TZS",
            "customer",
            Map.of(
                "first_name", payload.firstName(),
                "last_name", payload.lastName(),
                "email", payload.email(),
                "phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description(),
            "redirect_url", payload.redirectUrl());
    return client.postCardCheckout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private @Nullable SnippeApiKey apiKey = null;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    @SuppressWarnings("nullness") // null-checked above before passing to constructor
    public CardPaymentImpl build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new CardPaymentImpl(new SnippeHttpClient(), apiKey);
    }
  }
}
