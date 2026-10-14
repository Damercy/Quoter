#!/usr/bin/env bash
set -euo pipefail

if [[ "${CI:-}" != true ]]; then
  printf '%s\n' 'This setup targets a disposable CI runner.' >&2
  exit 2
fi

# Older avdmanager versions silently encode minor-version platforms as android-0.
sdkmanager --install 'cmdline-tools;22.0'
sdk_tools="$ANDROID_HOME/cmdline-tools/22.0"
sdk_latest="$ANDROID_HOME/cmdline-tools/latest"
sdk_backup="$ANDROID_HOME/cmdline-tools/preinstalled-ci"
test -x "$sdk_tools/bin/avdmanager"
if [[ "$(readlink -f "$sdk_latest")" != "$(readlink -f "$sdk_tools")" ]]; then
  if [[ -e "$sdk_backup" ]]; then
    printf '%s\n' 'A previous SDK backup already exists; refusing to replace it.' >&2
    exit 1
  fi
  mv -- "$sdk_latest" "$sdk_backup"
  ln -s -- "$sdk_tools" "$sdk_latest"
fi
"$sdk_latest/bin/sdkmanager" --version
