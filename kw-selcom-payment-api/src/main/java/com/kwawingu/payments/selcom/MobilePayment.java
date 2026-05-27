/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(SelcomCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(SelcomDisbursePayload payload) throws IOException, InterruptedException;
}
