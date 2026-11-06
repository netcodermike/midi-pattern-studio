# MIDI Pattern Studio

Compose one-bar drum patterns on an 8×16 grid, loop them through MIDI, and export standard MIDI files or editable patterns.

[![Build and verify](https://github.com/netcodermike/midi-pattern-studio/actions/workflows/verify.yml/badge.svg)](https://github.com/netcodermike/midi-pattern-studio/actions/workflows/verify.yml)

**Language:** Java · **Platform:** Windows, macOS, Linux desktop · **App type:** Desktop drum step sequencer

First-release learning implementation, with explicit scope below. This is one independently runnable app, not a placeholder in a combined repository.

## Features

- Eight General MIDI percussion rows with 16 sixteenth-note steps.
- Tempo range 40–240 BPM and looping playback snapshots.
- MIDI file export with tempo and paired note-on/off events.
- Properties-based pattern save/open and clear controls.

## Requirements

JDK 17+ with Swing and javax.sound.midi. Audible playback requires an available MIDI sequencer/synthesizer/audio device; model tests and MIDI export do not.

## Quick start

```sh
git clone https://github.com/netcodermike/midi-pattern-studio.git
cd midi-pattern-studio
javac -d build src/MidiStudio.java
java -cp build MidiStudio
```

Commands run from the repository root. Lines marked for a particular operating system apply only there. Language tools are prerequisites, not installed by the app. Close desktop windows normally; terminal/server apps stop with Ctrl+C unless their usage notes say otherwise.

## Example workflow

Place kick steps at 1 and 9, snare at 5 and 13, and closed hats on odd steps. Play the loop, stop, change tempo, and restart. Save/open the editable pattern and export MIDI to inspect in a DAW.

## Architecture, configuration, and data

`MidiStudio.java` maps eight instruments to percussion channel 10 (Java channel index 9). A PPQ-96 Sequence uses 24 ticks per sixteenth and 12-tick note durations. The one-bar end is tick 384. A tempo meta-event stores microseconds per quarter note. Playback loops ticks 0–383 and uses a snapshot; edits require stop/play to take effect. Pattern files store bpm plus 128 validated binary steps. Tests count note-on/off events, validate tick length and pattern encoding, and round-trip standard MIDI bytes without opening a synthesizer.

## Verification

Build with the quick-start command where needed, then run:

```sh
java -cp build MidiStudio --self-test
```

Manual acceptance: Toggle drum steps, play/stop, compare tempo, save/reopen, reject a malformed pattern, export MIDI, and listen on a machine with a synthesizer.

See [validation record](docs/VALIDATION.md) for observed checks and their limits. CI results verify only what the workflow runs; they do not establish native GUI correctness or production readiness.

## Limitations and next steps

One bar, fixed drum mapping/velocity/note duration, no swing, piano roll, multi-pattern songs, recording, device selection, or audio/WAV rendering. Playback depends on system MIDI/audio support and may be unavailable in VMs. Exports contain MIDI events rather than sound. Pattern files are plaintext; saving/exporting replaces the chosen path. Closing or clearing does not prompt for unsaved edits. Playback does not update live while editing.

## Troubleshooting

Use a JDK for javac and run from a graphical desktop. A missing synthesizer affects playback, not model tests or export. If edits are inaudible, stop/play to load a new snapshot. Invalid pattern sizes/tempo are rejected before replacing the grid. Exported MIDI needs a MIDI-capable player/DAW.

## Source map

- `src/MidiStudio.java`

## Development and license

Read [contributing](CONTRIBUTING.md) before changing the app, and [security/privacy](SECURITY.md) before supplying private data. Use synthetic fixtures for tests. No telemetry, paid API key, or account is required by the app itself. Dependencies, when present, are stated in Requirements.

[MIT license](LICENSE), copyright 2026 netcodermike.
