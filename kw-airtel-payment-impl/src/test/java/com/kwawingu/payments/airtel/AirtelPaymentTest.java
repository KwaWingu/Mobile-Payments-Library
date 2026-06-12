/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

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

public class AirtelPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AirtelPaymentTest.class);

  private AirtelPayment payment;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(
        System.getenv("SNIPPE_API_KEY") != null,
        "SNIPPE_API_KEY not set; skipping Snippe integration tests");
    payment = new AirtelPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    AirtelCollectPayload payload =
        new AirtelCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255780000000")
            .setReference("TEST-AIRTEL-C2B-001")
            .setDescription("Test Airtel collection")
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
    AirtelDisbursePayload payload =
        new AirtelDisbursePayload.Builder()
            .setAmount(5000)
            .setPhone("+255780000000")
            .setReference("TEST-AIRTEL-B2B-001")
            .setDescription("Test Airtel disbursement")
            .setRecipientName("Test Recipient")
            .build();

    try {
      PayoutResponse response = payment.disburse(payload);

      assertNotNull(response.reference());
      assertFalse(response.reference().isBlank());
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
