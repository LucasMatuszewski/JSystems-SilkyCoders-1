#!/usr/bin/env bash

# Claude Code Status Line Script
# Docs: https://code.claude.com/docs/en/statusline
# 2 lines: git|path on line 1, model|context|cost on line 2

set -euo pipefail

input=$(cat)

# ANSI colors
BAR_FILLED='\033[38;5;244m'  # Medium gray for used context
BAR_EMPTY='\033[38;5;240m'   # Dark gray for empty context
GRAY='\033[38;5;245m'
GOLD='\033[38;5;220m'
RESET='\033[0m'

# Check if jq is available
if command -v jq &>/dev/null; then
    HAS_JQ=true
    model_id=$(echo "$input" | jq -r '.model.id // "unknown"')
    model_display=$(echo "$input" | jq -r '.model.display_name // "Unknown"')
    context_used=$(echo "$input" | jq -r '.context_window.used_percentage // 0' | cut -d. -f1)
    used_tokens=$(echo "$input" | jq -r '.context_window.tokens_used // 0')
else
    HAS_JQ=false
    model_id=""
    model_display=""
    context_used=0
    used_tokens=0
fi

# Get git branch (works without jq)
git_branch=""
if git rev-parse --git-dir > /dev/null 2>&1; then
    git_branch=$(git --no-optional-locks rev-parse --abbrev-ref HEAD 2>/dev/null)
fi

# ---------- LINE 1: git | path ----------
# Git branch
git_part=""
if [ -n "$git_branch" ]; then
    git_part="${GRAY}git:$git_branch${RESET}"
fi

# Current directory (shortened)
if [ "$HAS_JQ" = true ]; then
    current_dir=$(echo "$input" | jq -r '.workspace.current_dir // "N/A"')
else
    current_dir=$(echo "$input" | grep -o '"current_dir":"[^"]*"' | sed 's/"current_dir":"//;s/"$//;s/\\\\/\\/g' || echo "N/A")
fi

dir_display="N/A"
if [ "$current_dir" != "N/A" ]; then
    dir_short=$(echo "$current_dir" | sed 's|.*/||')
    parent_dir=$(echo "$current_dir" | sed 's|.*/\([^/]*/[^/]*\)|\1|' | grep -o '[^/]*$' 2>/dev/null || true)
    if [ -n "$parent_dir" ] && [ "$parent_dir" != "$dir_short" ]; then
        dir_display="$parent_dir/$dir_short"
    else
        dir_display="$dir_short"
    fi
fi

# Build line 1
if [ -n "$git_part" ]; then
    line1="$git_part | $dir_display"
else
    line1="$dir_display"
fi

# ---------- LINE 2: model | context bar | cost ----------
# Model name
model_part="?"
if [ "$HAS_JQ" = true ]; then
    model_short="$model_display"
    case "$model_id" in
        *sonnet*) model_short="Sonnet" ;;
        *opus*) model_short="Opus" ;;
        *haiku*) model_short="Haiku" ;;
        *deepseek*) model_short="DeepSeek" ;;
        *glm*) model_short="GLM" ;;
    esac
    model_part="$model_short"
else
    model_part="? (no jq)"
fi

# Progress bar for context
BAR_WIDTH=10
FILLED=$((context_used * BAR_WIDTH / 100))
EMPTY=$((BAR_WIDTH - FILLED))

BAR=""
if [ "$FILLED" -gt 0 ]; then
    printf -v FILL "%${FILLED}s" ""
    BAR="${BAR}${BAR_FILLED}${FILL// /▓}${RESET}"
fi
if [ "$EMPTY" -gt 0 ]; then
    printf -v PAD "%${EMPTY}s" ""
    BAR="${BAR}${BAR_EMPTY}${PAD// /░}${RESET}"
fi

context_part="$BAR $context_used%"

# Cost estimate (using bash arithmetic to avoid bc dependency)
cost_part=""
if [ "$HAS_JQ" = true ] && [ "$used_tokens" -gt 0 ]; then
    # Cost in microdollars (millionths of a dollar) to avoid floating point
    case "$model_id" in
        *opus*)
            microdollars=$((used_tokens * 15 / 1000))  # $15/1M tokens
            ;;
        *sonnet*)
            microdollars=$((used_tokens * 3 / 1000))   # $3/1M tokens
            ;;
        *haiku*)
            microdollars=$((used_tokens * 1 / 4000))   # $0.25/1M tokens
            ;;
        *glm*)
            microdollars=$((used_tokens * 1 / 10000))  # ~$0.1/1M tokens (DeepSeek/GLM cheap)
            ;;
        *)
            microdollars=$((used_tokens * 1 / 1000))   # Default $1/1M
            ;;
    esac

    # Convert to dollars - show up to 3 decimal places for small amounts
    total_cents=$((microdollars / 10000))  # Total in hundredths of a dollar
    dollars=$((total_cents / 100))
    cents=$((total_cents % 100))

    if [ "$dollars" -gt 0 ]; then
        cost_formatted="\$${dollars}.$(printf '%02d' $cents)"
    elif [ "$total_cents" -gt 0 ]; then
        # Under $1 - show cents
        cost_formatted="\$0.$(printf '%02d' $total_cents)"
    else
        # Show tenths of cent for very small amounts
        sub_cents=$((microdollars / 1000))  # thousandths of dollar
        if [ "$sub_cents" -gt 0 ]; then
            cost_formatted="\$0.00$sub_cents"
        else
            cost_formatted=""
        fi
    fi
    cost_part="${GOLD}${cost_formatted}${RESET}"
fi

# Build line 2
line2="$model_part | $context_part"
if [ -n "$cost_part" ]; then
    line2="$line2 | $cost_part"
fi

# ---------- OUTPUT ----------
printf -v output "%s\n%s" "$line1" "$line2"
echo -e "$output"
