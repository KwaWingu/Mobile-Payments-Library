/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

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

public class MixxByYasPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(MixxByYasPaymentTest.class);

  private MixxByYasPayment payment;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(
        System.getenv("SNIPPE_API_KEY") != null,
        "SNIPPE_API_KEY not set; skipping Snippe integration tests");
    payment = new MixxByYasPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    MixxByYasCollectPayload payload =
        new MixxByYasCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255676000000")
            .setReference("mixxbyyas-collect-test")
            .setDescription("MixxByYas collect test")
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
    MixxByYasDisbursePayload payload =
        new MixxByYasDisbursePayload.Builder()
            .setAmount(5000L)
            .setPhone("255676000000")
            .setReference("mixxbyyas-disburse-test")
            .setDescription("MixxByYas disbursement test")
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
