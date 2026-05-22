/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

public record PaymentResponse(
    String reference,
    String status,
    long amount,
    String currency,
    String createdAt,
    String completedAt) {}
