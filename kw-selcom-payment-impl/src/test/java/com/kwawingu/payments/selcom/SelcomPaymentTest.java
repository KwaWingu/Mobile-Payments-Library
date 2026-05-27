/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

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

public class SelcomPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(SelcomPaymentTest.class);
  private static final Set<String> VALID_COLLECT_STATUSES =
      Set.of("pending", "processing", "completed", "failed");
  private static final Set<String> VALID_PAYOUT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "reversed");

  private SelcomPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new SelcomPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    SelcomCollectPayload payload =
        new SelcomCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("selcom-collect-test")
            .setDescription("Selcom collect test")
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
    SelcomDisbursePayload payload =
        new SelcomDisbursePayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("selcom-disburse-test")
            .setDescription("Selcom disburse test")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_PAYOUT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_PAYOUT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
