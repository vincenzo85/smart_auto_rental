#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_BASE_DIR="$ROOT_DIR/src/main/java/com/smartautorental/platform"
MIGRATION_DIR="$ROOT_DIR/src/main/resources/db/migration"

log() {
  printf "%s\n" "$1"
}

fail() {
  printf "[ERROR] %s\n" "$1" >&2
  exit 1
}

ensure_non_empty() {
  local value="$1"
  local field="$2"
  [[ -n "$value" ]] || fail "$field is required"
}

sanitize_feature() {
  echo "$1" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9_]+/_/g' | sed -E 's/^_+|_+$//g'
}

sanitize_desc() {
  echo "$1" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g' | sed -E 's/^_+|_+$//g'
}

pascal_case() {
  echo "$1" | sed -E 's/[_-]+/ /g' | awk '{
    for (i = 1; i <= NF; i++) {
      $i = toupper(substr($i,1,1)) tolower(substr($i,2))
    }
    printf "%s", $0
  }' | sed 's/ //g'
}

camel_case() {
  local pascal
  pascal="$(pascal_case "$1")"
  if [[ -z "$pascal" ]]; then
    echo ""
    return
  fi
  echo "$(tr '[:upper:]' '[:lower:]' <<< "${pascal:0:1}")${pascal:1}"
}

