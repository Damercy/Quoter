# Local device configuration

The current Windows workspace uses the existing
`Resizable_Experimental_API_VanillaIceCream` AVD for phone, fold and tablet checks.
Keep its native window available and do not create additional local AVDs.

Software rendering and three-button navigation are the current workaround for
graphics saturation and the edge-swipe monitor ANR. Cold boot when required.
Gesture behavior and physical performance remain unverified.

If the local screenshot mirror is running, pause it through
`http://127.0.0.1:8765/pause` during boot, builds and automated interaction;
resume through `/resume` for manual viewing. The native emulator is the
interactive window. Do not start an absent mirror.

Remote physical-device validation is deferred. Use verified free quota only
when resuming it, and release reservations after testing. See
[migration validation](MIGRATION-VALIDATION.md) for outstanding checks.
