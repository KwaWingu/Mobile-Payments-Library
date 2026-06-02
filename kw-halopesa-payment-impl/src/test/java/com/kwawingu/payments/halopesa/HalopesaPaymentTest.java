/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HalopesaPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(HalopesaPaymentTest.class);
  private static final Set<String> VALID_COLLECT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "cancelled");
  private static final Set<String> VALID_PAYOUT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "reversed", "cancelled");

  private HalopesaPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new HalopesaPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    HalopesaCollectPayload payload =
        new HalopesaCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255762000000")
            .setReference("halopesa-collect-test")
            .setDescription("Halopesa collect test")
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
    HalopesaDisbursePayload payload =
        new HalopesaDisbursePayload.Builder()
            .setAmount(5000L)
            .setPhone("255762000000")
            .setReference("halopesa-disburse-test")
            .setDescription("Halopesa disburse test")
            .setRecipientName("Test Recipient")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_PAYOUT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_PAYOUT_STATUSES + ", got: " + response.status());
    assertEquals(5000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
