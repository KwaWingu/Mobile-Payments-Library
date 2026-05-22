/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(HalopesaCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(HalopesaDisbursePayload payload) throws IOException, InterruptedException;
}
