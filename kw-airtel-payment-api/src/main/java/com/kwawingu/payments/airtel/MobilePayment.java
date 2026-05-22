/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(AirtelCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(AirtelDisbursePayload payload) throws IOException, InterruptedException;
}
