/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

import org.checkerframework.checker.nullness.qual.Nullable;

public record PaymentResponse(
    String reference,
    PaymentStatus status,
    long amount,
    String currency,
    @Nullable String createdAt,
    @Nullable String completedAt) {}
