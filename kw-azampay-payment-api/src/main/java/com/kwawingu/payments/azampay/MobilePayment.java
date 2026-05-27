/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(AzampayCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(AzampayDisbursePayload payload) throws IOException, InterruptedException;
}
