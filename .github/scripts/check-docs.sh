#!/usr/bin/env bash
# Checks a generated Dokka site documents something, and documents the ref it was
# told to. Its own file so the pull request job and the publish job run the same
# code rather than two copies that drift.
set -euo pipefail

site=${1:?usage: check-docs.sh <site-dir> <expected-source-ref>}
ref=${2:?usage: check-docs.sh <site-dir> <expected-source-ref>}

[ -d "$site" ] || { echo "no site at $site" >&2; exit 1; }

# A declaration page, not the index: Dokka writes an index whatever it found.
pages=$(find "$site" -name "*.html" -path "*com.devddagnet.hardwareinsets*" | wc -l | tr -d ' ')
[ "$pages" -ge 10 ] || {
    echo "only $pages documented pages under $site, expected at least 10" >&2
    exit 1
}

# The three packages the library publishes. Missing one is the failure mode a
# file-existence check sails past.
for pkg in \
    "com.devddagnet.hardwareinsets.lib" \
    "com.devddagnet.hardwareinsets.lib.domain" \
    "com.devddagnet.hardwareinsets.lib.platform" ; do
    [ -d "$site/compose-hardware-insets/$pkg" ] || {
        echo "package $pkg is not documented" >&2
        exit 1
    }
done

# The feature. Every source link has to name the ref being published; a link to any
# other ref means a stale cached output or a misread property.
wrong=$(grep -rhoE 'blob/[^/]+/hardware-insets' "$site" | sort -u | grep -v "^blob/$ref/hardware-insets" || true)
[ -z "$wrong" ] || {
    echo "source links point at the wrong ref, expected $ref:" >&2
    echo "$wrong" >&2
    exit 1
}
grep -rq "blob/$ref/hardware-insets" "$site" || {
    echo "no source link names $ref at all" >&2
    exit 1
}

echo "$site documents $pages pages, three packages, source links pinned to $ref"
