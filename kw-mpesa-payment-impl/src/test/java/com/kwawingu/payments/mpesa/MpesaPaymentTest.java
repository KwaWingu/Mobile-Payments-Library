/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiException;
import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PaymentStatus;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpesaPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(MpesaPaymentTest.class);

  private MpesaPayment payment;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(
        System.getenv("SNIPPE_API_KEY") != null,
        "SNIPPE_API_KEY not set; skipping Snippe integration tests");
    payment = new MpesaPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    MpesaCollectPayload payload =
        new MpesaCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255741000000")
            .setReference("TEST-MPESA-C2B-001")
            .setDescription("Test M-Pesa collection")
            .setFirstName("Test")
            .setLastName("User")
            .setEmail("test@example.com")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertNotEquals(
        PaymentStatus.UNKNOWN,
        response.status(),
        "status must be a known value, got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    MpesaDisbursePayload payload =
        new MpesaDisbursePayload.Builder()
            .setAmount(5000)
            .setPhone("+255741000000")
            .setReference("TEST-MPESA-B2B-001")
            .setDescription("Test M-Pesa disbursement")
            .setRecipientName("Test Recipient")
            .build();

    try {
      PayoutResponse response = payment.disburse(payload);

      assertNotNull(response.reference(), "reference must not be null");
      assertFalse(response.reference().isBlank(), "reference must not be blank");
      assertNotEquals(
          PaymentStatus.UNKNOWN,
          response.status(),
          "status must be a known value, got: " + response.status());
      assertEquals(5000L, response.amount());
      assertEquals("TZS", response.currency());
      LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
    } catch (SnippeApiException e) {
      if ("PAY_004".equals(e.errorCode())) {
        Assumptions.abort("Snippe sandbox payout balance unavailable: " + e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
