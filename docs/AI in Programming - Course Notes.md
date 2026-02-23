
Notes for my presentation for JSystem AI in Programming course I will lead in 01.2026

Survey results: [Form - Tally](https://tally.so/forms/lbdPzv?tab=answers)

## PYTANIA NA START:
- Jak do tej pory używasz AI w pracy? Jakich narzędzi i w jaki sposób? (poziom autonomii)
- Jakie są Twoje doświadczenia? Pomaga? Przyspiesza? Spowalnia?
	- Czy używasz AGENTS.md, Rules?
- Największe problemy jakie masz z AI
- Co sądzisz o AI i przyszłości programowania?

---
## Trends 2025 and what to expect in 2026

- Knowledge base on latest trends: [NotebookLM](https://notebooklm.google.com/notebook/5a69e473-2453-4ee7-bcb6-866af29ba553)
- **Vibe Codding vs Vibe Engineering**
	- Linus Torvalds, DHH, and Node.js creator use automated code generation, e.g.:
		- "[the era of humans writing code is over.](https://x.com/kimmonismus/status/2013524952553492933)" (Node.js creator on AI generating code)
	- Microsoft named their new Power Apps platform Vibe - we prompt to build apps: [Vibe - Power Apps](https://vibe.powerapps.com/) (only in US right now) | Docs: [Omówienie nowego środowiska usługi Power Apps - Power Apps | Microsoft Learn](https://learn.microsoft.com/pl-pl/power-apps/vibe/overview)
	- Vibe Coding definition: [X.com](https://x.com/karpathy/status/1886192184808149383?lang=en)
- **Context rot & Context engineering**
- **Async Codding Agents in Cloud & on Mobile**
- CLI tools, Automation, YOLO in container/cloud
- Huge jump in quality of **Opus 4.5**, GPT 5.2 and Gemini 3
- **Open Source LLMs** so close to top models (GLM-4.7 and Minimax M2.1)
- **AI disappointment**, broken promises, productivity drop, less fun and joy, frustration:
	- [AI Coding Sucks - YouTube](https://www.youtube.com/watch?v=0ZUkQF6boNg)
	- undeterministic nature of LLMs,
	- hallucinations,
	- crazy pace of changes in AI tools,
	- skill degradation, vanishing muscle memory
		- AI detoks

### Research data on AI Coding:

- Knowledge base with research data: [NotebookLM](https://notebooklm.google.com/notebook/ae14d724-0256-482a-85b8-95fca9ba1c11)
- **Stanford** research on ROI of AI code (from 07.2025):
	- YT talk 12.2025 [Can you prove AI ROI in Software Eng? (Stanford 120k Devs Study) – Yegor Denisov-Blanch, Stanford - YouTube](https://www.youtube.com/watch?v=JvosMkuNxF8)
	- Insights on [X.com](https://x.com/dexhorthy/status/1992266735970652447)
	- Slides: [Yegor Denisov-Blanch - The AI Conference](https://aiconference.com/speakers/yegor-denisov-blanch/)
	- Original talk from 07.2025 on YT: [Does AI Actually Boost Developer Productivity? (100k Devs Study) - Yegor Denisov-Blanch, Stanford - YouTube](https://www.youtube.com/watch?v=tbDDYKRFjhk)
- US, 12.2025: [Beyond Productivity: Evaluating the Hidden Costs of Generative AI in Software Development by Edward Anderson, Geoffrey Parker, Burcu Tan :: SSRN](https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5842302)
- METR, US, 07.2025: [Measuring the Impact of Early-2025 AI on Experienced Open-Source Developer Productivity - METR](https://metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/)
- India 09.2025: [\[2509.19708\] Intuition to Evidence: Measuring AI's True Impact on Developer Productivity](https://arxiv.org/abs/2509.19708)
- Meta: [Measuring the Impact of AI on Developer Productivity at Meta - YouTube](https://www.youtube.com/watch?v=1OzxYK2-qsI)
- **QUESTION:**
	- **Do you measure velocity / ROI / time estimations / Quality?** e.g.
	- Velocity in Jira with story points,
	- number of comments on CR,
	- number of issues/bugs/tickets,
	- it helps to measure AI impact on their work
- **The Productivity J-Curve** - productivity drops for 1-2 months when you learn

### Case Studies:

- Microsoft plans to rewrite all code from C/C++ to Rust using AI and algorithms
	- Linkedin post from [Galen Hunt - Principal Software Engineer (CoreAI)](https://www.linkedin.com/posts/galenh_principal-software-engineer-coreai-microsoft-activity-7407863239289729024-WTzf)
	 My goal is to eliminate every line of C and C++ from Microsoft by 2030. Our strategy is to combine AI *and* Algorithms to rewrite Microsoft’s largest codebases. Our North Star is “1 engineer, 1 month, 1 million lines of code”.   To accomplish this previously unimaginable task, we’ve built a powerful code processing infrastructure. Our algorithmic infrastructure creates a scalable graph over source code at scale. Our AI processing infrastructure then enables us to apply AI agents, guided by algorithms, to make code modifications at scale. The core of this infrastructure is already operating at scale on problems such as code understanding.

### AI real impact we already see in Dev/IT:
- TailwindCSS financial issues
	- people stopped visiting Docs = less sales of their products = how to maintain OSS Projects???
	- [The Tailwind drama - YouTube](https://www.youtube.com/watch?v=luhgjBrRulk)
	- PR comment: [feat: add llms.txt endpoint for LLM-optimized documentation by quantizor · Pull Request #2388 · tailwindlabs/tailwindcss.com · GitHub](https://github.com/tailwindlabs/tailwindcss.com/pull/2388#issuecomment-3717222957)
	- Adam Wathan on X: [morning walk talk](https://x.com/adamwathan/status/2008909129591443925)
- StackOverflow is dying [Prepare your goodbyes - YouTube - Primagen](https://www.youtube.com/watch?v=Gy0fp4Pab0g)
- All Publishers see huge decline in Traffic because of AI search tools - "The Great Decoupling" (people search more, but traffic goes down - 60% of Google searches without clicks!)
- Long Video Courses are dying - people stop to use Udemy and similar platforms
	- [Why I stopped making coding tutorials - YouTube](https://www.youtube.com/watch?v=WCGTQBCE3FA)

---

## AI Fundamentals

- **Knowledge Cutoff** - AI doesn't know latest versions & features of Tools/Languages
	- Better use **Java 21 (2023)** than 25 (07.2025)
	- Add context, docs for latest features
	- Overfitting - LLM may still struggle, ignore context and follow old path
- **Hallucinations** - RAG / Context / Browser helps to solve this issue, but not 100%
- **Context Window**
	- Temporary "working memory" of LLM,
	- Attention (uwaga, skupienie),
	- Context Rot,
	- Lost in the Middle
- **Tokens**, Tokenizer, Embedding (osadzanie), Vector DB, etc.
- **Autoregression**
	- model predicts future values in a sequence by using a linear combination of its own past values
	- one mistake may lead to cascade / domino effect of mistakes (better to start again - branching in ChatGPT, Cursor can)
-  What LLM needs to support to use it as **AI Agent**???
	- **Structured Output** / Json schema
	- **Function Calling** / Tool usage
- **RAG** - architecture / concept of augmenting context / input with real data
	- Retrieval-Augmented Generation (creator regrets this name ;)
	- Semantic search in vector space, Vector DB, Graph DB, Hybrid
	- Reranking
- **Cache** - cost optimization, caching calculated weights for same input tokens
- **Reasoning**, Reinforced Learning & R1:
	- [How was DeepSeek-R1 built; For dummies : r/LLMDevs](https://www.reddit.com/r/LLMDevs/comments/1ibhpqw/how_was_deepseekr1_built_for_dummies/)
	- It changed a lot in AI, enabled Vibe Coding, Autonomus Agents
	- Should we always use reasoning models??? :)
- Types of AI Assistance in programming:
	- **autocomplete** (starting from [Tabnine](https://www.tabnine.com/), GH Copilot, now Cursor Tab [based on Supermaven](https://supermaven.com/blog/cursor-announcement))
	- Chat / Research / Talk about your code
	- Inline Generation
	- Code Generation, planning, TODOs, Tools, MCPs, etc.
	- Agentic workflows, task automation (local), not only codding, e.g.
		- Goose
		- Claude Cowork: [Introducing Cowork | Claude](https://claude.com/blog/cowork-research-preview)
	- Agentic workflows in the cloud, CI/CD, Repo

## LLM benchmarks, best models

- SWE Bench: [SWE-bench Leaderboards](https://www.swebench.com/)
- LMArena WebDev [Just a moment...](https://lmarena.ai/pl/leaderboard/webdev)
- Terminal Bench: [Terminal-Bench](https://www.tbench.ai/leaderboard/terminal-bench/2.0)
- Tool Calling: [Berkeley Function Calling Leaderboard (BFCL) V4](https://gorilla.cs.berkeley.edu/leaderboard.html)
- AIME (High School Math Exam): [AIME 2025 Benchmark Leaderboard | Artificial Analysis](https://artificialanalysis.ai/evaluations/aime-2025)
- Market Share - Open Router: [LLM Rankings | OpenRouter](https://openrouter.ai/rankings)

---

### Best AI Codding Tools
- Gartner: [Best AI Code Assistants Reviews 2026 | Gartner Peer Insights](https://www.gartner.com/reviews/market/ai-code-assistants)
- YT comparison: [Best AI Coding Tools for Developers in 2026 - YouTube](https://www.youtube.com/watch?v=pvMGRSZJ4Jw&t=330s)
- **IDE & plugins:**
	- GitHub Copilot: [GitHub Copilot · Plans & pricing · GitHub](https://github.com/features/copilot/plans)
		- Not only GPT, Opus 4.5 in all plans: [Claude Opus 4.5 is now generally available in GitHub Copilot - GitHub Changelog](https://github.blog/changelog/2025-12-18-claude-opus-4-5-is-now-generally-available-in-github-copilot/)
	- Cursor [Cursor](https://cursor.com/)
	- Zed [Zed — Love your editor again](https://zed.dev/) (Open Source)
		- [Should You Use Zed In 2026? - YouTube](https://www.youtube.com/watch?v=lRrElGM23h4)
	- Antigravity by Google [Google Antigravity Product](https://antigravity.google/product)
	- [Junie | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/junie.html)
	- [Cline - AI Coding, Open Source and Uncompromised](https://cline.bot/)
	- [Augment Code - The Software Agent Company](https://www.augmentcode.com/)
	- [Kilo - Move at Kilo Speed](https://kilo.ai/)
	- [Tabnine AI Code Assistant | Smarter AI Coding Agents. Total Enterprise Control.](https://www.tabnine.com/)
	- Windsurf / Codeium
	- .... list can go on....
- **CLI tools:**
	- Knowledge Base with comparisons: [NotebookLM](https://notebooklm.google.com/notebook/44128edb-6841-4315-909c-4402b2d13bd1)
	- Claude Code [Claude Code - AI coding agent for terminal & IDE | Claude](https://claude.com/product/claude-code)
	- Gemini CLI (open source) [Build, debug & deploy with AI | Gemini CLI](https://geminicli.com/)
	- Codex CLI [Codex CLI](https://developers.openai.com/codex/cli/)
	- **Goose** (also has good Desktop app): [Goose](https://block.github.io/goose/) (Open Source)
	- OpenCode [OpenCode | The open source AI coding agent](https://opencode.ai/) (Open Source)
	- Aider [Aider - AI Pair Programming in Your Terminal](https://aider.chat/) (Open Source)
	- Copilot CLI: [About GitHub Copilot CLI - GitHub Enterprise Cloud Docs](https://docs.github.com/en/enterprise-cloud@latest/copilot/concepts/agents/about-copilot-cli)
	- Droid by Factory: [Factory | Agent-Native Software Development](https://factory.ai/)
	- Letta Code (MemGPT): [Quickstart | Letta Docs](https://docs.letta.com/letta-code/quickstart) (Open Source)
	- AIChat for Terminal commands help: [GitHub - aichat - Shell Assistant, Chat-REPL, RAG, AI Agent](https://github.com/sigoden/aichat) (Open Source)
	- ... many more....

#### Codex - official plugin from OpenAI for VSCode "family":
- [https://developers.openai.com/codex/ide/](https://developers.openai.com/codex/ide/)
- VSCode / Cursor / Antigravity / Windsurf: [https://open-vsx.org/extension/openai/chatgpt#review-details](https://open-vsx.org/extension/openai/chatgpt#review-details)

#### Claude Code extension for VSCode "family":
[https://open-vsx.org/extension/Anthropic/claude-code#review-details](https://open-vsx.org/extension/Anthropic/claude-code#review-details)


---

## Pricing - rate limits

[NotebookLM](https://notebooklm.google.com/notebook/8a4739c4-f9b3-41a2-aaf0-612daa0750ce) on pricing of main AI Coding tools
[NotebookLM](https://notebooklm.google.com/notebook/ddbe409f-832b-4d19-ac7d-aed2e059d2c2) on pricing & quality of OpenSource Models (GLM/ M2) vs big providers

- **Google Gemini AI Pro** plan with CLI / Code Assist / Antigravity:
	- on Pro: 120 requests/m, 1500 r/day - [Quotas and limits  |  Gemini Code Assist ](https://developers.google.com/gemini-code-assist/resources/quotas)
	- Antigravity on Pro "high generous quota refreshed every 5h": [Google Antigravity Quota](https://antigravity.google/docs/plans)
- **JetBrains** AI Assistant has only 10 credits for $10 = around 100 requests/month
	- [Licensing and subscriptions | AI Assistant Documentation](https://www.jetbrains.com/help/ai-assistant/licensing-and-subscriptions.html#individual-use)
- **Claude Code Pro** ($17-20/m) & Max ($100 5x, $200 20x Pro) plans:
	- [Pricing | Claude](https://claude.com/pricing)
- **GitHub Copilot** 
	- Free: 50 premium requests/m + 2000 completions (NO: CLI, PRs, issue assign,  code reviews)
	- Pro for $10/m - unlimited GPT-5 mini, 300 premium requests (also Opus 4.5)
	- Pro+ for $39/m - 1500 premium requests
	- [GitHub Copilot · Plans & pricing · GitHub](https://github.com/features/copilot/plans)
	- [Plans for GitHub Copilot - GitHub Docs](https://docs.github.com/en/copilot/get-started/plans) (detailed docs)
- **Cursor Pro** for $20/m provides best autocomplete
	- [Pricing · Cursor](https://cursor.com/pricing)
- **ChatGPT Plus** for $20/m
	- provides access to **Codex** (both CLI and Cloud agents with GitHub integration)
- **GLM Codding Plan**
	- GLM-4.7 is probably the best open source Coding LLM (some argue that M2 is better/faster)
	- Lite plan may be slow but costs only $3/m and offers 3x usage of Claude Code Pro!
	- Pro plan is for $12 for the first year, $30/m later (5x Lite plan = 15x Claude Code Pro)
	  Video on GLM-4.7 and Minimax  M2.1: [So close to Opus at 1/10th the price (GLM-4.7 and Minimax M2.1 showdown) - YouTube](https://www.youtube.com/watch?v=kEPLuEjVr_4)
	- Invite link with additional 10%: https://z.ai/subscribe?ic=IA5VBRGQV4
- **Tetrate.ai** $15 for free (OpenRouter for Enterprise):
	- 1. Create account for $5 free: [Tetrate: Safe, Fast, and Profitable AI for the Enterprise](https://tetrate.io/)
	- 2. Use goose CLI to add Tetrate Agent Router for $10 more: [Quickstart | goose](https://block.github.io/goose/docs/quickstart/)

## My Recommendations - tools to install/use:

- **Handy STT** (like SuperWhisper but Open Source MIT)
	- [Handy](https://handy.computer/)
	- architecture details (Tauri): [Code Wiki](https://codewiki.google/github.com/cjpais/handy)
- Zed.ai - to use any AI tool/Provider you wont, Open Source, Rust (fast)
- Cursor - for best autocomplete and cloud agents, RAG on any docs
- GitHub Copilot - for integration with JetBrains IDEs & Code Reviews online
- Google AI Pro - Gemini App, Gmail, Docs + Gemini CLI + Antigravity + Veo
	- NotebookLM for learning / knowledge base: [NotebookLM](https://notebooklm.google.com/)
- [OpenRouter](https://openrouter.ai/) for control, observability and 1 API Key for all tools
- [Goose](https://block.github.io/goose/) Agent for programming, PC control & automation workflows
	- Advent of AI: [Advent of AI](https://adventofai.dev/) (by Goose)
	- based on: [Advent of Code 2025](https://adventofcode.com/)


---
## AI Agents Tool-belt:

- **Rules** - custom prompts per project / folder / type of file
	- Cursor, GitHub Copilot & Claude Code has path based targeting e.g. `"src/api/**/*.ts"`
	- in Cluade Code: [Path Specific Rules](https://code.claude.com/docs/en/memory#path-specific-rules)
	- in Cursor: [Rules | Cursor Docs](https://cursor.com/docs/context/rules)
	- in OpenCode: [Rules | OpenCode](https://opencode.ai/docs/rules/)
	- in Codex CLI: [Rules](https://developers.openai.com/codex/rules)
	- examples:
		- [payload/templates/website/.cursor/rules at main · payloadcms/payload · GitHub](https://github.com/payloadcms/payload/tree/main/templates/website/.cursor/rules)
- **AGENTS.md**
	- Growing standard: [AGENTS.md](https://agents.md/)
	- CLAUDE.md [CLAUDE.MD files: Customizing Claude Code for your codebase | Claude](https://claude.com/blog/using-claude-md-files)
	- GEMINI.md [Provide context with GEMINI.md files | Gemini CLI](https://geminicli.com/docs/cli/gemini-md/)
	- Discussions / Sources:
		- X.com thread: [Matt Pocock](https://x.com/mattpocockuk/status/2012906065856270504)
		- Article & Prompt to fix AGENTS.md: [A Complete Guide To AGENTS.md](https://www.aihero.dev/a-complete-guide-to-agents-md)
	- Examples:
		- [mcp-for-beginners/AGENTS.md at main · microsoft/mcp-for-beginners · GitHub](https://github.com/microsoft/mcp-for-beginners/blob/main/AGENTS.md) (includes Java Spring Boot)
		- [payload/templates/website/AGENTS.md at main · payloadcms/payload · GitHub](https://github.com/payloadcms/payload/blob/main/templates/website/AGENTS.md)
- **SKILLS.md**
	- [GitHub - agentskills/agentskills: Specification and documentation for Agent Skills](https://github.com/agentskills/agentskills)
	- [Overview - Agent Skills](https://agentskills.io/home)
		- Library of Skills from Vercel: [The Agent Skills Directory](https://skills.sh/)
	- in Copilot: [About Agent Skills - GitHub Docs](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills)
	- in Claude Code: [Agent Skills - Claude Code Docs](https://code.claude.com/docs/en/skills)
	- in Gemini CLI: [Agent Skills | Gemini CLI](https://geminicli.com/docs/cli/skills/)
	- in Cursor: [Agent Skills | Cursor Docs](https://cursor.com/docs/context/skills)
	- in OpenCode: [Agent Skills | OpenCode](https://opencode.ai/docs/skills/)
	- in Codex CLI: [Agent Skills](https://developers.openai.com/codex/skills)
- **Tools** - built-in Agent tools (provided by IDE / CLI you use):
	- Terminal
	- Browse / Search
	- Docs (RAG) lub Contex7 MCP jeśli nie ma docs wbudowanych
	- GIT
	- TODO list
	- Browser integration (e.g. in Cursor) / Chrome-devtools-mcp
	- + Custom Tools
- **LSP** (Language Server Protocol)
	- allows agent to use diagnostic tools and understand syntax, linting errors
- **MCP** Servers (Model Context Protocol)
	- MCP as type of a tool based on open standard for anybody to build tools for agents
	- Problems:
		- Context bloating = Context rot,
		- Too many choices for agent (max 5x MCP servers / 50 tools at once!)
		- Slower than just Bash scripts, terminal commands (Agent also can use them!)
	- Introduced by Anthropic in 2024: [Introducing the Model Context Protocol \\ Anthropic](https://www.anthropic.com/news/model-context-protocol)
	- Donated to Linux Foundation subsidiary: [Donating the MCP and the Agentic AI Foundation](https://www.anthropic.com/news/donating-the-model-context-protocol-and-establishing-of-the-agentic-ai-foundation)
	- Open Standard: [What is the Model Context Protocol (MCP)? - Model Context Protocol](https://modelcontextprotocol.io/docs/getting-started/intro)
	- Why MCP moved from SSE to Streamable HTTP: [SSE vs. Streamable HTTP - which will be the standard for remote servers? : r/mcp](https://www.reddit.com/r/mcp/comments/1kdyse2/sse_vs_streamable_http_which_will_be_the_standard/)
	- Example usage for Agents:
		- in Claude Code: [Connect Claude Code to tools via MCP - Claude Code Docs](https://code.claude.com/docs/en/mcp)
		- in Zed: [Redirecting... | Zed Code Editor Documentation](https://zed.dev/docs/assistant/model-context-protocol)
		- in Cursor: [Model Context Protocol (MCP) | Cursor Docs](https://cursor.com/docs/context/mcp)
		- in OpenCode: [MCP servers | OpenCode](https://opencode.ai/docs/mcp-servers/)
	- Most Popular:
		- **Context7** - documentation: [Context7 - Up-to-date documentation for AI](https://context7.com/)
		- Chrome DevTools: [GitHub - Chrome DevTools for coding agents](https://github.com/ChromeDevTools/chrome-devtools-mcp)
		- Browser Use: [MCP Server - Browser Use](https://docs.browser-use.com/customize/integrations/mcp-server)
		- IntelliJ IDEA: [MCP Server | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/mcp-server.html)
		- GitHub: [MCP GitHub · GitHub](https://github.com/mcp/io.github.github/github-mcp-server)
		- Linear, GitLab, Atlassian, Azure DevOps
		- Notion, PostHog, Postman, Sentry, ...
		- PostgreSQL, MongoDB, ...
		- Figma
		- Blender
- **ACP** - use CLI Agents in other tools / IDEs
	- Created by Zed IDE to use Claude Code in Zed (now also for Codex, Gemini, OpenCode)
	- in ZED [Zed — Agent Client Protocol](https://zed.dev/acp)
	- in OpenCode: [ACP Support | OpenCode](https://opencode.ai/docs/acp/)
	- in Goose: [Using goose in ACP Clients | goose](https://block.github.io/goose/docs/guides/acp-clients)
- **Sub-Agents**
	- in Claude Code: [Create custom subagents - Claude Code Docs](https://code.claude.com/docs/en/sub-agents)
	- in Cursor: [Subagents | Cursor Docs](https://cursor.com/docs/context/subagents)
	- in Goose: [Subagents | goose](https://block.github.io/goose/docs/guides/subagents/)
- Parallel Agents / Git Worktree
	- in Cursor: [Parallel Agents | Cursor Docs](https://cursor.com/docs/configuration/worktrees)
- Custom Agents (custom system prompt / instructions)
	- In Copilot: [About custom agents - GitHub Docs](https://docs.github.com/en/copilot/concepts/agents/coding-agent/about-custom-agents)
	- in OpenCode: [Agents | OpenCode](https://opencode.ai/docs/agents/)

## Best Practices / Methodologies for Agentic Codding:

- Plan & Solve
- Plan-Execute-Test-Commit loop
- Good AGENTS.md and rules for better understanding of the project
- Make AI Agent ask you questions instead of acting immediately (Custom agent or in AGENTS.md)
- Product Requirement Document (PRD): 
	- [GitHub - snarktank/ai-dev-tasks: A simple task management system for managing AI dev agents](https://github.com/snarktank/ai-dev-tasks)
	- [Legal agent income up-cert PRD creation - Amp](https://ampcode.com/threads/T-019b98b9-3fe1-77ea-9d43-235c62200559)
- TODO List / Task list / Progress tracking
	-  Not needed with Opus 4.5? [TODOs Are Done - Amp](https://ampcode.com/news/todos-are-done)
- **System 2 Thinking**
	- refers to a **slow, deliberate** approach to software development that prioritizes logic and architecture over intuitive, rapid generation. This philosophy has emerged as a rigorous defense against "slop"—the influx of unmaintainable or hallucinated code that often results from "System 1" thinking (fast, intuitive "vibe coding")
- Tips for OpenCode: [Don’t sign up the yearly plan, it’s a trap : r/ZaiGLM](https://www.reddit.com/r/ZaiGLM/comments/1q2oqhx/comment/nxf1idh/)
	- Agent RPI-V8 [Claudette coding agent](https://gist.github.com/bizzkoot/bdf957cd745de8c788df3ca7f353daad#file-rpi-v8-opencode-agent-md)
	- Skill **Superpowers** [GitHub - obra/superpowers: An agentic skills framework & software development methodology that works.](https://github.com/obra/superpowers/tree/main)
	- MCP sequential-thinking
- Clavix - templates for better prompts. PRDs
	- [GitHub - ClavixDev/Clavix: Transform vague ideas into production-ready prompts. Analyze gaps, generate PRDs, and supercharge your AI coding workflow with the CLEAR framework.](https://github.com/ClavixDev/Clavix)

### Ralph Wiggum Bash loop:

- Knowledge Base: [NotebookLM](https://notebooklm.google.com/notebook/c92dbf13-8174-42eb-9f1d-15be3f7c842d)
- original post from Geoffrey Huntley: [Ralph Wiggum as a "software engineer"](https://ghuntley.com/ralph/)
- repo that Geoffrey supports: [GitHub - ghuntley - The Ralph Wiggum Technique—the AI development methodology that reduces software costs to less than a fast food worker's wage.](https://github.com/ghuntley/how-to-ralph-wiggum)
- Video where G.H. explains Ralph: [Ralph Wiggum (and why Claude Code's implementation isn't it) with Geoffrey Huntley and Dexter Horthy - YouTube](https://www.youtube.com/watch?v=O2bBWDoxO4s)
- Ryan Carson version: [GitHub - snarktank/ralph: Ralph is an autonomous AI agent loop that runs repeatedly until all PRD items are complete.](https://github.com/snarktank/ralph) / on [X.com](https://x.com/ryancarson/status/2008548371712135632)
	- G.H. wrote "it isn't it" on [X.com](https://x.com/GeoffreyHuntley/status/2008731415312236984)
- Ralph meme coin and technique explanation: [$RALPH - The Memecoin That's Helping AI Ship Code While You Sleep](https://ralphcoin.org/#technique)
- Ralph Wiggum from 1st principles: [The Ralph Wiggum Loop from 1st principles (by the creator of Ralph) - YouTube](https://www.youtube.com/watch?v=4Nna09dG_c0)
- Claude Code Ralph plugin (official, but not fixing context rot): [claude-code/plugins/ralph-wiggum at main · anthropics/claude-code · GitHub](https://github.com/anthropics/claude-code/tree/main/plugins/ralph-wiggum)
- Goose with Ralph loop: [Ralph Loop | goose](https://block.github.io/goose/docs/tutorials/ralph-loop)
- Detailed technical video: ["Ralph Wiggum" AI Agent will 10x Claude Code/Amp - YouTube](https://www.youtube.com/watch?v=RpvQH0r0ecM)

---

## Cloud Agents / Containerization / VM - Security

- Cursor Cloud Agents [Cursor Agents](https://cursor.com/agents)
- Copilot Agents: [GitHub Copilot Agents](https://github.com/copilot/agents)
	- Docs: [About GitHub Copilot coding agent - GitHub Enterprise Cloud Docs](https://docs.github.com/en/enterprise-cloud@latest/copilot/concepts/agents/coding-agent/about-coding-agent)
- Goose in Docker: [Building goose in Docker | goose](https://block.github.io/goose/docs/tutorials/goose-in-docker)

## Other AI Tools and Ecosystem

 - **Design / UX / FE with AI:**
	 - Google: [Stitch - Design with AI](https://stitch.withgoogle.com/?pli=1)
	 - Figma AI plugins
		 - Figma MCP: [Guide to the Figma MCP server – Figma Learn - Help Center](https://help.figma.com/hc/en-us/articles/32132100833559-Guide-to-the-Figma-MCP-server)
	 - Lovable
	 - v0 from Vercel
	 - GitHub Spark [GitHub Spark · Dream it. See it. Ship it. · GitHub](https://github.com/features/spark)
	 - Tailwind & Shadcn popularity
 - **Code Reviews, GitHub integrations**
	 - qodo - [AI Code Review for Teams – IDE, GitHub, GitLab & CLI](https://www.qodo.ai/) (open source)
	 - CodeRabbit [AI Code Reviews | CodeRabbit | Try for Free](https://www.coderabbit.ai/)
		 - GitLab Sef-Managed: [CodeRabbit GitLab](https://docs.coderabbit.ai/platforms/self-hosted-gitlab)
	 - Gemini Code Assist: [Review GitHub code using Gemini Code Assist](https://developers.google.com/gemini-code-assist/docs/review-github-code)
		 - Consumer - config files: [Customize Gemini Code Assist behavior in GitHub  |  Google for Developers](https://developers.google.com/gemini-code-assist/docs/customize-gemini-behavior-github#consumer)
		 - Enterprise - Cloud Console: [Customize Gemini Code Assist behavior in GitHub  |  Google for Developers](https://developers.google.com/gemini-code-assist/docs/customize-gemini-behavior-github#enterprise)
	- Gemini CLI w CI/CD:
		- Google Course: [Sprawdzanie kodu i analiza bezpieczeństwa za pomocą interfejsu wiersza poleceń Gemini](https://codelabs.developers.google.com/gemini-cli-code-analysis?hl=pl#0)
		- GitLab MR Article: [Building an automated GitLab Merge Request Review Agent with Gemini CLI  | Medium](https://medium.com/google-cloud/building-an-automated-gitlab-merge-request-review-agent-with-gemini-cli-35855d53bec1)
	- GitHub Copilot:
		- [Configuring automatic code review by GitHub Copilot - GitHub Docs](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/request-a-code-review/configure-automatic-review)
		- [Using GitHub Copilot code review - GitHub Docs](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/request-a-code-review/use-code-review)
	 - OpenAI Codex [Use Codex in GitHub](https://developers.openai.com/codex/integrations/github/)
	 - Cursor Bugbot: [Bugbot | Cursor Docs](https://cursor.com/docs/bugbot)
	 - Cursor CLI in GH Actions: [Code Review with Cursor CLI | Cursor Docs](https://cursor.com/docs/cli/cookbook/code-review)
 - **Debugging, Security, Docs**
	 - Sentry Seer: [Seer: AI debugging agent that works for you. | Sentry](https://sentry.io/product/seer/)
	 - [Jam | Build a bug-free product.](https://jam.dev/)
	 - [Mend.io - AI Powered Application Security](https://www.mend.io/)
	 - [Mintlify - The Intelligent Documentation Platform](https://www.mintlify.com/)
	 - Snyk: [Snyk AI-powered Developer Security Platform | AI-powered AppSec Tool & Security Platform | Snyk](https://snyk.io/)
	 - Snyk Evo: [Security for Agentic AI Applications and Tools | Evo by Snyk | Evo](https://evo.ai.snyk.io/)
	 - Snyk vs Mend: [Mend.io vs Snyk 2026 | Gartner Peer Insights](https://www.gartner.com/reviews/market/application-security-testing/compare/mend-io-vs-snyk)
	 - **Markdown** is best for AI Docs, and supports many additional features, eg.
		 - Diagrams with Mermaid [Mermaid | Diagramming and charting tool](https://mermaid.js.org/)
		 - Slides with [Marp: Markdown Presentation Ecosystem](https://marp.app/)
		 - **Confluence** synchronization in GH Action: [confluence-markdown-sync · Actions · GitHub Marketplace · GitHub](https://github.com/marketplace/actions/confluence-markdown-sync)
			 - [GitHub - andygolubev/github-to-confluence-publisher](https://github.com/andygolubev/github-to-confluence-publisher)
			 - [GitHub - mihaeu/cosmere: Sync your markdown files to Confluence](https://github.com/mihaeu/cosmere)
- Frameworks & Tools for Agents:
	- [AI SDK by Vercel](https://ai-sdk.dev/docs/introduction)
	- Rust [GitHub - 0xPlaygrounds/rig: ⚙️🦀 Build modular and scalable LLM Applications in Rust](https://github.com/0xPlaygrounds/rig)
	- [LangChain](https://www.langchain.com/) & [LangGraph](https://www.langchain.com/langgraph)
	- [The Leading Multi-Agent Platform](https://www.crewai.com/)
	- [Flowise - Build AI Agents, Visually](https://flowiseai.com/)
	- [AI Workflow Automation Platform & Tools - n8n](https://n8n.io/)

### JetBrains AI Ecostystem
 
 - Article: [Best Software Composition Analysis Tools - Qodana Blog](https://blog.jetbrains.com/qodana/2025/09/best-software-composition-analysis-tools/) 
   (Qodana, Mend, Snyk, OWASP, Black Duck, FOSSA)
 - **MCP Server:** [MCP Server | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/mcp-server.html)
 - **AI Assistant** - [AI Assistant in JetBrains IDEs](https://www.jetbrains.com/help/idea/ai-assistant-in-jetbrains-ides.html?utm_source=product&utm_medium=link&utm_campaign=IU&utm_content=2025.3)
	 - **low quota** - only 10 credits in Pro for $10 (~10 agent requests): [Licensing and subscriptions | AI Assistant Documentation](https://www.jetbrains.com/help/ai-assistant/licensing-and-subscriptions.html#ai-quota)
	 - **Junie** AI Agent (unified in Chat now): [Junie, the AI coding agent by JetBrains - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/26104-junie-the-ai-coding-agent-by-jetbrains)
 - **GH Copilot** - higher quota, also on free tier (GPT-5 mini, Haiku 4.5)
	 - [GitHub Copilot - Your AI Pair Programmer - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/17718-github-copilot--your-ai-pair-programmer)
	 - [GitHub Copilot app modernization - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/28791-github-copilot-app-modernization) (legacy code, refactors)
 - **AI Unit Testing**: [Diffblue Cover - AI Agent for unit testing - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/14946-diffblue-cover--ai-agent-for-unit-testing)
	 - Does NOT use LLM. Own small RL model that generates tests automatically!
	 - has mixed reviews, probably LLM based agent can write better tests nowadays
	 - Specialized in Java, JUnit and TestNG
 - **SonarQube** static analysis (to control better AI generated code):
	 - [SonarQube for IDE - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/7973-sonarqube-for-ide)
	 - [SonarQube Plans & Pricing | Static Code Analysis Tool | Sonar](https://www.sonarsource.com/plans-and-pricing/)
 - **Qodana** Static Analysis & CI/CD:
	 - [Qodana: Static Code Analysis Tool by JetBrains](https://www.jetbrains.com/qodana/)

### SDK for AI Apps in Spring / Java:
- Spring AI:
	- [Introduction :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)
	- [OpenAI Chat :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
	- [OpenAI SDK Chat (Official) :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/chat/openai-sdk-chat.html)
- Official Open AI SDK:
	- [GitHub - openai/openai-java: The official Java library for the OpenAI API](https://github.com/openai/openai-java)
	- [Libraries | OpenAI API](https://platform.openai.com/docs/libraries?language=java&desktop-os=windows)
- UI Libraries:
	- Vercel AI SDK UI [AI SDK UI: Overview](https://ai-sdk.dev/docs/ai-sdk-ui/overview)
	- assistant-ui [GitHub - assistant-ui/assistant-ui: Typescript/React Library for AI Chat💬🚀](https://github.com/assistant-ui/assistant-ui)
