#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_FILE="${1:-$ROOT_DIR/project-single-file.txt}"
OUTPUT_BASENAME="$(basename "$OUTPUT_FILE")"
TMP_FILE="$(mktemp)"

cleanup() {
  rm -f "$TMP_FILE"
}
trap cleanup EXIT

if [[ -f "$OUTPUT_FILE" ]]; then
  rm -f "$OUTPUT_FILE"
fi

while IFS= read -r -d '' file; do
  rel_path="${file#$ROOT_DIR/}"

  if grep -Iq . "$file"; then
    {
      echo "===== FILE: $rel_path ====="
      cat "$file"
      echo
    } >> "$TMP_FILE"
  else
    {
      echo "===== FILE: $rel_path (binary skipped) ====="
      echo
    } >> "$TMP_FILE"
  fi
done < <(
  find "$ROOT_DIR" \
    \( -path "$ROOT_DIR/.git" -o -path "$ROOT_DIR/target" -o -path "$ROOT_DIR/.idea" \) -prune -o \
    -type f \
    ! -name "$OUTPUT_BASENAME" \
    -print0 \
  | sort -z
)

mv "$TMP_FILE" "$OUTPUT_FILE"
echo "Generated single file: $OUTPUT_FILE"
