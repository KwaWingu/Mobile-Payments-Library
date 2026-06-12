/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Pure builder/validation tests — no network, representative of all four providers. */
public class MpesaPayloadBuilderTest {

  private MpesaCollectPayload.Builder validCollect() {
    return new MpesaCollectPayload.Builder()
        .setAmount(5000)
        .setPhone("0712345678")
        .setReference("INV-001")
        .setDescription("Invoice")
        .setFirstName("Test")
        .setLastName("User")
        .setEmail("test@example.com");
  }

  @Test
  public void collectBuildNormalizesPhone() {
    MpesaCollectPayload payload = validCollect().build();
    assertEquals("255712345678", payload.phone());
    assertEquals(5000L, payload.amount());
  }

  @Test
  public void collectBuildRejectsBadPhone() {
    assertThrows(IllegalArgumentException.class, () -> validCollect().setPhone("123").build());
  }

  @Test
  public void collectBuildRejectsBadEmail() {
    assertThrows(IllegalArgumentException.class, () -> validCollect().setEmail("nope").build());
  }

  @Test
  public void collectBuildRejectsLongReference() {
    assertThrows(
        IllegalArgumentException.class, () -> validCollect().setReference("x".repeat(31)).build());
  }

  @Test
  public void collectBuildRejectsNonPositiveAmount() {
    assertThrows(IllegalArgumentException.class, () -> validCollect().setAmount(0).build());
  }

  @Test
  public void disburseBuildNormalizesPhone() {
    MpesaDisbursePayload payload =
        new MpesaDisbursePayload.Builder()
            .setAmount(5000)
            .setPhone("+255712345678")
            .setReference("PAY-001")
            .setDescription("Payout")
            .setRecipientName("Test Recipient")
            .build();
    assertEquals("255712345678", payload.phone());
  }
}
