/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PaymentStatus;
import com.kwawingu.payments.client.response.PayoutResponse;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercises the real HTTP client against a localhost stub — no Snippe sandbox required. */
public class SnippeHttpClientTest {

  private HttpServer server;
  private SnippeHttpClient client;
  private final SnippeApiKey apiKey = SnippeApiKey.of("test-key");

  private void start(int status, String body) throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(status, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });
    server.start();
    String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    client = new SnippeHttpClient(HttpClient.newHttpClient(), baseUrl);
  }

  @AfterEach
  public void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @BeforeEach
  public void noop() {
    // server started per-test via start(...)
  }

  @Test
  public void postPaymentParsesSuccessBody() throws Exception {
    start(
        201,
        "{\"status\":\"success\",\"data\":{\"reference\":\"SN1\",\"status\":\"pending\","
            + "\"amount\":{\"value\":5000,\"currency\":\"TZS\"},\"created_at\":\"2026-01-01\"}}");
    PaymentResponse r = client.postPayment(apiKey, "ref-001", Map.of("k", "v"));
    assertEquals("SN1", r.reference());
    assertEquals(PaymentStatus.PENDING, r.status());
    assertEquals(5000L, r.amount());
    assertEquals("TZS", r.currency());
  }

  @Test
  public void postPayoutSurfacesErrorCodeOnNon2xx() throws Exception {
    // Snippe returns PAY_004 as an HTTP 400 with the code in the body.
    start(
        400,
        "{\"status\":\"error\",\"code\":400,\"error_code\":\"PAY_004\","
            + "\"message\":\"Insufficient balance to complete this transaction.\"}");
    SnippeApiException ex =
        assertThrows(
            SnippeApiException.class, () -> client.postPayout(apiKey, "ref-002", Map.of()));
    assertEquals("PAY_004", ex.errorCode());
    assertEquals(400, ex.httpStatus());
  }

  @Test
  public void unknownStatusMapsToEnumFallback() throws Exception {
    start(
        201,
        "{\"status\":\"success\",\"data\":{\"reference\":\"SN2\",\"status\":\"weird_new_state\","
            + "\"amount\":{\"value\":1000,\"currency\":\"TZS\"}}}");
    PayoutResponse r = client.postPayout(apiKey, "ref-003", Map.of());
    assertEquals(PaymentStatus.UNKNOWN, r.status());
  }
}
