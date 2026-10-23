#!/usr/bin/env bash
# Compares two runs produced by run.sh and reports every difference.
#
# Usage:   scripts/regression/compare.sh <reference-label> <new-label>
# Exit:    0 when both runs are identical, 1 when there are differences.
#
# A label is looked up first in output/ (your local runs) and then in
# reference/ (shared runs committed to the repository).
#
# Before comparing, values that legitimately change between runs are masked:
# dates (sales and warranties use today's date), the month/year typed in the
# monthly balance, and the line numbers of Java stack traces.
set -u

A="${1:?usage: compare.sh <reference-label> <new-label>}"
B="${2:?usage: compare.sh <reference-label> <new-label>}"
HERE="$(cd "$(dirname "$0")" && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

resolve() {
    if [ -d "$HERE/output/$1" ]; then
        echo "$HERE/output/$1"
    elif [ -d "$HERE/reference/$1" ]; then
        echo "$HERE/reference/$1"
    fi
}

meta() {
    echo "$(grep -h '^commit' "$1/META.txt"), $(grep -h '^branch' "$1/META.txt")"
}

for label in "$A" "$B"; do
    SRC="$(resolve "$label")"
    [ -n "$SRC" ] || { echo "No run named '$label' (looked in output/ and reference/). Run run.sh $label first." >&2; exit 2; }
    cp -r "$SRC" "$WORK/$label"
    rm -f "$WORK/$label/META.txt"
    find "$WORK/$label" -type f -exec sed -i -E \
        -e 's/[0-9]{4}-[0-9]{2}-[0-9]{2}/<DATE>/g' \
        -e 's/Balance mensual [0-9]{2}\/[0-9]{4}/Balance mensual <MM\/YYYY>/' \
        -e 's/\([A-Za-z0-9_]+\.java:[0-9]+\)/(<src>)/g' \
        -e 's/\r$//' {} +
done

echo "Reference: $A  ($(meta "$(resolve "$A")"))"
echo "New:       $B  ($(meta "$(resolve "$B")"))"
echo

STATUS=0
for f in "$WORK/$A"/*.exit; do
    name="$(basename "$f" .exit)"
    kinds=""
    for ext in stdout stderr exit; do
        diff -q "$WORK/$A/$name.$ext" "$WORK/$B/$name.$ext" >/dev/null 2>&1 || kinds="$kinds $ext"
    done
    diff -rq "$WORK/$A/$name.data" "$WORK/$B/$name.data" >/dev/null 2>&1 || kinds="$kinds data"
    if [ -z "$kinds" ]; then
        printf 'OK    %s\n' "$name"
    else
        printf 'DIFF  %s  [%s ]\n' "$name" "$kinds"
        STATUS=1
    fi
done

if [ "$STATUS" -ne 0 ]; then
    mkdir -p "$HERE/output"
    diff -ru "$WORK/$A" "$WORK/$B" | sed -e "s#$WORK/##g" > "$HERE/output/diff-$A-vs-$B.txt"
    echo
    echo "Differences found. Full detail: scripts/regression/output/diff-$A-vs-$B.txt"
else
    echo
    echo "No differences: behavior is identical."
fi
exit $STATUS
