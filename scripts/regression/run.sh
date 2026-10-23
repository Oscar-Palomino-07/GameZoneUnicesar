#!/usr/bin/env bash
# Non-regression runner for GameZone Unicesar.
#
# Compiles the CURRENT working tree, then replays every scenario against the
# console menu with fixed inputs and stores what the program printed and what
# it left in the data files. Run it once on develop (baseline) and again after
# each change, then compare both runs with compare.sh.
#
# Usage:   scripts/regression/run.sh <label>        e.g. baseline, after-v01
# Needs:   java/javac on PATH and the gson jar in ~/.m2 (or set GSON_JAR).
#
# Scenarios in scenarios/ run in order and share one data folder (like one
# long user session). Scenarios in scenarios-isolated/ each get a fresh data
# folder; an optional <name>.pre script prepares it (for example, corrupting a
# file). The real data/ folder of the project is never touched.
set -u

LABEL="${1:?usage: run.sh <label>}"
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
OUT="$HERE/output/$LABEL"

if command -v cygpath >/dev/null 2>&1; then
    SEP=';'
    winpath() { cygpath -m "$1"; }
else
    SEP=':'
    winpath() { echo "$1"; }
fi

GSON="${GSON_JAR:-$(find "$HOME/.m2" -name 'gson-*.jar' ! -name '*sources*' ! -name '*javadoc*' 2>/dev/null | sort | tail -1)}"
if [ -z "$GSON" ]; then
    echo "gson jar not found. Run 'mvn compile' once or set GSON_JAR." >&2
    exit 2
fi

rm -rf "$OUT"
mkdir -p "$OUT"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/classes"

echo "Compiling sources ($(git -C "$ROOT" rev-parse --abbrev-ref HEAD))..."
(cd "$ROOT/src/main/java" && find . -name '*.java' > "$WORK/sources.txt" \
    && javac -encoding UTF-8 -d "$(winpath "$WORK/classes")" -cp "$(winpath "$GSON")" @"$(winpath "$WORK/sources.txt")") \
    || { echo "COMPILATION FAILED" >&2; exit 2; }

{
    echo "label:  $LABEL"
    echo "branch: $(git -C "$ROOT" rev-parse --abbrev-ref HEAD)"
    echo "commit: $(git -C "$ROOT" rev-parse --short HEAD)"
    echo "dirty:  $(git -C "$ROOT" status --short -- src | wc -l) changed file(s) under src/"
    echo "date:   $(date '+%Y-%m-%d %H:%M:%S')"
} > "$OUT/META.txt"

CP="$(winpath "$WORK/classes")${SEP}$(winpath "$GSON")"
MONTH="$(date +%-m)"
YEAR="$(date +%Y)"

# run_scenario <input file> <sandbox dir> <name>
run_scenario() {
    local input="$1" sandbox="$2" name="$3"
    sed -e "s/@MONTH@/$MONTH/g" -e "s/@YEAR@/$YEAR/g" "$input" > "$WORK/input.txt"
    (cd "$sandbox" && java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 \
        -Duser.language=es -Duser.country=CO -cp "$CP" com.gamezone.Main < "$WORK/input.txt" > "$OUT/$name.stdout" 2> "$OUT/$name.stderr")
    echo $? > "$OUT/$name.exit"
    mkdir -p "$OUT/$name.data"
    cp "$sandbox"/data/*.json "$OUT/$name.data/" 2>/dev/null || true
    echo "  ran $name (exit $(cat "$OUT/$name.exit"))"
}

echo "Chained scenarios:"
SANDBOX="$WORK/chain"
mkdir -p "$SANDBOX" && cp -r "$HERE/fixtures/data" "$SANDBOX/data"
for f in "$HERE"/scenarios/*.in; do
    [ -e "$f" ] || continue
    run_scenario "$f" "$SANDBOX" "$(basename "$f" .in)"
done

echo "Isolated scenarios:"
for f in "$HERE"/scenarios-isolated/*.in; do
    [ -e "$f" ] || continue
    name="$(basename "$f" .in)"
    SANDBOX="$WORK/iso-$name"
    mkdir -p "$SANDBOX" && cp -r "$HERE/fixtures/data" "$SANDBOX/data"
    [ -f "${f%.in}.pre" ] && (cd "$SANDBOX" && bash "${f%.in}.pre")
    run_scenario "$f" "$SANDBOX" "$name"
done

echo "Done. Results in scripts/regression/output/$LABEL"
