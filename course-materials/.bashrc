# Set default editor for CLI tools
export EDITOR=zed

# Claude Code backend switchers
# Usage:
#   claude-or [claude args...]        -> OpenRouter, default model: deepseek/deepseek-v3.2
#   claude-zai [claude args...]       -> Z.ai Coding Plan, default model mapping to GLM-4.7
#   claude-ollama [claude args...]    -> local Ollama, default model: qwen3-coder
#   claude-ollama-cloud [args...]     -> Ollama cloud, default model: glm-5:cloud
# Optional model overrides:
#   CLAUDE_CODE_MODEL="z-ai/glm-5" claude-or
#   CLAUDE_CODE_MODEL="glm-5" claude-zai
#   CLAUDE_CODE_MODEL="glm-4.7:cloud" claude-ollama-cloud
# Check active model inside Claude Code with:
#   /status

# Alternative methods with npm modules:
# npm install -g ccconfig
#   ccc add openrouter
#   ccc use openrouter
#
# npm install -g claude-provider
#   # Inside Claude Code:
#   /provider:add deepseek
#   /provider:switch deepseek


_claude_clear_backend_env() {
  unset ANTHROPIC_BASE_URL
  unset ANTHROPIC_API_KEY
  unset ANTHROPIC_AUTH_TOKEN
  unset ANTHROPIC_CUSTOM_HEADERS
  unset ANTHROPIC_DEFAULT_HAIKU_MODEL
  unset ANTHROPIC_DEFAULT_SONNET_MODEL
  unset ANTHROPIC_DEFAULT_OPUS_MODEL
}

claude-native() {
  _claude_clear_backend_env
  export ANTHROPIC_API_KEY="${ANTHROPIC_API_KEY:-$ANTHROPIC_KEY}"
  claude "$@"
}

claude-or() {
  _claude_clear_backend_env
  export ANTHROPIC_BASE_URL="https://openrouter.ai/api"
  export ANTHROPIC_API_KEY=""
  export ANTHROPIC_AUTH_TOKEN="${OPENROUTER_API_KEY}"
  export ANTHROPIC_CUSTOM_HEADERS="HTTP-Referer: https://openrouter.ai, X-Title: Claude Code via Git Bash"
  export ANTHROPIC_DEFAULT_SONNET_MODEL="${CLAUDE_CODE_MODEL:-deepseek/deepseek-v3.2}"
  export ANTHROPIC_DEFAULT_OPUS_MODEL="${CLAUDE_CODE_MODEL:-deepseek/deepseek-v3.2}"
  export ANTHROPIC_DEFAULT_HAIKU_MODEL="${CLAUDE_CODE_MODEL:-deepseek/deepseek-v3.2}"
  export CLAUDE_CODE_SUBAGENT_MODEL="${CLAUDE_CODE_MODEL:-deepseek/deepseek-v3.2}"
  claude "$@"
}

claude-zai() {
  _claude_clear_backend_env
  export ANTHROPIC_BASE_URL="https://api.z.ai/api/anthropic"
  export ANTHROPIC_API_KEY=""
  export ANTHROPIC_AUTH_TOKEN="${ZAI_API_KEY}"
  export ANTHROPIC_DEFAULT_SONNET_MODEL="${CLAUDE_CODE_MODEL:-GLM-4.7}"
  export ANTHROPIC_DEFAULT_OPUS_MODEL="${CLAUDE_CODE_MODEL:-GLM-4.7}"
  export ANTHROPIC_DEFAULT_HAIKU_MODEL="${CLAUDE_CODE_HAIKU_MODEL:-GLM-4.7}"
  export API_TIMEOUT_MS="3000000"
  claude "$@"
}

claude-ollama() {
  _claude_clear_backend_env
  export ANTHROPIC_BASE_URL="${OLLAMA_ANTHROPIC_BASE_URL:-http://localhost:11434}"
  export ANTHROPIC_API_KEY=""
  export ANTHROPIC_AUTH_TOKEN="${OLLAMA_ANTHROPIC_AUTH_TOKEN:-ollama}"
  export ANTHROPIC_DEFAULT_SONNET_MODEL="${CLAUDE_CODE_MODEL:-glm-5:cloud}"
  export ANTHROPIC_DEFAULT_OPUS_MODEL="${CLAUDE_CODE_MODEL:-glm-5:cloud}"
  export ANTHROPIC_DEFAULT_HAIKU_MODEL="${CLAUDE_CODE_HAIKU_MODEL:-glm-5:cloud}"
  claude "$@"
}

alias ccn='claude-native'
alias cco='claude-or'
alias ccz='claude-zai'
alias ccol='claude-ollama'
