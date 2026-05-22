/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

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

public class MixxByYasPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(MixxByYasPaymentTest.class);
  private static final Set<String> VALID_STATUSES =
      Set.of("pending", "processing", "completed", "failed");

  private MixxByYasPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new MixxByYasPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    MixxByYasCollectPayload payload =
        new MixxByYasCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("+255676000000")
            .setReference("mixxbyyas-collect-test")
            .setDescription("MixxByYas collect test")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertTrue(
        VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    MixxByYasDisbursePayload payload =
        new MixxByYasDisbursePayload.Builder()
            .setAmount(1000L)
            .setPhone("+255676000000")
            .setReference("mixxbyyas-disburse-test")
            .setDescription("MixxByYas disbursement test")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertTrue(
        VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
