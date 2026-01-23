#!/bin/bash
# Usage: ./loop.sh [plan] [max_iterations]
# Examples:
#   ./loop.sh              # Build mode, unlimited iterations
#   ./loop.sh 20           # Build mode, max 20 iterations
#   ./loop.sh plan         # Plan mode, unlimited iterations
#   ./loop.sh plan 5       # Plan mode, max 5 iterations

# CONFIGURATION
GEMINI_CMD="gemini" # Adjust if your binary is named differently (e.g. 'google-gemini')
LOG_DIR="logs"

# Ensure log directory exists
mkdir -p "$LOG_DIR"

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed."
    echo "Please install jq from: https://jqlang.org/"
    exit 1
fi

# Parse arguments
if [ "$1" = "plan" ]; then
    # Plan mode
    MODE="plan"
    PROMPT_FILE="PROMPT_plan.md"
    MAX_ITERATIONS=${2:-0}
elif [[ "$1" =~ ^[0-9]+$ ]]; then
    # Build mode with max iterations
    MODE="build"
    PROMPT_FILE="PROMPT_build.md"
    MAX_ITERATIONS=$1
else
    # Build mode, unlimited (no arguments or invalid input)
    MODE="build"
    PROMPT_FILE="PROMPT_build.md"
    MAX_ITERATIONS=0
fi

ITERATION=0
CURRENT_BRANCH=$(git branch --show-current)

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Mode:   $MODE"
echo "Prompt: $PROMPT_FILE"
echo "Branch: $CURRENT_BRANCH"
echo "Logs:   $LOG_DIR"
[ $MAX_ITERATIONS -gt 0 ] && echo "Max:    $MAX_ITERATIONS iterations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Verify prompt file exists
if [ ! -f "$PROMPT_FILE" ]; then
    echo "Error: $PROMPT_FILE not found"
    exit 1
fi

while true; do
    if [ $MAX_ITERATIONS -gt 0 ] && [ $ITERATION -ge $MAX_ITERATIONS ]; then
        echo "Reached max iterations: $MAX_ITERATIONS"
        break
    fi

    LOG_FILE="$LOG_DIR/loop_${ITERATION}.jsonl"
    echo "Starting Loop $ITERATION - Full Debug Log: $LOG_FILE"

        # Run Ralph iteration with streaming JSON output
        # 1. Pipe prompt to gemini
        # 2. Redirect stderr to stdout (2>&1) so both go into the pipe
        # 3. Use tee to save RAW mixed output (text logs + JSON) to log file
        # 4. Use jq with -R (raw input) to handle the mixed stream:
        #    - Try to parse line as JSON
        #    - If valid JSON: Apply formatting logic
        #    - If text (parsing fails): Print as dim gray text (system logs)

        cat "$PROMPT_FILE" | \
        $GEMINI_CMD --yolo --output-format stream-json 2>&1 | \
        tee "$LOG_FILE" | \
        jq -R -r --unbuffered '
            # Try to parse the raw line as JSON
            (try fromjson catch null) as $json |

            if $json then
                # Valid JSON Event
                if $json.type == "tool_use" then
                    "\u001b[36m[Tool] " + $json.tool_name + "\u001b[0m"
                elif $json.type == "tool_result" and $json.status == "error" then
                    "\u001b[31m[Error] " + $json.tool_name + ": " + $json.output + "\u001b[0m"
                elif $json.type == "message" and $json.role == "assistant" and $json.content != null then
                    $json.content
                elif $json.type == "error" then
                     "\u001b[31m[System Error] " + $json.message + "\u001b[0m"
                else
                    empty
                end
            else
                # Not JSON (System Log from stderr) - Print in dim gray
                "\u001b[90m[Log] " + . + "\u001b[0m"
            end'
    # Push changes after each iteration
    # git push origin "$CURRENT_BRANCH" || {
    #     echo "Failed to push. Creating remote branch..."
    #     git push -u origin "$CURRENT_BRANCH"
    # }
    # Git push removed to prevent hanging on credentials.
    # Commits are local only.

    ITERATION=$((ITERATION + 1))
    echo -e "\n\n======================== LOOP $ITERATION FINISHED ========================\n"
done
