#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
version=$(sed -n 's/^mod_version[[:space:]]*=[[:space:]]*//p' "$root/gradle.properties")
tag=${1:?Provide the release tag}
[[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+-1\.7\.10$ && ${tag#v} == "$version" ]] || {
    echo "Tag $tag must match mod_version ($version), optionally prefixed with v." >&2
    exit 1
}
