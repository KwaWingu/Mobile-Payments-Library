/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

public record PayoutResponse(
    String reference, PaymentStatus status, long amount, String currency) {}
