# Development Notes

# Domain Design

## Overview

The domain model represents the core concepts required to generate and manipulate waveforms. The initial design intentionally focuses on the smallest useful abstraction that can support simple signal generation while remaining extensible for future waveform types.

Rather than designing for every possible use case from the outset, the domain is allowed to evolve incrementally as new requirements emerge. This approach keeps the model simple, understandable, and easy to extend.

## WaveformConfig

`WaveformConfig` encapsulates the parameters required to describe a waveform. In the current implementation, a waveform is characterized by its frequency, duty cycle, and initial state.

```scala
case class WaveformConfig(
    frequencyHz: Double,
    dutyCycle: Double,   // 0.0 to 1.0
    initialState: Int    // 0 or 1
)
```

### frequencyHz

`frequencyHz` specifies the number of waveform cycles generated per second and is expressed in **Hertz (Hz)**.

Examples:

| Frequency | Description |
|-----------|-------------|
| `1.0` | One cycle per second |
| `10.0` | Ten cycles per second |
| `1000.0` | One kilohertz (1 kHz) |
| `1000000.0` | One megahertz (1 MHz) |

The frequency determines the period of the waveform.

```
Period = 1 / Frequency
```

For example:

| Frequency | Period |
|-----------|--------|
| 1 Hz | 1 second |
| 10 Hz | 100 ms |
| 100 Hz | 10 ms |
| 1000 Hz | 1 ms |

---

### dutyCycle

`dutyCycle` specifies the fraction of each waveform period during which the signal remains in the active (high) state.

The valid range is:

```
0.0 ≤ dutyCycle ≤ 1.0
```

where:

| Value | Meaning |
|-------:|---------|
| `0.0` | Signal is always low (OFF) |
| `0.25` | Signal is high for 25% of the period |
| `0.5` | Signal is high for 50% of the period |
| `0.75` | Signal is high for 75% of the period |
| `1.0` | Signal is always high (ON) |

For example, a waveform with:

- Frequency: **10 Hz**
- Duty Cycle: **0.25**

produces a waveform whose period is 100 ms:

```
████________________
```

The signal remains high for 25 ms and low for 75 ms.

---
### initialState

`initialState` specifies start value of the waveform.

The valid values are 0 or 1.

initialState 1 would produce the following pattern
████________________

and initial value of 0 would produce the following pattern.

________________████
---

## Design Rationale

The initial domain model intentionally consists of three parameters.

These three attributes are sufficient to generate common digital waveforms such as:

- Square waves
- PWM (Pulse Width Modulation) signals
- LED blink patterns

Keeping the model minimal has several advantages:

- Easy to understand
- Easy to test
- Stable API
- Minimal implementation complexity
- Supports incremental evolution

Future waveform types may require additional parameters such as:

- Amplitude
- Phase
- Offset
- Rise/Fall time
- Pulse width
- Repeat count

These should be introduced only when required by concrete use cases rather than anticipated prematurely.

---

## Domain Philosophy

The domain model is intentionally independent of:

- Hardware devices
- GPIO implementations
- FPGA platforms
- Browser UI
- REST APIs
- AI services

It describes only **what** waveform should be generated, not **how** it is generated or **where** it is consumed.

This separation enables the same domain model to be reused for simulation, visualization, hardware control, automated testing, and future AI-assisted engineering workflows.

---

## Future Evolution

As muHil evolves, `WaveformConfig` may become a hierarchy of waveform-specific configurations.

For example:

```scala
sealed trait WaveformConfig

case class PwmConfig(
    frequencyHz: Double,
    dutyCycle: Double,
    initialState: Int
) extends WaveformConfig

case class SineWaveConfig(
    frequencyHz: Double,
    amplitude: Double,
    phase: Double = 0.0
) extends WaveformConfig
```

The current implementation deliberately postpones this abstraction until multiple waveform types exist. This allows the domain model to evolve naturally based on real requirements rather than speculation.

---

## WaveformGenerator

### Overview

The `WaveformGenerator` trait defines the interface for generating waveform signals. It produces an `fs2.Stream` of `SignalState` values representing the waveform's amplitude over time.

```scala
trait WaveformGenerator[F[_]]:
    def generate(config: WaveformConfig): fs2.Stream[F, SignalState]
```

### WaveformGeneratorSimulator

`WaveformGeneratorSimulator` is a software implementation that generates discrete-time waveform samples at fixed intervals.

#### Key Design Decisions

1. **Fixed Sample Interval**: The simulator emits samples at a fixed interval (default 10ms), making it predictable and easy to test. This mirrors how FPGA systems sample at a fixed clock rate.

2. **Time-Based Calculation**: Instead of counting samples, the implementation uses elapsed time within each period to determine the signal state. This keeps frequency-related calculations straightforward.

3. **Edge Case Handling**:
   - `dutyCycle = 0.0`: Signal is always low (0.0)
   - `dutyCycle = 1.0`: Signal is always at `initialState`

#### Implementation

```scala
class WaveformGeneratorSimulator[F[_]](sampleIntervalMs: Long) extends WaveformGenerator[F]:

  override def generate(config: WaveformConfig): Stream[F, SignalState] =
    val periodMs = (1000.0 / config.frequencyHz).toLong
    val activeDurationMs = (periodMs * config.dutyCycle).toLong
    
    Stream.iterate(0L)(t => (t + sampleIntervalMs) % periodMs)
      .map { elapsed =>
        val currentState = 
          if (activeDurationMs == 0) 0.0
          else if (elapsed < activeDurationMs) config.initialState.toDouble
          else (1 - config.initialState).toDouble
        SignalState(currentState)
      }
```

#### How It Works

| Step | Description |
|------|-------------|
| 1. Calculate period | `periodMs = 1000 / frequencyHz` (period in milliseconds) |
| 2. Calculate active duration | `activeDurationMs = periodMs * dutyCycle` (how long signal stays at initialState) |
| 3. Generate time sequence | Iterate 0, sampleInterval, 2*sampleInterval, ... modulo period |
| 4. Determine state | If elapsed < activeDurationMs → use initialState, else use inverse |

#### Example: 10Hz, 50% duty cycle, initialState=1

```
Frequency: 10 Hz → Period = 100 ms
Duty Cycle: 0.5 → Active duration = 50 ms
Sample Interval: 10 ms

Time:    0    10   20   30   40   50   60   70   80   90  100
State:   1    1    1    1    1    0    0    0    0    0   1...
         └─────────┬─────────┘└─────────┬─────────┘
            active (50ms)        inactive (50ms)
```

#### Testing

The simulator is tested with the following scenarios:

- 100% duty cycle with initialState=1 → always emits 1.0
- 0% duty cycle with initialState=0 → always emits 0.0
- 50% duty cycle → emits alternating 0 and 1 values
- Initial state verification → first sample matches config.initialState

---

## Summary

The current `WaveformConfig` provides a simple, expressive, and extensible representation of waveform characteristics. It serves as the foundational domain object for signal generation while maintaining a clear separation between domain concepts and implementation details.