# Silky Coders 03.2026 - notatki z odręcznych skanów

Opracowanie na podstawie zdjęć notatek, w kolejności czasowej plików. To nie jest surowy OCR, tylko uporządkowany odczyt treści z dopowiedzeniem struktury. Fragmenty niepewne oznaczyłem cytatem blokowym.

Pierwsze strony z imionami uczestników to notatki z rozmowy o ich problemach z AI, dotychczasowych doświadczeniach i oczekiwaniach wobec kursu.

---

## Kontekst rozmowy

- Start zajęć i zebranie oczekiwań
- Rozmowa o problemach z AI i agentami
- Rozpoznanie doświadczenia uczestników

## Uczestnicy i ich potrzeby

### Łukasz

- Codex
- MCP
- Jira, GitHub, Confluence
- Agent
- Hallucynacje, co robić?
- Claude

### Artur

- FE developer
- React
- Codex w IntelliJ
- Gemini app prywatnie
- Testy, commit names
- Hallucynacje
- Weryfikacja odpowiedzi

### Sebastian

- Java
- System architect
- React
- JS, TS
- Java, C++
- Mało pisze kodu teraz, zarządza teraz głównie
- Testy, Architektura, Diagramy
- Problem i oczekiwania:
  - Hallucynacje
  - Przegląd narzędzi

### Daniel

- Senior Java dev
- AI assistant Junie w IntelliJ
- Full stack, też FE
- Problemy:
  - Spam (LLM / AI Agent dużo generuje textu)
  - Hallucynacje
  - Gubienie kontekstu

### Tomasz

- Architect
- Outlook, Teams, Excel
- Czytanie kodu
- Bolt.new bawił się
- Cursor IDE używa na co dzień
- Agenci trochę
- ChatGPT używa, Deep Research często
- PlantUML w Markdown (podobny do Mermaid)
- Problemy
  - Dużo tekstu / spam generowany przez modele LLM
  - Hallucynacje, wymyśla rzeczy

### Kamil

- Java dev
- DevOps
- FE więcej z AI ostatnio generuje
- Od 3 tygodni nie pisze kodu samemu, całkowicie agenci generują
- Terminal 100% przez CLI agentów
- Claude Code głównie
- Skills, Subagenci
- Poradził sobie sam z hallucynacjami
- Problemy i oczekiwania:
  - Współbieżna praca subagentów
  - Lepiej definiować skills
  - Agent zapomina, że skill ma używać

### Karol

- Kotlin
- IntelliJ
- Full stack
- Server side
- HTMX
- Android
- Izolacja agentów w Linux, WSL, Docker, sandbox
- Problemy:
  - Nie używa agentów
  - Ma złe wspomnienia z Junie AI Agent w IntelliJ, długo trwa generowanie i słaba jakość

### Rafał

- Tech lead
- BE, DevOps, FE
- Z AI od 8 miesięcy
- Chat, Sonnet, Junie z IntelliJ
- Claude Code
- Problemy:
  - Sam nie wychwyci błędów, trzeba go pilnować
  - Jak nauczyć agenta swojego stylu programowania, aby pamiętał
  - Testy integracyjne z aentami
    - Gubią się agenci, testy nie działają
    - TDD?
    - Testuje Endpoint i wynik requestu
    - Chce bazę danych z danymi a nie mocki
    - Happy Path i Edge Case
    - Agent za dużo kombinuje, robi słabe asercje
    - Używa obecnie Java class Factory do robienia Seed Bazy Danych
      - dać to agentowi jako narzędzie aby sam seedował zanim testy zrobi
- Który agent do czego najlepiej działa (Claude vs Codex vs ...)

### Arek

- Java dev
- Kotlin
- React
- BE głównie
- iOS
- Analiza, research
- Problemy:
  - Mniej pisania kodu
  - Agent gubi się, Marnowanie czasu
  - W dużym projekcie agent gubi się bardzo
  - Mało używa AI bo słabo to działa
  - Archetypy w kodzie słabo wykrywają agenci (legacy code)
- Subagenci
- TDD Chce z agentami robić
- Chciałby AI-first pracować
  - aby agent podejmował decyzje np. architektoniczne
  - teraz ma tendencję do decydowania samemu, nie oddaje kontroli,
  - czyta wszystko

### Patryk

- Android
- Kotlin
- BE
- AI Chat w Android Studio
- Gemini App
- Błędy wykrywa wrzucając problemy do Gemini App lub AI Chat
- Testy robi z AI ale ręcznie
- nie używa agentów, tylko chat prosty

---

## Pytania i tematy do wyjaśnienia

- /rewind - jak działa kompresja przy rewind?
- 1M context in Claude is now default? In Desktop?
  - 2x price >200k as in the past?
- Agent Review w Jenkins workflows
  - Więcej pracy w chmurze, review w chmurze
  - Oddelegować poza CI/CD workflow
  - Połączenie z Jira, Confluence on premisses
- Claude Desktop Dispatch (mobile app)
  - Teams licence? - NO, Pro / Max only
  - requires Private mobile phone
- Sandbox na MacOS vs Linux i WSL2 (brak sandbox na Windows, tylko w WSL2)
- Envs do Claude w `.claude` folder?

## Claude Auto Mode

- Teams only
- Admin settings
- similar to Codex guardian approvals (but Codex notify human on every issue)
- uses much more tokens (separate LLM rund to review commands)
- still not 100% safe.
- Tells Agent to try different method. After 5 blocks notify human.