create_feature() {
  local feature
  feature="$(sanitize_feature "$1")"
  ensure_non_empty "$feature" "feature"

  local base="$JAVA_BASE_DIR/$feature"
  mkdir -p "$base"/{controller,service,dto,model,repo}

  local readme="$base/README.md"
  if [[ ! -f "$readme" ]]; then
    cat > "$readme" <<EOT
# ${feature} module

Package skeleton for feature \`${feature}\`.

Folders:
- controller
- service
- dto
- model
- repo
EOT
  fi

  log "[OK] Feature created: $feature"
}

create_service() {
  local feature class_name
  feature="$(sanitize_feature "$1")"
  class_name="$(pascal_case "$2")"
  ensure_non_empty "$feature" "feature"
  ensure_non_empty "$class_name" "service name"

  local file="$JAVA_BASE_DIR/$feature/service/${class_name}Service.java"
  [[ ! -f "$file" ]] || fail "File already exists: $file"

  cat > "$file" <<EOT
package com.smartautorental.platform.${feature}.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ${class_name}Service {

}
EOT

  log "[OK] Service created: $file"
}

create_controller() {
  local feature class_name base_name service_class service_field path_segment
  feature="$(sanitize_feature "$1")"
  class_name="$(pascal_case "$2")"
  ensure_non_empty "$feature" "feature"
  ensure_non_empty "$class_name" "controller name"

  if [[ "$class_name" == *Controller ]]; then
    base_name="${class_name%Controller}"
  else
    base_name="$class_name"
    class_name="${class_name}Controller"
  fi

  service_class="${base_name}Service"
  service_field="$(camel_case "$base_name")Service"
  path_segment="$(sanitize_desc "$feature")"

  local file="$JAVA_BASE_DIR/$feature/controller/${class_name}.java"
  [[ ! -f "$file" ]] || fail "File already exists: $file"

  cat > "$file" <<EOT
package com.smartautorental.platform.${feature}.controller;

import com.smartautorental.platform.${feature}.service.${service_class};
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/${path_segment}")
@RequiredArgsConstructor
public class ${class_name} {

    private final ${service_class} ${service_field};
}
EOT

  log "[OK] Controller created: $file"
}

create_dto() {
  local feature name dto_type class_name file
  feature="$(sanitize_feature "$1")"
  name="$(pascal_case "$2")"
  dto_type="${3:-request}"
  ensure_non_empty "$feature" "feature"
  ensure_non_empty "$name" "dto name"

  case "$dto_type" in
    request)
      class_name="${name}Request"
      ;;
    response)
      class_name="${name}Response"
      ;;
    *)
      fail "dto type must be: request|response"
      ;;
  esac

  file="$JAVA_BASE_DIR/$feature/dto/${class_name}.java"
  [[ ! -f "$file" ]] || fail "File already exists: $file"

  cat > "$file" <<EOT
package com.smartautorental.platform.${feature}.dto;

public record ${class_name}() {
}
EOT

  log "[OK] DTO created: $file"
}

create_api_bundle() {
  local feature resource
  feature="$1"
  resource="$2"
  create_feature "$feature"
  create_service "$feature" "$resource"
  create_controller "$feature" "$resource"
  create_dto "$feature" "$resource" request
  create_dto "$feature" "$resource" response
  log "[OK] API bundle generated for feature=$feature resource=$resource"
}

next_migration_version() {
  local latest
  latest="$(find "$MIGRATION_DIR" -maxdepth 1 -type f -name 'V*__*.sql' -printf '%f\n' \
    | sed -E 's/^V([0-9]+)__.*/\1/' | sort -n | tail -1)"

  if [[ -z "$latest" ]]; then
    echo "1"
  else
    echo "$((latest + 1))"
  fi
}

create_migration() {
  local desc version safe_desc file
  desc="$1"
  safe_desc="$(sanitize_desc "$desc")"
  ensure_non_empty "$safe_desc" "migration description"

  version="$(next_migration_version)"
  file="$MIGRATION_DIR/V${version}__${safe_desc}.sql"
  [[ ! -f "$file" ]] || fail "Migration already exists: $file"

  cat > "$file" <<EOT
-- V${version}: ${desc}
-- Write your migration SQL here.
EOT

  log "[OK] Migration created: $file"
}

usage() {
  cat <<'EOT'
Usage:
  ./scripts/scaffold-cli.sh menu
  ./scripts/scaffold-cli.sh feature <feature_name>
  ./scripts/scaffold-cli.sh controller <feature_name> <name>
  ./scripts/scaffold-cli.sh service <feature_name> <name>
  ./scripts/scaffold-cli.sh dto <feature_name> <name> <request|response>
  ./scripts/scaffold-cli.sh api <feature_name> <resource_name>
  ./scripts/scaffold-cli.sh migration <description>

Examples:
  ./scripts/scaffold-cli.sh feature coupon
  ./scripts/scaffold-cli.sh controller coupon Coupon
  ./scripts/scaffold-cli.sh service coupon Coupon
  ./scripts/scaffold-cli.sh dto coupon Coupon request
  ./scripts/scaffold-cli.sh api coupon Coupon
  ./scripts/scaffold-cli.sh migration add_coupon_tables
EOT
}

menu() {
  while true; do
    echo
    echo "=== Smart Auto Rental Scaffold CLI ==="
    echo "1) Create feature skeleton"
    echo "2) Create controller"
    echo "3) Create service"
    echo "4) Create DTO request"
    echo "5) Create DTO response"
    echo "6) Create API bundle (feature+service+controller+dto)"
    echo "7) Create Flyway migration"
    echo "0) Exit"
    printf "Choice: "
    read -r choice

    case "$choice" in
      1)
        printf "Feature name: "
        read -r feature
        create_feature "$feature"
        ;;
      2)
        printf "Feature name: "
        read -r feature
        printf "Controller name (es: Coupon): "
        read -r name
        create_controller "$feature" "$name"
        ;;
      3)
        printf "Feature name: "
        read -r feature
        printf "Service name (es: Coupon): "
        read -r name
        create_service "$feature" "$name"
        ;;
      4)
        printf "Feature name: "
        read -r feature
        printf "DTO base name (es: Coupon): "
        read -r name
        create_dto "$feature" "$name" request
        ;;
      5)
        printf "Feature name: "
        read -r feature
        printf "DTO base name (es: Coupon): "
        read -r name
        create_dto "$feature" "$name" response
        ;;
      6)
        printf "Feature name: "
        read -r feature
        printf "Resource name (es: Coupon): "
        read -r resource
        create_api_bundle "$feature" "$resource"
        ;;
      7)
        printf "Migration description: "
        read -r desc
        create_migration "$desc"
        ;;
      0)
        exit 0
        ;;
      *)
        echo "Invalid choice"
        ;;
    esac
  done
}

main() {
  local cmd="${1:-}"
  case "$cmd" in
    menu)
      menu
      ;;
    feature)
      create_feature "${2:-}"
      ;;
    controller)
      create_controller "${2:-}" "${3:-}"
      ;;
    service)
      create_service "${2:-}" "${3:-}"
      ;;
    dto)
      create_dto "${2:-}" "${3:-}" "${4:-request}"
      ;;
    api)
      create_api_bundle "${2:-}" "${3:-}"
      ;;
    migration)
      create_migration "${2:-}"
      ;;
    ""|-h|--help|help)
      usage
      ;;
    *)
      fail "Unknown command: $cmd"
      ;;
  esac
}

main "$@"
