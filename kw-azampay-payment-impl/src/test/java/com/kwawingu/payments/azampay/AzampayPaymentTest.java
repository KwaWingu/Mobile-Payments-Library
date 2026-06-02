/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzampayPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AzampayPaymentTest.class);
  private static final Set<String> VALID_COLLECT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "cancelled");
  private static final Set<String> VALID_PAYOUT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "reversed", "cancelled");

  private AzampayPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new AzampayPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    AzampayCollectPayload payload =
        new AzampayCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("azampay-collect-test")
            .setDescription("Azampay collect test")
            .setFirstName("Test")
            .setLastName("User")
            .setEmail("test@example.com")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_COLLECT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_COLLECT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    AzampayDisbursePayload payload =
        new AzampayDisbursePayload.Builder()
            .setAmount(5000L)
            .setPhone("255741000000")
            .setReference("azampay-disburse-test")
            .setDescription("Azampay disburse test")
            .setRecipientName("Test Recipient")
            .build();

    try {
      PayoutResponse response = payment.disburse(payload);

      assertNotNull(response.reference(), "reference must not be null");
      assertFalse(response.reference().isBlank(), "reference must not be blank");
      assertTrue(
          VALID_PAYOUT_STATUSES.contains(response.status()),
          "status must be one of " + VALID_PAYOUT_STATUSES + ", got: " + response.status());
      assertEquals(5000L, response.amount());
      assertEquals("TZS", response.currency());
      LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
    } catch (IOException e) {
      if (e.getMessage() != null && e.getMessage().contains("PAY_004")) {
        Assumptions.abort("Snippe sandbox payout balance unavailable: " + e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
