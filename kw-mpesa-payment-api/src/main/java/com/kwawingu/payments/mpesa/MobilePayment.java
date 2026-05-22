/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(MpesaCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(MpesaDisbursePayload payload) throws IOException, InterruptedException;
}
