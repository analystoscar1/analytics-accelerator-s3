package com.amazon.connector.s3.common.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings(
    value = "NP_NONNULL_PARAM_VIOLATION",
    justification = "We mean to pass nulls to checks")
public class TelemetryTest {
  @Test
  void testGetTelemetry() {
    Telemetry newTelemetry = Telemetry.getTelemetry(TelemetryConfiguration.DEFAULT);
    assertNotNull(newTelemetry);
    assertInstanceOf(ConfigurableTelemetry.class, newTelemetry);
  }

  @Test
  void testCreateTelemetryWithNulls() {
    assertThrows(NullPointerException.class, () -> Telemetry.getTelemetry(null));
  }

  @Test
  void testNoOp() {
    Telemetry noopTelemetry = Telemetry.NOOP;
    assertInstanceOf(DefaultTelemetry.class, noopTelemetry);
    DefaultTelemetry telemetry = (DefaultTelemetry) noopTelemetry;
    assertInstanceOf(NoOpTelemetryReporter.class, telemetry.getReporter());
  }

  @Test
  void testMeasureJoin() throws Exception {
    TickingClock wallClock = new TickingClock(0L);
    TickingClock elapsedClock = new TickingClock(0L);
    CollectingTelemetryReporter reporter = new CollectingTelemetryReporter();
    DefaultTelemetry defaultTelemetry =
        new DefaultTelemetry(wallClock, elapsedClock, reporter, TelemetryLevel.CRITICAL);

    Operation operation = Operation.builder().name("name").attribute("foo", "bar").build();
    final CompletableFuture<Long> completableFuture = new CompletableFuture<>();
    Thread completionThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(5_000);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              elapsedClock.tick(5);
              completableFuture.complete(42L);
            });

    elapsedClock.tick(10);
    wallClock.tick(5);

    // This will complete the future
    completionThread.start();
    defaultTelemetry.measureJoinCritical(() -> operation, completableFuture);
    assertTrue(completableFuture.isDone());
    assertFalse(completableFuture.isCompletedExceptionally());
    assertEquals(42, completableFuture.get());

    assertEquals(1, reporter.getOperationCompletions().size());
    OperationMeasurement operationMeasurement =
        reporter.getOperationCompletions().stream().findFirst().get();
    assertEquals(operation, operationMeasurement.getOperation());
    assertEquals(TelemetryLevel.CRITICAL, operationMeasurement.getLevel());
    assertEquals(10, operationMeasurement.getElapsedStartTimeNanos());
    assertEquals(15, operationMeasurement.getElapsedCompleteTimeNanos());
    assertEquals(5, operationMeasurement.getElapsedTimeNanos());
    assertEquals(5, operationMeasurement.getEpochTimestampNanos());
    assertEquals(Optional.empty(), operationMeasurement.getError());

    // Try again - nothing should be recorded
    long result = defaultTelemetry.measureJoinStandard(() -> operation, completableFuture);
    assertEquals(1, reporter.getOperationCompletions().size());
    assertEquals(42, result);
  }

  @Test
  void testMeasureJoinStandard() throws Exception {
    TickingClock wallClock = new TickingClock(0L);
    TickingClock elapsedClock = new TickingClock(0L);
    CollectingTelemetryReporter reporter = new CollectingTelemetryReporter();
    DefaultTelemetry defaultTelemetry =
        new DefaultTelemetry(wallClock, elapsedClock, reporter, TelemetryLevel.STANDARD);

    Operation operation = Operation.builder().name("name").attribute("foo", "bar").build();
    final CompletableFuture<Long> completableFuture = new CompletableFuture<>();
    Thread completionThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(5_000);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              elapsedClock.tick(5);
              completableFuture.complete(42L);
            });

    elapsedClock.tick(10);
    wallClock.tick(5);

    // This will complete the future
    completionThread.start();
    defaultTelemetry.measureJoinStandard(() -> operation, completableFuture);
    assertTrue(completableFuture.isDone());
    assertFalse(completableFuture.isCompletedExceptionally());
    assertEquals(42, completableFuture.get());

    assertEquals(1, reporter.getOperationCompletions().size());
    OperationMeasurement operationMeasurement =
        reporter.getOperationCompletions().stream().findFirst().get();
    assertEquals(operation, operationMeasurement.getOperation());
    assertEquals(TelemetryLevel.STANDARD, operationMeasurement.getLevel());
    assertEquals(10, operationMeasurement.getElapsedStartTimeNanos());
    assertEquals(15, operationMeasurement.getElapsedCompleteTimeNanos());
    assertEquals(5, operationMeasurement.getElapsedTimeNanos());
    assertEquals(5, operationMeasurement.getEpochTimestampNanos());
    assertEquals(Optional.empty(), operationMeasurement.getError());

    // Try again - nothing should be recorded
    long result = defaultTelemetry.measureJoinStandard(() -> operation, completableFuture);
    assertEquals(1, reporter.getOperationCompletions().size());
    assertEquals(42, result);
  }

  @Test
  void testMeasureJoinVerbose() throws Exception {
    TickingClock wallClock = new TickingClock(0L);
    TickingClock elapsedClock = new TickingClock(0L);
    CollectingTelemetryReporter reporter = new CollectingTelemetryReporter();
    DefaultTelemetry defaultTelemetry =
        new DefaultTelemetry(wallClock, elapsedClock, reporter, TelemetryLevel.VERBOSE);

    Operation operation = Operation.builder().name("name").attribute("foo", "bar").build();
    final CompletableFuture<Long> completableFuture = new CompletableFuture<>();
    Thread completionThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(5_000);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              elapsedClock.tick(5);
              completableFuture.complete(42L);
            });

    elapsedClock.tick(10);
    wallClock.tick(5);

    // This will complete the future
    completionThread.start();
    Long result = defaultTelemetry.measureJoinVerbose(() -> operation, completableFuture);
    assertEquals(42L, result);
    assertTrue(completableFuture.isDone());
    assertFalse(completableFuture.isCompletedExceptionally());
    assertEquals(42, completableFuture.get());

    assertEquals(1, reporter.getOperationCompletions().size());
    OperationMeasurement operationMeasurement =
        reporter.getOperationCompletions().stream().findFirst().get();
    assertEquals(operation, operationMeasurement.getOperation());
    assertEquals(TelemetryLevel.VERBOSE, operationMeasurement.getLevel());
    assertEquals(10, operationMeasurement.getElapsedStartTimeNanos());
    assertEquals(15, operationMeasurement.getElapsedCompleteTimeNanos());
    assertEquals(5, operationMeasurement.getElapsedTimeNanos());
    assertEquals(5, operationMeasurement.getEpochTimestampNanos());
    assertEquals(Optional.empty(), operationMeasurement.getError());

    // Try again - nothing should be recorded
    result = defaultTelemetry.measureJoinStandard(() -> operation, completableFuture);
    assertEquals(1, reporter.getOperationCompletions().size());
    assertEquals(42, result);
  }

  @Test
  void testMeasureJoinCheckNulls() throws Exception {
    TickingClock wallClock = new TickingClock(0L);
    TickingClock elapsedClock = new TickingClock(0L);
    CollectingTelemetryReporter reporter = new CollectingTelemetryReporter();
    DefaultTelemetry defaultTelemetry =
        new DefaultTelemetry(wallClock, elapsedClock, reporter, TelemetryLevel.STANDARD);
    CompletableFuture<Long> completableFuture = new CompletableFuture<>();
    Operation operation = Operation.builder().name("name").attribute("foo", "bar").build();

    assertThrows(
        NullPointerException.class,
        () -> defaultTelemetry.measure(null, () -> operation, completableFuture));

    assertThrows(
        NullPointerException.class,
        () -> defaultTelemetry.measureJoinStandard(null, completableFuture));

    assertThrows(
        NullPointerException.class,
        () -> defaultTelemetry.measureJoinStandard(() -> null, completableFuture));
    assertThrows(
        NullPointerException.class,
        () -> defaultTelemetry.measureJoinStandard(() -> operation, null));
  }

  @Test
  void testAllSignatures() {
    TickingClock wallClock = new TickingClock(0L);
    TickingClock elapsedClock = new TickingClock(0L);
    CollectingTelemetryReporter reporter = new CollectingTelemetryReporter();
    DefaultTelemetry defaultTelemetry =
        new DefaultTelemetry(wallClock, elapsedClock, reporter, TelemetryLevel.VERBOSE);

    OperationSupplier operationSupplier =
        () -> Operation.builder().name("name").attribute("foo", "bar").build();
    TelemetrySupplier<Long> telemetrySupplier = () -> 42L;
    TelemetryAction telemetryAction = () -> {};
    CompletableFuture<Long> completableFuture = new CompletableFuture<>();
    completableFuture.complete(42L);
    // We test all fields of the OperationMeasurements elsewhere.
    // Here our main goal to make sure that operation gets logged with the right level

    // Critical
    defaultTelemetry.measureCritical(operationSupplier, telemetrySupplier);
    assertMeasuredWithLeve(TelemetryLevel.CRITICAL, reporter);
    defaultTelemetry.measureCritical(operationSupplier, telemetryAction);
    assertMeasuredWithLeve(TelemetryLevel.CRITICAL, reporter);
    defaultTelemetry.measureCritical(operationSupplier, completableFuture);
    assertMeasuredWithLeve(TelemetryLevel.CRITICAL, reporter);

    // Standard
    defaultTelemetry.measureStandard(operationSupplier, telemetrySupplier);
    assertMeasuredWithLeve(TelemetryLevel.STANDARD, reporter);
    defaultTelemetry.measureStandard(operationSupplier, telemetryAction);
    assertMeasuredWithLeve(TelemetryLevel.STANDARD, reporter);
    defaultTelemetry.measureStandard(operationSupplier, completableFuture);
    assertMeasuredWithLeve(TelemetryLevel.STANDARD, reporter);

    // Verbose
    defaultTelemetry.measureVerbose(operationSupplier, telemetrySupplier);
    assertMeasuredWithLeve(TelemetryLevel.VERBOSE, reporter);
    defaultTelemetry.measureVerbose(operationSupplier, telemetryAction);
    assertMeasuredWithLeve(TelemetryLevel.VERBOSE, reporter);
    defaultTelemetry.measureVerbose(operationSupplier, completableFuture);
    assertMeasuredWithLeve(TelemetryLevel.VERBOSE, reporter);
  }

  private static void assertMeasuredWithLeve(
      TelemetryLevel level, CollectingTelemetryReporter reporter) {
    assertEquals(1, reporter.getOperationCompletions().size());
    assertEquals(level, reporter.getOperationCompletions().stream().findFirst().get().getLevel());
    reporter.clear();
  }
}
