# Validation

Authoring date: November 6, 2026. Host: Windows. Target: Windows, macOS, Linux desktop.

## Local result

JDK/audio unavailable on authoring host. CI compiles and tests MIDI generation without opening an audio device.

## Reproducible checks

The GitHub Actions workflow builds/runs the following on `ubuntu-latest`:

```sh
javac -d build src/MidiStudio.java
java -cp build MidiStudio --self-test
```

## Manual acceptance

Toggle drum steps, play/stop, compare tempo, save/reopen, reject a malformed pattern, export MIDI, and listen on a machine with a synthesizer.

A workflow file is not evidence of a passing run. Observed CI run links and source commits are recorded after publication. Native GUI, sound, sleep/resume, and platform permissions require direct target-platform acceptance even when model tests pass.

## Observed CI result

Passed: [target-platform build/check](https://github.com/netcodermike/midi-pattern-studio/actions/runs/42204138450). Source commit: `69b37f070ad5af0c5ddeacfb930a1492f529c5c3`. Later documentation-only changes do not alter the tested implementation. The reproducible commands above identify test scope; native GUI/audio/permissions are not implied by compilation.