---

## Przydatne funkcje

- `/voice` mode -> hold Space to talk, change language config for whole app to use Polish language (in both UI / agent responses and for voice dictation)
- `/update-config` - Claude helps with own config file, np. Allow command
- `/schedule` to automate work, schedule tasks
- Claude Debugging:
  - `/doctor`
  - `/export`
  - `/debug`
  - `/heapdump`

---

## Skróty i komendy w Claude

- `Ctrl + R` - search prompts
  - `Ctrl + Shift + F` w Windows Terminal - powiązany skrót do szukania
- `!command` - bash mode
  - to execute commands and provide output to agent's context window
  - e.g. `!git status`
- `Shift + Tab` - toggle: auto accept changes, plan mode
- `Ctrl + O` - verbose output, see thinking, tools output (not everything)
- `Ctrl + Shift + -` - undo (`-` = minus)
- `Alt + V` - paste image from clipboard, e.g. screenshot
- `Ctrl + S` - stash prompt (don't send immediatelly)
- `/btw` - side question, like separate micro-session without interupting work
- `enter` - Just write next prompt when agent thinks to Queue prompts
- `Ctrl + G` - edit prompt in `$EDITOR` set in `.bashrc`
  - `@filename` nie działa w edytorze, ale wygodne dla długich promptów
- `/fast` - zużywa 2x tokenów, ale działa szybciej
  - używaj jeśli rzedko korzystasz, nie dobijasz do limitu i tak.
- `/update-config <prompt>` - agent zmieni swoje ustawienia
  - e.g. add Playwright MCP na poziomie globalnym / projektu
- `/mcp` - włączenie / wyłączenie MCP

## Pętle i agenci

- `/loop <time> <command/prompt>` - (10m default), repeat same prompt/command
- `/batch` - 5-30 worktree agents - Each creates separate PR

---

## Diagramy i dokumentacja

- **PlantUML** w Markdown
  - GitLab: tak
  - GitHub: nie, zamiast tego **Mermaid**

## Praktyka pracy w CLI

- Nie pisać długich promptów w oknie CLI - łatwo w CLI niechcący je usunąć
- Używać `Ctrl + G` (edit prompt in editor)
- Alternatywnie używać desktop app

## Pytania konfiguracyjne

- `.mcp.json` w root projektu - wymaga czasem restartu terminala
- `CLAUDE.md` - Claude czyta zagnieżdżone pliki
  - Odnośnik do `@AGENTS.md` i podać niżej co tylko Claude Code dotyczy
- **LangGraph Studio**:
  - jak działa?
  - Online czy offline?
  - Jak update kodu robi?
- Context7 - skill vs MCP
- Linux sandbox: o wszystko pyta?
  - Nie ma auto allow?

## Dokumentacja projektowa

- PRD i ADR gotowe stworzyć
- Dopracować tylko wzór
- Skill do robienia ADR i PRD (na podstawie naszej sesji)

## Sterowanie zespołowe

- Team subscription pozwala na sterowanie ustawieniami dla zespołu
- Wspólne rules i MD files dla wielu projektów
  - Claude Code pozwala ustawić główny CLAUDE.md dla wszystkich pracowników
  - Ale dla dzielenia Rules i zagnieżdżonych CLAUDE.md polecają wspólne repo i np. robienie symlinków do projektu z innego folderu wspólnego

## Zarządzanie taskami

- Zdalne taski z `/schedule`, aby **curation CLAUDE.md** był robiony automatem dla wielu developerów, np. w Jenkins flow
- Schedule -> 1/week sprzątanie plików `CLAUDE.md` i skills
- Hooks i Automatyzacje ze skryptami

## Edit plan

- Edit plan
  - files are saved in user's `~/.claude/plans`
  - use `Ctrl + G` after plan was created to edit it manually


## Subagenci: Claude vs Codex
- Czy mogą używać wspólnych definicji subagentów jakoś?

## Git Worktrees i bash

- Flaga `codex --worktree` or `-w` with optional worktree name
- Does `bash(* git add:*)` work to allow anything before `git`?
  - agent likes to enter folder with `cd` before using `git add`)

---

## Praca na wielu repo

- How to work on multiple separate repos, not monorepo
- `/add-dir`

## Hooks i konfiguracja

- Hooks do skills
- `/hooks`

---

## Zbiorcze wnioski z notatek

### Najczęstsze tematy

- Hallucynacje i sposoby weryfikacji odpowiedzi agentów
- Gubienie kontekstu przy większych projektach
- Sens używania subagentów i worktree
- Jak dobrze pisać skills, rules i pliki typu `CLAUDE.md`
- Testy: integracyjne, TDD, happy path, edge case, asercje
- Różnice między Claude Code, Codex, Cursor, Gemini i innymi narzędziami
- Sandbox, approvals, MCP, konfiguracja lokalna vs globalna

### Pytania, które warto doprecyzować na kolejnych zajęciach

- Kiedy używać subagentów, a kiedy nie
- Jak ograniczać hallucynacje w praktyce
- Jak organizować pamięć projektu i utrzymywać kontekst
- Jak dzielić pracę agentów na osobne worktree i PR-y
- Jak układać ADR/PRD i czy warto to wspierać skillami
- Co powinno być globalne, a co projektowe: MCP, hooks, config, rules
