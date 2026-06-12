/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PayloadValidationTest {

  @Test
  public void normalizePhoneAcceptsCommonTanzanianForms() {
    assertEquals("255712345678", PayloadValidation.normalizePhone("+255712345678"));
    assertEquals("255712345678", PayloadValidation.normalizePhone("255712345678"));
    assertEquals("255712345678", PayloadValidation.normalizePhone("0712345678"));
    assertEquals("255712345678", PayloadValidation.normalizePhone("  255712345678  "));
  }

  @Test
  public void normalizePhoneRejectsInvalid() {
    assertThrows(NullPointerException.class, () -> PayloadValidation.normalizePhone(null));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.normalizePhone("12345"));
    assertThrows(
        IllegalArgumentException.class, () -> PayloadValidation.normalizePhone("25571234567"));
    assertThrows(
        IllegalArgumentException.class, () -> PayloadValidation.normalizePhone("notaphone"));
  }

  @Test
  public void requireEmailValidatesFormat() {
    assertEquals("a@b.com", PayloadValidation.requireEmail("a@b.com"));
    assertThrows(NullPointerException.class, () -> PayloadValidation.requireEmail(null));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.requireEmail("nope"));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.requireEmail("a@b"));
  }

  @Test
  public void requireReferenceEnforcesLength() {
    assertEquals("INV-001", PayloadValidation.requireReference("INV-001"));
    assertThrows(NullPointerException.class, () -> PayloadValidation.requireReference(null));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.requireReference("  "));
    assertThrows(
        IllegalArgumentException.class, () -> PayloadValidation.requireReference("x".repeat(31)));
  }

  @Test
  public void requirePositiveAmountRejectsNonPositive() {
    assertEquals(5000L, PayloadValidation.requirePositiveAmount(5000L));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.requirePositiveAmount(0));
    assertThrows(IllegalArgumentException.class, () -> PayloadValidation.requirePositiveAmount(-1));
  }
}
