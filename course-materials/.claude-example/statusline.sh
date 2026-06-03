#!/usr/bin/env bash

# Claude Code Status Line Script
# Docs: https://code.claude.com/docs/en/statusline
# 2 lines: git|path on line 1, model|context|cost|usage on line 2

set -euo pipefail

input=$(cat)

# ---------- Tokyo Night palette (truecolor) ----------
# 24-bit hex so the status line matches a Tokyo Night terminal theme exactly.
FG='\033[38;2;169;177;214m'      # #a9b1d6 foreground
DIM='\033[38;2;120;124;153m'     # #787c99 comment/dim (separators, labels)
ACCENT='\033[38;2;187;154;247m'  # #bb9af7 magenta (model name)
BLUE='\033[38;2;122;162;247m'    # #7aa2f7 blue (path, context bar fill)
CYAN='\033[38;2;68;157;171m'     # #449dab teal (git branch)
GOLD='\033[38;2;224;175;104m'    # #e0af68 yellow (cost)
GREEN='\033[38;2;158;206;106m'   # #9ece6a green   — usage OK (<50%)
ORANGE='\033[38;2;255;158;100m'  # #ff9e64 orange  — usage warning (50-79%)
RED='\033[38;2;247;118;142m'     # #f7768e red     — usage high (>=80%)
BAR_FILLED='\033[38;2;122;162;247m' # #7aa2f7 blue  — filled context
BAR_EMPTY='\033[38;2;50;52;74m'     # #32344a       — empty context
RESET='\033[0m'

# Themed separator used between segments
SEP="${DIM} | ${RESET}"

# Check if jq is available
if command -v jq &>/dev/null; then
    HAS_JQ=true
    model_id=$(echo "$input" | jq -r '.model.id // "unknown"')
    model_display=$(echo "$input" | jq -r '.model.display_name // "Unknown"')
    context_used=$(echo "$input" | jq -r '.context_window.used_percentage // 0' | cut -d. -f1)
    used_tokens=$(echo "$input" | jq -r '.context_window.tokens_used // 0')
    # Subscription rate limits (Pro/Max only; absent until first API response)
    five_h_pct=$(echo "$input" | jq -r '.rate_limits.five_hour.used_percentage // empty')
    five_h_reset=$(echo "$input" | jq -r '.rate_limits.five_hour.resets_at // empty')
    week_pct=$(echo "$input" | jq -r '.rate_limits.seven_day.used_percentage // empty')
    week_reset=$(echo "$input" | jq -r '.rate_limits.seven_day.resets_at // empty')
else
    HAS_JQ=false
    model_id=""
    model_display=""
    context_used=0
    used_tokens=0
    five_h_pct=""
    five_h_reset=""
    week_pct=""
    week_reset=""
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
    git_part="${CYAN}git:$git_branch${RESET}"
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
dir_part="${BLUE}${dir_display}${RESET}"
if [ -n "$git_part" ]; then
    line1="${git_part}${SEP}${dir_part}"
else
    line1="$dir_part"
fi

# ---------- LINE 2: model | context bar | cost | usage ----------
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
    model_part="${ACCENT}${model_short}${RESET}"
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

context_part="$BAR ${FG}${context_used}%${RESET}"

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
    [ -n "$cost_formatted" ] && cost_part="${GOLD}${cost_formatted}${RESET}"
fi

# ---------- Subscription usage (5h + weekly) ----------
# Claude Code now provides your subscription usage DIRECTLY on stdin as
# `rate_limits` — no API calls, no third-party tools (ccusage, etc.) needed.
# Requirements: a Pro/Max (Claude.ai) subscription and Claude Code >= 2.1.x.
# Notes:
#   - `rate_limits` only appears AFTER the first API response in a session,
#     so the segment is simply hidden at the very start of a chat.
#   - On API-key / Console billing (no subscription) it is absent too, and the
#     script degrades gracefully — the rest of the status line still renders.
# Color by severity, and append a short "resets in" countdown.
usage_color() {
    # $1 = integer percent -> echoes the color escape
    if [ "$1" -ge 80 ]; then printf '%b' "$RED"
    elif [ "$1" -ge 50 ]; then printf '%b' "$ORANGE"
    else printf '%b' "$GREEN"; fi
}

reset_in() {
    # $1 = unix epoch -> echoes compact "3d4h" / "1h23m" / "45m" until reset
    local now diff d h m
    now=$(date +%s)
    diff=$(( $1 - now ))
    [ "$diff" -le 0 ] && { printf 'now'; return; }
    d=$(( diff / 86400 )); h=$(( (diff % 86400) / 3600 )); m=$(( (diff % 3600) / 60 ))
    if [ "$d" -gt 0 ]; then printf '%dd%dh' "$d" "$h"
    elif [ "$h" -gt 0 ]; then printf '%dh%02dm' "$h" "$m"
    else printf '%dm' "$m"; fi
}

usage_part=""
if [ -n "$five_h_pct" ]; then
    p=${five_h_pct%.*}
    seg="$(usage_color "$p")5h:${p}%"
    [ -n "$five_h_reset" ] && seg="$seg(↻$(reset_in "$five_h_reset"))"
    usage_part="${seg}${RESET}"
fi
if [ -n "$week_pct" ]; then
    p=${week_pct%.*}
    seg="$(usage_color "$p")7d:${p}%"
    [ -n "$week_reset" ] && seg="$seg(↻$(reset_in "$week_reset"))"
    usage_part="${usage_part:+$usage_part }${seg}${RESET}"
fi

# Build line 2
line2="${model_part}${SEP}${context_part}"
if [ -n "$cost_part" ]; then
    line2="${line2}${SEP}${cost_part}"
fi
if [ -n "$usage_part" ]; then
    line2="${line2}${SEP}${usage_part}"
fi

# ---------- OUTPUT ----------
printf -v output "%s\n%s" "$line1" "$line2"
echo -e "$output"
