/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import java.io.IOException;

public interface CardPayment {
  String checkoutUrl(CardCollectPayload payload) throws IOException, InterruptedException;
}
