/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(MixxByYasCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(MixxByYasDisbursePayload payload)
      throws IOException, InterruptedException;
}
