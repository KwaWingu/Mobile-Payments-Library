/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(CardPaymentTest.class);

  private CardPaymentImpl payment;

  @BeforeEach
  public void setUp() {
    payment = new CardPaymentImpl.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCheckoutUrl() throws IOException, InterruptedException {
    CardCollectPayload payload =
        new CardCollectPayload.Builder()
            .setAmount(1000L)
            .setEmail("test@example.com")
            .setReference("card-checkout-test")
            .setDescription("Card checkout test")
            .build();

    String checkoutUrl = payment.checkoutUrl(payload);

    assertNotNull(checkoutUrl, "checkoutUrl must not be null");
    assertFalse(checkoutUrl.isBlank(), "checkoutUrl must not be blank");
    assertTrue(checkoutUrl.startsWith("https://"), "checkoutUrl must start with https://");
    LOG.info("card checkout url: {}", checkoutUrl);
  }
}
