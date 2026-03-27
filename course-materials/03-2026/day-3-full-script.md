# Dzień 3 — Pełny scenariusz prowadzenia
**Temat: Kontekst, Sub-Agenci i Pierwsze Linie Kodu**
**Godz. 9:00–16:00 | Szkolenie stacjonarne / online | Uczestnicy: Java devs, seniorzy, tech leads, architekci**

> 🎬 = co mówię (dosłownie lub prawie dosłownie)
> 📺 = co pokazuję na ekranie
> 💬 = wklejam na chat (gotowy tekst do skopiowania)
> 🏋️ = ćwiczenie dla uczestników
> ⏱️ = czas bloku
> 💡 = wskazówka / uwaga dla prowadzącego (nie mów tego głośno)
> 🔵 = zadanie dodatkowe dla zaawansowanych (opcjonalne, równolegle)

---

## AGENDA DNIA (wyślij na starcie)

💬 WKLEJ NA CHAT:
```
Dzień 3 – agenda:

=== RANO: DOMKNIĘCIE + CLAUDE.md 2.0 ===
09:00  Reset + agenda
09:10  CLAUDE.md 2.0 — nowe funkcje ogłoszone 2 dni temu!
09:55  Skills + Sub-Agenci — konfiguracja
10:40  PRD/ADR weryfikacja + Task Plan Matrix
11:00  ☕ PRZERWA

=== DZIEŃ 3: FULL-STACK Z AI ===
11:15  Moduł 3.1 — AI UX Researcher: Playwright + design tokens + Sinsay.com
12:30  Moduł 3.2 — Multi-Agent TDD: config agentów, implementacja
13:00  🍽️ PRZERWA OBIAD
13:30  Moduł 3.2 cd — TDD workflow + Git Worktrees + parallel agents
14:30  ☕ OPCJONALNA PRZERWA
14:45  Moduł 3.3 — Debugging & Refactoring z agentem
15:30  Git Worktrees demo + podsumowanie
15:55  Podsumowanie dnia
16:00  Koniec
```

---

## 09:00–09:10 — Reset + agenda
⏱️ 10 min

🎬 **CO MÓWIĘ:**

„Dzień dobry! Kciuki jeśli mnie słychać.

Mam dla was dziś wyjątkowo dobrą wiadomość — i nie zmyślam, dosłownie 2 dni temu Anthropic ogłosił nowe funkcje CLAUDE.md, które zmieniają to, jak konfigurujemy agentów. Szczegóły za chwilę — to będzie pierwsza rzecz dziś rano.

Ale najpierw szybki status: gdzie jesteśmy, co mamy, czego jeszcze brakuje. Dajcie na chat:
- 👍 jeśli macie gotowe PRD i ADR
- ✍️ jeśli jeszcze nie
- 🐛 jeśli coś się wczoraj posypało

...bo od tego zależy, co robimy przez pierwsze 30 minut."

💡 **Uwaga:** Zbierz status i wróć do PRD/ADR przed modułem 3.2. Jeśli większość nie ma — zarezerwuj czas po pierwszej przerwie.

---

## 09:10–09:55 — CLAUDE.md 2.0 — nowe funkcje
⏱️ 45 min

🎬 **CO MÓWIĘ:**

„OK, zacznijmy od gwiazdy dzisiejszego poranka. Anthropic właśnie — dosłownie 2 dni temu — ogłosił trzy nowe funkcje w CLAUDE.md. Jeśli znacie Cursor, jedna z nich będzie wam bardzo znajoma.

Zanim pokażę, szybkie przypomnienie dlaczego CLAUDE.md w ogóle ma znaczenie. Kto pamięta z dnia 1 i 2?"

💡 Poczekaj na odpowiedzi. Hasła: 'kontekst agenta', 'instrukcje projektu', 'co agent widzi na start'.

🎬 **CO MÓWIĘ (kontynuacja):**

„Dokładnie. CLAUDE.md to nie plik konfiguracyjny — to mózg agenta. To co tam wpiszemy, agent widzi zawsze, na starcie każdej sesji. Dlatego jakość tego pliku bezpośrednio wpływa na jakość kodu, który dostajemy.

I właśnie dlatego te trzy nowe funkcje są takie ważne. Lecę po kolei."

---

### Nowość #1: Limit 200 linii

📺 **CO POKAZUJĘ:** Otwieramy `CLAUDE.md` projektu w edytorze. Liczymy linie.

🎬 **CO MÓWIĘ:**

„Pierwsza zmiana: CLAUDE.md ma teraz twardy limit 200 linii. Linie powyżej 200 są po prostu obcinane — agent ich nie widzi.

Zanim powiecie 'ale mój CLAUDE.md ma 350 linii' — tak, widzę minę. I tak, to jest problem, który wiele projektów ma dziś, nie wiedząc o tym.

Dlaczego limit? Bo zbyt długi kontekst = szum. Badania pokazują, że model traci uwagę na środkowych fragmentach długiego kontekstu. Lepiej mieć 150 linii precyzyjnych instrukcji niż 400 linii bałaganu.

Rozwiązanie? Nowość numer dwa."

💬 WKLEJ NA CHAT:
```
CLAUDE.md — limit 200 linii (nowe!)
Linie 201+ są obcinane. Agent ich nie widzi.

Docs: https://code.claude.com/docs/en/memory#how-claude-md-files-load
```

---

### Nowość #2: @AGENTS.md — referencja zamiast duplikacji

🎬 **CO MÓWIĘ:**

„Zamiast wklejać wszystko do CLAUDE.md — możecie teraz napisać w CLAUDE.md tylko:

```
@AGENTS.md
```

I agent automatycznie załaduje ten plik. To jest odpowiednik `import` — agent widzi obydwa pliki, ale CLAUDE.md zostaje krótki i skupiony na tym, co naprawdę ważne.

Schemat który polecam:

CLAUDE.md → tylko rzeczy specificzne dla Claude Code
@AGENTS.md → szczegóły projektu, konwencje, workflow, wszystko inne przydatne dla Claude i innych agentów (np. Codex)
.claude/rules/ → zasady specyficzne dla konkretnych folderów (ale działają tylko dla Claude...)

Czyli CLAUDE.md to 'cover page' — pierwsza strona, która mówi agentowi o co chodzi. Reszta w osobnych plikach."

💬 WKLEJ NA CHAT:
```
CLAUDE.md 2.0 — struktura (rekomendowana)

CLAUDE.md (<80 linii):
  - tylko to co specifyczne dla Claude Code
  - @AGENTS.md  ← auto-include!

AGENTS.md:
  - szczegóły projektu
  - konwencje kodu
  - workflow
  - to co dotyczy Claude i innych agentów (np. Codex)

.claude/rules/backend.md:
  - zasady tylko dla backend/** i np. tylko dla plików java albo sql

.claude/rules/frontend.md:
  - zasady tylko dla frontend/** i np. tylko dla plików .tsx

Docs: https://code.claude.com/docs/en/memory#agents-md
https://code.claude.com/docs/en/memory#path-specific-rules
```

---

### Nowość #3: Path-based rules (jak Cursor Rules!)

📺 **CO POKAZUJĘ:** Tworzę folder `.claude/rules/` w projekcie, następnie tworzę plik `backend.md`.

🎬 **CO MÓWIĘ:**

„I teraz najlepsza nowość — ta, na którą czekali użytkownicy Cursora. Path-based rules.

Możemy teraz tworzyć pliki zasad w folderze `.claude/rules/` i każdy plik może mieć frontmatter z glob patternem. Dokładnie jak Cursor Rules, jeśli znacie.

Przykład:"

📺 **CO POKAZUJĘ:** Tworzę `.claude/rules/backend.md`:

```markdown
---
paths:
  - "backend/**/*.java"
  - "backend/pom.xml"
---

# Backend Rules

- Używaj Java 21 + Spring Boot 3.5
- Pakiety: com.sinsay.*
- Zawsze dodawaj testy JUnit 5 obok klasy
- Streaming: SSE musi emitować Vercel Data Stream format (0:"text")
- Nigdy nie commituj kluczy API
```

🎬 **CO MÓWIĘ:**

„Co to oznacza? Gdy agent pracuje nad plikiem `backend/src/main/java/...`, automatycznie dostaje zasady z `backend.md`. Gdy pracuje nad `frontend/src/...` — dostaje zasady z `frontend.md`. Agent nie musi czytać wszystkiego naraz.

To jest ogromna zmiana jeśli macie monorepo albo projekt z kilkoma stackami. Zamiast jednego długiego CLAUDE.md — precyzyjne zasady dla każdego obszaru."

💬 WKLEJ NA CHAT:
```
.claude/rules/ — path-based rules (nowe! 2 dni temu)

Składnia — frontmatter z path patternem:

---
paths:
  - "backend/**/*.java"
  - "backend/pom.xml"
---
# Backend Rules
...

Gdy agent edytuje plik pasujący do paths → reguły są automatycznie ładowane.
Jeśli nie ma paths to plik jest ZAWSZE ładowany do kontekstu.
Działa jak Cursor Rules.

Docs: https://code.claude.com/docs/en/memory#path-specific-rules
```

---

### 🏋️ Ćwiczenie 3.0 — CLAUDE.md Refactor
⏱️ 12 min

🎬 **CO MÓWIĘ:**

„OK, macie 12 minut. Chcę żebyście zrobili refactor waszego CLAUDE.md pod nowe zasady. Zadanie:"

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.0 — CLAUDE.md Refactor (12 min)

1. Sprawdź ile linii ma twój CLAUDE.md:
   wc -l CLAUDE.md

2. Jeśli więcej niż 200 — usuń zbędne sekcje lub przenieś do AGENTS.md

3. Dodaj @AGENTS.md do CLAUDE.md jeśli nie ma

4. Stwórz .claude/rules/backend.md z podstawowymi zasadami Java:
---
paths:
  - "backend/**/*.java"
  - "backend/pom.xml"
---
# Backend Java Rules
- Java 21, Spring Boot 3.5
- 4-space indentation
- Packages: com.sinsay.*
- Tests: *Tests suffix, JUnit 5
- Never commit API keys

5. 🔵 Zaawansowani:
- dodaj też .claude/rules/frontend.md dla TypeScript/React
```

💡 **Uwaga:** Chodź po uczestnikach, sprawdzaj linie CLAUDE.md. Większość będzie miała za długie pliki — to dobry moment na konkretną korektę.

🎬 **CO MÓWIĘ (po ćwiczeniu):**

„Dobra — kto miał ponad 200 linii? Podnieście rękę / dajcie na chat.

...OK. To jest rzecz, którą wiele projektów ma jako ukryty problem. Od dziś wiecie. Krótko, precyzyjnie, z referencją do AGENTS.md."

---

## 09:55–10:40 — Skills + Sub-Agenci — konfiguracja
⏱️ 45 min

🎬 **CO MÓWIĘ:**

„Teraz Skills i Sub-Agenci — domknięcie z wczoraj.

Przypomnę szybko: Skills to pliki SKILL.md, które agent dostaje jako instrukcje dla konkretnego zadania. Sub-agenci to wyspecjalizowane instancje agenta, które nasz główny agent może delegować zadania.

Analogia z managementu: jesteś tech leadem. Nie kodujesz wszystkiego sam. Masz backend deva, frontend deva, QA. Każdy dostaje brief — i robi swoją robotę. Tak samo tutaj."

### Skills — setup

📺 **CO POKAZUJĘ:** Folder `.claude/skills/` w projekcie.

🎬 **CO MÓWIĘ:**

„Skills możemy tworzyć ręcznie albo pobrać gotowe. Jest projekt `skills.sh` — community-driven zbiór Skills dla popularnych stacków. Sprawdźmy co tam jest."

💬 WKLEJ NA CHAT:
```
Skills — gotowe szablony (community):
https://github.com/anthropics/claude-code-skills

Pobierz skills dla naszego projektu:
curl -fsSL https://skills.sh | bash
# lub manualnie z GitHub

Przydatne dla nas:
- java-spring-boot
- java-testing
- react-typescript
- git-workflow
```

💡 Jeśli skills.sh nie działa w chwili szkolenia, pokaż manualnie jak wygląda plik SKILL.md i stwórzcie go razem.

📺 **CO POKAZUJĘ:** Przykładowy SKILL.md dla java-testing.

🎬 **CO MÓWIĘ:**

„Skill wygląda tak — to po prostu plik Markdown z instrukcjami. Agent dostaje go jako dodatkowy kontekst gdy wywołamy go w sesji przez `/skill java-testing` albo gdy agent sam zdecyduje że jest potrzebny.

Kluczowe: Skill nie jest regułą globalną — jest kontekstem na żądanie. To jak 'cheat sheet' dla agenta do konkretnego zadania."

---

### Sub-Agenci: teoria a praktyka

🎬 **CO MÓWIĘ:**

„Sub-agenci. Tutaj muszę być szczery co do terminologii, bo Anthropic ma dwa pojęcia i łatwo je pomylić.

Mamy: Sub-agents i Agent Teams.

Sub-agents — to jest stabilna, dostępna funkcja. Nasz główny agent (main agent) może używać narzędzia `Agent` żeby zdelegować zadanie do wyspecjalizowanego agenta. Ten agent dostaje własny kontekst, własne narzędzia, własne instrukcje — i raportuje wyniki do głównego agenta.

Agent Teams — to jest funkcja eksperymentalna, wymaga zmiennej środowiskowej żeby ją włączyć, i jest bardziej 'peer-to-peer' — agenci koordynują między sobą bez centralnego koordynatora. To temat na osobną sesję, dzisiaj skupiamy się na Sub-agents."

💬 WKLEJ NA CHAT:
```
Sub-agents vs Agent Teams:

Sub-agents (STABILNE):
- Główny agent deleguje do specjalistów
- Można planować pracę z góry razem z matrycą zależności zadań i subagentów
- Osobny kontekst dla subagentów = ograniczenie context root dla koordynatora i subagentów = lepsze attention
- Hierarchia: koordynator → specjalista
- Dostępne teraz, bez konfiguracji

Agent Teams (EKSPERYMENTALNE):
- Agenci peer-to-peer
- Wymaga env var: CLAUDE_AGENT_TEAMS=1
- Równoległe, bez centralnego koordynatora
- Team members rozmawiają ze sobą, planują i koordynują swoją pracę
- Wspólne Tasks z synchronizacją i informacją co kto robi
- Docs: https://code.claude.com/docs/en/agent-teams

Więcej: https://code.claude.com/docs/en/features-overview#subagent-vs-agent-team
```

---

### Konfiguracja Sub-Agentów dla Sinsay

📺 **CO POKAZUJĘ:** Tworzę/edytuję CLAUDE.md projektu z sekcją sub-agentów.

🎬 **CO MÓWIĘ:**

„Jak konfigurujemy sub-agentów dla naszego projektu? W AGENTS.md dodajemy sekcję która opisuje specjalistów. Agent czyta to i wie kogo może wołać i z jakim briefe'm.

Nasi specjaliści:"

💬 WKLEJ NA CHAT:
```
## Sub-Agent Configs (wklej do AGENTS.md)

### BE Developer (Java)
- Focus: backend/src/main/java/
- Zakres opisu sub-agenta:
  - Nie powielaj inforamcji z CLAUDE.md, Rules i Skills!!!
  - Skup się główni na flow pracy danego subagenta, zasadach jego pracy, metodyce
- Nie podawaj np.
  - stack (podany w plikach projektu, np. /backend/CLAUDE.md ): Spring Boot 3.5, Spring AI, Java 21, SQLite/JPA
  - komend/skryptów i opisu workflow podanego w ./CLAUDE.md z root projektu
  - Szczegółowych workflow ze skills (można podać agentowi jakich skills ma używać)

### FE Developer (React)
- Focus: frontend/src/

### QA Engineer
- Focus: całe repozytorium
- Odpowiedzialność: testy integracyjne, edge cases, code review
- Sprawdzaj: bezpieczeństwo (XSS, injection), pokrycie testami
- Raportuj: lista bugów + sugestie poprawek
```

---

### 🏋️ Ćwiczenie 3.1 — Sub-Agent Config
⏱️ 10 min

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.1 — Sub-Agent Config (10 min)

1. Otwórz AGENTS.md projektu
2. Dodaj sekcję "## Sub-Agent Configs" z 3 agentami (BE, FE, QA)
   (użyj tekstu z chatu jako bazy, dostosuj do waszego projektu)
3. Commit: "Docs: add sub-agent configs to AGENTS.md"

4. Następnie zapytaj agenta:
   "Masz teraz sub-agent configs w AGENTS.md.
    Opisz w kilku zdaniach jak byś użył BE Developer agenta
    do implementacji ChatController. Co by dostał jako brief?"

🔵 Zaawansowani:
   Stwórz .claude/rules/e2e-tests.md z zasadami specyficznymi
   dla pisania testów E2E w Playwright
```

---

## 10:40–11:00 — PRD/ADR weryfikacja + Task Plan Matrix
⏱️ 20 min

🎬 **CO MÓWIĘ:**

„Zanim zaczniemy pisać kod — checkpoint. Potrzebujemy żeby wszyscy mieli PRD i ADR. Bez tego agenci będą generować kod w ciemno.

Szybka weryfikacja — dajcie na chat:
- 👍 mam PRD i ADR w repo
- ✍️ mam tylko PRD
- 🐛 nie mam żadnego

...liczę..."

💡 Jeśli ktoś nie ma PRD/ADR — daj mu prompt poniżej i powiedz żeby to był priorytet podczas przerwy lub wróćcie do tego po przerwie jako ćwiczenie.

💬 WKLEJ NA CHAT:
```
Szybki PRD dla Sinsay (jeśli nie masz) — wklej do claude:

"Stwórz PRD dla projektu Sinsay Complaint & Returns Chatbot.

Projekt: chatbot do obsługi reklamacji i zwrotów dla sklepu Sinsay Fashion.
Flow:
1. Formularz: numer zamówienia, data zakupu, typ (reklamacja/zwrot), opis, zdjęcie
2. AI analizuje zdjęcie + czyta procedury z MD files
3. Wydaje werdykt w języku polskim
4. Klient może kontynuować rozmowę na chacie

Stack: Java 21 + Spring Boot + Spring AI + OpenAI gpt-4o (vision)
Frontend: React 19 + Vercel AI SDK + assistant-ui
Baza: SQLite

Zapisz jako docs/PRD-Sinsay-PoC.md"
```

### Task Plan Matrix

🎬 **CO MÓWIĘ:**

„Mając PRD i ADR — możemy teraz zrobić Task Plan Matrix. To jest tabela która odpowiada na pytanie: kto co robi, w jakiej kolejności, co zależy od czego.

Bez tego multi-agent system to chaos. Z tym — to pipeline."

💬 WKLEJ NA CHAT:
```
Task Plan Matrix — prompt (wklej do claude):

"Na podstawie ADR i PRD stwórz Task Plan Matrix dla implementacji.

Format tabeli:
| Task | Agent | Depends On | Parallel? | Estymacja |

Uwzględnij:
- Backend: model entities, ChatController, VerificationService, SSE adapter
- Frontend: Form component, Chat component, useChat integration
- Testy: unit, integration, SSE format test
- Config: CORS, application.properties, MCP setup

Zaznacz które tasks mogą iść równolegle (Git Worktrees).
Zapisz jako docs/task-plan-matrix.md"
```

---

## 11:00–11:15 — ☕ PRZERWA
⏱️ 15 min

💡 Podczas przerwy: sprawdź kto nie ma PRD/ADR — pomóż im go wygenerować zanim wrócą.

---

## 11:15–12:30 — Moduł 3.1: AI UX Researcher
⏱️ 75 min

### Playwright MCP vs Playwright Skill

🎬 **CO MÓWIĘ:**

„Moduł 3.1 — AI jako UX Researcher. Zanim napiszemy linię kodu frontendu — chcemy wiedzieć jak wygląda Sinsay. Design tokeny, kolory, fonty, logo. Żeby nasz chatbot wyglądał jak część sklepu, nie jak losowy projekt z Dribbble.

Ale najpierw — ważne rozróżnienie. Mamy dwa pojęcia z 'Playwright' w nazwie i są kompletnie inne rzeczy.

Playwright MCP — to serwer MCP, który daje agentowi dostęp do prawdziwej przeglądarki. Agent może nawigować, klikać, czytać DOM, robić screenshoty. To narzędzie do automatyzacji i analizy.

Playwright Skill — to SKILL.md który uczy agenta jak pisać testy Playwright. Agent nie odpala przeglądarki — agent pisze kod testów.

Pytanie do was: do czego dzisiaj potrzebujemy Playwright?"

💡 Poczekaj na odpowiedzi. Poprawna: MCP — do analizy Sinsay.com, Skill — do pisania testów E2E.

💬 WKLEJ NA CHAT:
```
Playwright MCP vs Playwright Skill:

Playwright MCP:
- Persistent browser session (real Chrome)
- Agent może: nawigować, klikać, screenshoty, czytać DOM
- Użycie: analiza stron, automatyzacja, scraping, prostsze zadania (np. otwórz czat i zrób screen)

Playwright Skill (SKILL.md):
- Uczy agenta jak pisać skrypt wykonujący sekwencję działań w Playwright
- Agent generuje kod i potem go odpala, 2 kroki często, mniej manualnej kontroli, mniej interaktywne
- Użycie: pisanie E2E testów, test automation, sprawdzanie co zrobił i kontrola jakości

Dziś: MCP → analiza Sinsay.com
Jutro: Skill → testy E2E dla naszej aplikacji
```

---

### Setup Playwright MCP

📺 **CO POKAZUJĘ:** Dodaję Playwright MCP do konfiguracji Claude Code.

🎬 **CO MÓWIĘ:**

„Jeśli nie macie jeszcze Playwright MCP — szybka instalacja. Potem pokażę jak go używamy."

💬 WKLEJ NA CHAT:
```
Playwright MCP — instalacja:

1. Sprawdź czy jest już w konfiguracji:
   cat ~/.claude.json | grep playwright
   (lub Claude Desktop → Settings → Developer → MCP Servers)

2. Jeśli nie ma, dodaj:
   claude mcp add playwright npx @playwright/mcp@latest

3. Weryfikacja:
   claude mcp list

Dokumentacja: https://github.com/microsoft/playwright-mcp
```

💡 Jeśli ktoś ma problem z instalacją MCP — mogą pójść ścieżką alternatywną: zapytać agenta o analizę Sinsay na podstawie publicznych informacji (bez browser MCP). Wyniki będą gorsze, ale ćwiczenie ma wartość edukacyjną.

---

### 🏋️ Ćwiczenie 3.2 — Analiza Sinsay.com z Playwright MCP
⏱️ 20 min

🎬 **CO MÓWIĘ:**

„Teraz ćwiczenie. Chcę żebyście kazali agentowi przeanalizować Sinsay.com i wyciągnąć design tokeny. Prompt do wklejenia:"

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.2 — Analiza designu Sinsay.com (20 min)

Wklej do claude:

---

Używając Playwright MCP, przeanalizuj stronę https://www.sinsay.com/pl/pl i:

1. Zrób screenshot strony głównej i zapisz jako assets/sinsay-homepage.png
2. Wyciągnij z CSS/DOM design tokeny:
   - Kolory (primary, secondary, background, text, accent)
   - Fonty (family, size, weight)
   - Spacing (padding, margin values używane najczęściej)
   - Border radius
   - itp.
3. Pobierz favicon i zapisz jako assets/sinsay-favicon.ico
4. Sprawdź czy logo jest dostępne jako SVG lub PNG i zapisz je jako assets/logo.png (lub svg)
5. Zapisz znalezione tokeny oraz szczegółowy opis design system jako /docs/design-guidelines.md który ma posłużyć agentom do dokładnego odzwierciedlenia wyglądu strony Sinsay w naszej aplikacji aby była ona spójna - powinien on zawierać odnośniki do wszystkich zapisanych plików w assets.
6. same design tokens zapisz w pliku assets/design-tokens.json

Cel: będziemy używać tych tokenów i opisu design guidelines tworząc naszą aplikację aby była spójna z brendem Sinsay.

Commit: "Docs: add sinsay design tokens and assets"
```

💡 Obserwuj postęp. Agent może mieć problem ze stroną jeśli jest zabezpieczona (bot detection). W tym przypadku — poproś uczestników żeby zrobili screenshoty ręcznie i wkleili agentowi jako obrazki do analizy (multimodal!).

🔵 **Zadanie dla zaawansowanych:**
```
🔵 Zaawansowani:
Po analizie Sinsay — użyj PromptCowboy (promptcowboy.ai)
żeby ulepszyć prompt do generowania wireframów.
Następnie zapytaj agenta o wygenerowanie wireframe dla
formularza reklamacji w formacie ASCII lub Mermaid diagram.
```

---

### Design Tokens → projekt

📺 **CO POKAZUJĘ:** Otwieram `assets/design-tokens.json` wygenerowany przez agenta.

🎬 **CO MÓWIĘ:**

„Zobaczmy co agent znalazł. Patrzcie na kolory — Sinsay ma specyficzną paletę. Będziemy to podawać agentowi jako kontekst do generowania komponentów.

Teraz coś ważnego: te tokeny wklejamy do `.claude/rules/frontend.md`. Dlaczego? Bo każdy komponent który agent wygeneruje dla frontendu, automatycznie dostanie te kolory i fonty jako wytyczne. Zero 'wymyśl sobie design' — agent wie jak to ma wyglądać."

📺 **CO POKAZUJĘ:** Dodaję tokeny do `.claude/rules/frontend.md`:

```markdown
---
globs: ["frontend/src/**/*.tsx", "frontend/src/**/*.ts"]
---

# Frontend Rules

## Design System (Sinsay)
Kolory:
- primary: #[kolor z analizy]
- background: #[kolor]
- text: #[kolor]

Fonty: [font z analizy]

## Komponenty
- Używaj Shadcn/ui jako bazy
- Dodawaj data-testid do interaktywnych elementów
- Walidacja formularzy: Zod
- Typy: TypeScript strict mode, no any
```

---

### Wireframe + PromptCowboy

🎬 **CO MÓWIĘ:**

„Mała dygresja — PromptCowboy. Kto słyszał?

To jest narzędzie które bierze wasz prompt i go ulepsza zanim wyślecie do agenta. Analogia: zamiast wysyłać maila w 5 sekund, dajesz go do copy-editora który wyłapuje niejasności i dodaje kontekst.

Adres: promptcowboy.ai

Demo:"

📺 **CO POKAZUJĘ:** Wchodzę na promptcowboy.ai i wklejam prompt:

```
Stwórz komponent formularza reklamacji dla Sinsay Fashion
z polami: numer zamówienia, data zakupu, typ (reklamacja/zwrot), opis, upload zdjęcia
```

📺 Pokazuję ulepszony prompt. Omawiam różnice.

🎬 **CO MÓWIĘ:**

„Widzicie różnicę? Prompt przed: 2 zdania, brak kontekstu. Prompt po: specifikacja z typami, walidacją, stylem, edge case'ami. Agent dostaje to i od razu wie co robić — bez 3 rund 'a co chciałeś przez X'.

Nie mówię że zawsze trzeba używać PromptCowboy — ale warto go znać, szczególnie do złożonych promptów przed startem implementacji."

---

## 12:30–13:00 — Moduł 3.2 intro: Task Plan Matrix + Agent Configs
⏱️ 30 min

🎬 **CO MÓWIĘ:**

„Mamy design, mamy PRD, mamy ADR. Czas na implementację.

Zanim puścimy agentów — pokażę jak myśleć o task plan matrix w kontekście multi-agent workflow. Bo to nie jest random 'zrób mi backend' — to jest świadome zarządzanie zależnościami między zadaniami."

📺 **CO POKAZUJĘ:** Otwieram `docs/task-plan-matrix.md` wygenerowany wcześniej. Przeglądamy razem.

🎬 **CO MÓWIĘ:**

„Patrzcie na kolumnę 'Parallel?'. Niektóre taski możemy puścić równolegle — BE i FE mogą iść jednocześnie jeśli mają ustalone kontrakty API. Inne muszą iść sekwencyjnie — FE komponent zależy od działającego endpointu.

Za 2 godziny pokażę jak to realizować za pomocą Git Worktrees — różne gałęzie, różne katalogi, różni agenci. Ale najpierw — zacznijmy implementację."

### TDD Workflow — zasady

🎬 **CO MÓWIĘ:**

„TDD z agentem. Ktoś tutaj praktykuje TDD? Ręce w górę.

OK — w kontekście agentów TDD ma dodatkową wartość. Nie tylko 'piszemy lepszy kod'. Test jest SPECYFIKACJĄ dla agenta. Zamiast opisywać słowami 'endpoint ma zwracać SSE z Vercel format' — piszemy test który to sprawdza. Agent implementuje tak długo aż test jest zielony.

Zasady TDD w naszym projekcie:"

💬 WKLEJ NA CHAT:
```
TDD z agentem — workflow:

1. RED: Napisz failing test (opisuje pożądane zachowanie)
2. COMMIT: git commit -m "Test: add failing test for [feature]"
3. GREEN: Implementuj do momentu gdy test przechodzi
4. COMMIT: git commit -m "Feature: implement [feature]"
5. REFACTOR: Uprość kod jeśli potrzeba
6. COMMIT: git commit -m "Refactor: simplify [feature]"

Granularne commity = łatwy rollback jeśli agent coś popsuje.

Selektor testów — co WOLNO i czego NIE WOLNO:
✅ aria labels:     getByRole('button', { name: 'Wyślij' })
✅ visible text:    getByText('Numer zamówienia')
✅ data-testid:     getByTestId('submit-button')
❌ CSS classes:    .btn-primary (niestabilne, zmienia się)
❌ DOM structure:  div > form > button:first-child (kruche)
```

---

### 🏋️ Ćwiczenie 3.3 — Pierwszy Test (TDD Red)
⏱️ 15 min

🎬 **CO MÓWIĘ:**

„Zanim przerwa obiadowa — chcę żebyście napisali pierwszy failing test. Nie implementację — tylko test. Agent napisze test, my zatwierdzimy, commitujemy i po obiedzie implementujemy.

Wybierzcie jeden element:"

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.3 — Pierwszy Test, TDD Red (15 min)

Wybierz zadanie i wklej prompt do claude:

--- OPCJA A: Backend (Java) ---
"Napisz test JUnit 5 dla ChatController który sprawdza:
- POST /api/chat zwraca status 200
- Content-Type to text/event-stream
- Odpowiedź zawiera przynajmniej jeden chunk w formacie Vercel Data Stream: 0:\"text\"

Użyj MockMvc i @SpringBootTest. Test ma teraz FAILOWAĆ (klasa nie istnieje).
Zapisz jako backend/src/test/java/com/sinsay/controller/ChatControllerTests.java
Następnie zrób commit: 'Test: add failing ChatController SSE format test'"

--- OPCJA B: Frontend (React) ---
"Napisz test Vitest + Testing Library dla komponentu ClaimsForm który sprawdza:
- Formularz renderuje pole 'Numer zamówienia' (getByLabelText)
- Formularz renderuje przycisk 'Wyślij reklamację' (getByRole)
- Po wysłaniu pustego formularza pokazuje błąd walidacji (getByText)

Test ma teraz FAILOWAĆ (komponent nie istnieje).
Zapisz jako frontend/src/components/__tests__/ClaimsForm.test.tsx
Następnie zrób commit: 'Test: add failing ClaimsForm validation test'"
```

💡 Pilnuj: test ma być czerwony na końcu ćwiczenia. Jeśli ktoś od razu pisze implementację — zatrzymaj ich. Red first.

---

## 13:00–13:30 — 🍽️ PRZERWA OBIAD
⏱️ 30 min

💡 Podczas przerwy: sprawdź czy wszyscy mają czerwone testy w repo. Jeśli ktoś ma problemy ze strukturą projektu — daj mu prompt do kickoffu projektu (patrz Appendix B).

---

## 13:30–14:30 — Moduł 3.2 cd: TDD Green + Git Worktrees
⏱️ 60 min

### TDD Green — implementacja

🎬 **CO MÓWIĘ:**

„Witam po obiedzie. Mamy czerwone testy. Teraz czas na zielone.

Zasada: agentowi dajemy jedno zadanie — sprawić żeby ten test przeszedł. Nic więcej. Nie prosimy o całą architekturę — prosimy o minimum kodu które sprawi że test jest zielony."

💬 WKLEJ NA CHAT:
```
TDD Green — prompt (dostosuj do swojego testu):

--- BACKEND ---
"Masz failing test w ChatControllerTests.java.
Zaimplementuj ChatController tak żeby ten test przechodził.

Wymagania:
- POST /api/chat → text/event-stream
- Vercel Data Stream format: 0:"escaped_text"\n
- Na razie możesz zwracać hardcoded response — focus na SSE format

Po implementacji sprawdź: ./mvnw test
Jeśli zielony: git commit -m 'Feature: add ChatController with SSE format'"

--- FRONTEND ---
"Masz failing test w ClaimsForm.test.tsx.
Zaimplementuj komponent ClaimsForm tak żeby test przechodził.

Wymagania:
- Pole 'Numer zamówienia' z label
- Przycisk 'Wyślij reklamację'
- Walidacja: pole wymagane, błąd gdy puste
- Użyj Shadcn Input + Button + Form + Zod

Po implementacji sprawdź: npm test && npm run lint
Jeśli zielony: git commit -m 'Feature: add ClaimsForm with validation'"
```

📺 **CO POKAZUJĘ:** Demon live — wklejam prompt, obserwuję jak agent implementuje, sprawdzam test.

🎬 **CO MÓWIĘ:**

„Patrzcie na co agent robi: nie wymyśla architektury, nie dodaje feature'ów których nie prosiłem — skupia się na tym żeby test przeszedł. To jest właśnie wartość TDD z agentem: test jest kontraktem, agent go spełnia."

---

### Git Worktrees — parallel agents

🎬 **CO MÓWIĘ:**

„Teraz Git Worktrees. To jest jeden z tych tematów, który zmienia jak myślicie o pracy równoległej.

Problem: chcę żeby agent pracował nad BE i FE jednocześnie. Ale agent nie może pracować w tym samym katalogu bo będą konflikty. Rozwiązanie: dwie kopie repozytorium w różnych gałęziach, każda w osobnym katalogu."

📺 **CO POKAZUJĘ:** Tworzę worktree w terminalu.

```bash
# Jesteśmy na branchu: main (lub march-2026)
# Tworzymy worktree dla BE feature
git worktree add ../sinsay-be feature/backend-chat-controller

# Tworzymy worktree dla FE feature
git worktree add ../sinsay-fe feature/frontend-claims-form

# Sprawdzamy
git worktree list
```

🎬 **CO MÓWIĘ:**

„Co właśnie zrobiliśmy? Mamy teraz trzy katalogi:
- `sinsay-poc/` — główne repo, branch main
- `sinsay-be/` — osobna kopia, branch feature/backend-chat-controller
- `sinsay-fe/` — osobna kopia, branch feature/frontend-claims-form

Możemy teraz otworzyć dwa terminale, w każdym uruchomić Claude Code — i mamy dwa niezależne agenty pracujące równolegle. Każdy ze swoim kontekstem, swoimi plikami, swoją historią."

💬 WKLEJ NA CHAT:
```
Git Worktrees — komendy:

# Utwórz worktree
git worktree add ../nazwa-katalogu nazwa-brancha

# Lista worktree
git worktree list

# Usuń worktree (po merge)
git worktree remove ../nazwa-katalogu

# W worktree możesz uruchomić osobną instancję claude:
cd ../sinsay-be && claude

Scenariusz multi-agent:
Terminal 1: cd ../sinsay-be && claude
  → "Zaimplementuj ChatController + testy"

Terminal 2: cd ../sinsay-fe && claude
  → "Zaimplementuj ClaimsForm + testy"

Potem: PR → merge → usuń worktree
```

---

### 🏋️ Ćwiczenie 3.4 — Git Worktree Setup
⏱️ 10 min

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.4 — Git Worktrees (10 min)

1. Utwórz worktree dla backend feature:
   git worktree add ../sinsay-backend feature/backend-chat-controller

2. Sprawdź że istnieje:
   git worktree list

3. Wejdź do worktree i sprawdź branch:
   cd ../sinsay-backend
   git branch

4. Wróć do głównego katalogu:
   cd ../sinsay-poc  (lub jak się nazywa twój folder)

5. Daj 👍 na chat gdy gotowe

🔵 Zaawansowani:
   Otwórz dwa terminale:
   - Terminal 1: cd ../sinsay-backend && claude
   - Terminal 2: cd ../główne-repo && claude
   Puść oba agenty z różnymi zadaniami i obserwuj pracę równoległą.
```

---

### Kontekst i jego koszty

🎬 **CO MÓWIĘ:**

„Zatrzymajmy się na chwilę na temat który jest mniej widoczny ale kosztowny — dosłownie.

Każdy agent ma okno kontekstu. Im więcej tokenów w kontekście — tym drożej i wolniej. Przy Git Worktrees i sub-agentach mamy wiele sesji — koszty rosną.

Kilka zasad które minimalizują koszty:"

💬 WKLEJ NA CHAT:
```
Zarządzanie kontekstem — dobre praktyki:

/compact  — kompresuje historię sesji (zostaje summary)
          → używaj gdy sesja jest długa i agent zaczyna się gubić

/clear    — czyści całą historię
          → nowa sesja, świeży kontekst

/cost     — pokaż ile tokenów/koszt sesji
          → monitoruj regularnie

Context Rot — kiedy agent zaczyna 'zapominać' wcześniejszych decyzji:
1. Użyj /compact
2. Jeśli nie pomaga → /clear + nowa sesja
3. CLAUDE.md gwarantuje że kluczowe instrukcje są zawsze w kontekście

Wskazówka: krótkie sesje > jedna mega-sesja.
Granularne commity pozwalają zaczynać nową sesję bez utraty postępu.
```

---

## 14:30–14:45 — ☕ OPCJONALNA PRZERWA
⏱️ 15 min

💡 Podczas przerwy: sprawdź worktrees uczestników, pomóż z merge conflicts.

---

## 14:45–15:30 — Moduł 3.3: Debugging & Refactoring z agentem
⏱️ 45 min

### Stack Trace → AI Debug

🎬 **CO MÓWIĘ:**

„Moduł 3.3 — debugging i refactoring. Wiem że cały dzień generowaliśmy kod — i założę się że część z was ma już błędy. Dobrze. Teraz nauczymy się jak pracować z agentem żeby je naprawić.

Pierwsza zasada debugowania z AI: nie pytaj 'dlaczego to nie działa'. Daj mu dokładny kontekst — stack trace, logi, krok po kroku co robiłeś. Agent jest dobry w analizie — ale tylko jeśli ma dane."

💬 WKLEJ NA CHAT:
```
Debug z agentem — dobry prompt:

"Mam błąd podczas uruchamiania aplikacji.

Stack trace:
[WKLEJ TUTAJ PEŁNY STACK TRACE]

Co robiłem:
1. Dodałem ChatController
2. Uruchomiłem ./mvnw spring-boot:run
3. Dostałem powyższy błąd

Kontekst:
- Spring Boot 3.5, Java 21
- ChatController implementuje SSE endpoint
- Mamy spring-ai-starter-model-openai w zależnościach

Pytanie: jaka jest przyczyna? Co poprawić?"

VS:

❌ ZŁY prompt:
"Dlaczego mój kod nie działa?"
```

---

### 🏋️ Ćwiczenie 3.5 — Debug Sesja
⏱️ 15 min

🎬 **CO MÓWIĘ:**

„Teraz chcę żebyście celowo wywołali błąd — i zdebugowali go z agentem. Jeśli macie już prawdziwy błąd z wcześniejszych ćwiczeń — użyjcie go. Jeśli nie — poniżej ćwiczenie:"

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.5 — Debug z agentem (15 min)

OPCJA A: Masz już błąd?
→ Skopiuj pełny stack trace + kontekst
→ Wklej do claude z dobrym promptem (wzór z chatu powyżej)
→ Sprawdź czy agent znalazł przyczynę i naprawił

OPCJA B: Nie masz błędu?
→ Wklej do claude:

"Celowo zepsuj jedno wywołanie w ChatController tak żeby
 aplikacja startowała ale /api/chat zwracało błąd 500.
 Następnie uruchom aplikację i pokaż mi stack trace.
 Na końcu — debuguj razem ze mną."

→ Obserwuj jak agent wprowadza błąd, generuje logi i go naprawia

🔵 Zaawansowani:
   Sprawdź czy SSE stream rzeczywiście emituje Vercel Data Stream format.
   Użyj curl do ręcznego sprawdzenia:
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"messages":[{"role":"user","content":"Test"}]}' \
     --no-buffer
   Porównaj output z oczekiwanym formatem: 0:"text"\n
```

---

### Refactoring z agentem

🎬 **CO MÓWIĘ:**

„Ostatni temat dziś: refactoring. Wygenerowany kod rzadko jest piękny za pierwszym razem. Agent wie o tym — i jest dobry w refactoringu, ale tylko z dobrym briefem.

Kluczowe: mów agentowi CO chcesz ulepszyć i DLACZEGO — nie tylko 'zrefaktoruj'. Agent bez kierunku będzie 'refactorować' losowo i może zmienić rzeczy których nie chcesz."

💬 WKLEJ NA CHAT:
```
Refactoring z agentem — dobry prompt:

"Refaktoruj VerificationService.java.

Co chcę poprawić:
1. Metoda analyzeImage() ma 80 linii — wyodrębnij logikę do prywatnych metod
2. String literals dla promptów powinny być stałymi (static final)
3. Dodaj @Slf4j i zastąp System.out.println logowaniem

Czego NIE zmieniaj:
- Sygnatur metod publicznych (używane w testach)
- Logiki biznesowej
- Formatu zwracanego obiektu

Po refaktoringu uruchom testy: ./mvnw test
Jeśli zielone: commit 'Refactor: extract methods in VerificationService'"

Wskazówka: zawsze definiuj zakres refactoringu.
Agent bez granic może zmienić zbyt wiele.
```

---

### Feedback loop — kiedy agent się myli

🎬 **CO MÓWIĘ:**

„Ważna umiejętność: rozpoznanie kiedy agent idzie w złym kierunku — i jak go skorygować bez cofania całej pracy.

Trzy sygnały że coś nie gra:
1. Agent zmienia rzeczy których nie prosiłeś
2. Test który był zielony — stał się czerwony
3. Kod jest bardziej skomplikowany niż był

Reakcja:"

💬 WKLEJ NA CHAT:
```
Kiedy agent idzie w złym kierunku:

1. STOP — nie kontynuuj sesji w tym kierunku
   Napisz: "Stop. Cofnij ostatnie zmiany."

2. REVERT — jeśli commitowałeś granularnie, łatwo:
   git diff HEAD~1 --stat  (co się zmieniło)
   git revert HEAD         (cofnij ostatni commit)

3. BRIEF — wyjaśnij dokładniej co chcesz:
   "Poprzednie zmiany były za szerokie.
    Chcę TYLKO [konkretna zmiana].
    NIE zmieniaj [co pozostawić bez zmian]."

4. WERYFIKUJ — po każdej sesji:
   git diff            (co się zmieniło)
   ./mvnw test         (czy testy zielone)
   npm test            (FE)

Granularne commity = safety net.
Każdy commit = punkt powrotu.
```

---

## 15:30–15:55 — Git Worktrees demo + podsumowanie
⏱️ 25 min

### Demo: Parallel Agents in Action

📺 **CO POKAZUJĘ:** Dwa terminale side-by-side.

🎬 **CO MÓWIĘ:**

„Ostatni element dnia — demo jak to wszystko wygląda razem. Mam dwa terminale:
- Lewy: `cd ../sinsay-be && claude`
- Prawy: `cd ../sinsay-fe && claude`

Terminal lewy dostaje zadanie: 'Dodaj endpoint /api/health który zwraca JSON ze statusem'
Terminal prawy dostaje zadanie: 'Dodaj komponent StatusBadge który wywołuje /api/health i pokazuje status'

Puścimy oba jednocześnie i obserwujemy."

📺 Wklejam prompty w oba terminale, obserwuję z uczestnikami.

🎬 **CO MÓWIĘ (po demo):**

„To jest Stage 4 w skali którą omawialiśmy w dniu 1. Nie jeden agent — wieloagentowy pipeline. BE i FE idą równolegle, kontrakt API ustalony z ADR, każdy agent pracuje niezależnie.

Za dwa dni — Stage 5. Ale najpierw musimy domknąć implementację Sinsay jutro."

---

### 🏋️ Ćwiczenie 3.6 — End-of-Day Commit
⏱️ 5 min

💬 WKLEJ NA CHAT:
```
🏋️ Ćwiczenie 3.6 — End-of-Day Commit (5 min)

1. Sprawdź status repo:
   git status
   git diff --stat

2. Uruchom testy:
   ./mvnw test                    (backend)
   cd frontend && npm test         (frontend, jeśli masz)

3. Jeśli wszystko zielone — commit:
   git add -A
   git commit -m "Day3: initial TDD implementation - red tests + partial green"

4. Jeśli masz worktrees — sprawdź je też:
   git worktree list
   cd do każdego i zrób commit jeśli potrzeba

5. Daj 👍 na chat gdy zcommitowane
```

---

## 15:55–16:00 — Podsumowanie dnia
⏱️ 5 min

🎬 **CO MÓWIĘ:**

„Dobra — szybkie podsumowanie. Co zrobiliśmy dziś:

CLAUDE.md 2.0 — teraz wiecie o limicie 200 linii, @AGENTS.md i path-based rules. To zmieniło jak konfigurujemy agentów.

Sub-agenci — mamy BE Developera, FE Developera i QA w AGENTS.md. Jutro ich naprawdę użyjemy.

TDD z agentem — test jako specyfikacja. Red → commit → Green → commit. Nie odwrotnie.

Git Worktrees — równoległe gałęzie, równoległe agenty. Bez konfliktów.

Jutro — domykamy implementację. Sinsay chatbot ma zadziałać end-to-end. Macie dziś wieczór zadanie domowe: upewnijcie się że testy backend są zielone. Frontend może być dalej na czerwono — ale Backend SSE endpoint musi działać.

Pytania? Problemy? Rzućcie na chat. Do jutra!"

💬 WKLEJ NA CHAT:
```
Dzień 3 — co dziś zrobiliśmy:

✅ CLAUDE.md 2.0: limit 200 linii, @AGENTS.md, path-based rules
✅ Sub-agent configs: BE, FE, QA w AGENTS.md
✅ Task Plan Matrix
✅ Playwright MCP + design tokens Sinsay
✅ TDD workflow: Red → commit → Green → commit
✅ Git Worktrees: parallel agents

Zadanie na wieczór:
→ Backend SSE endpoint musi mieć zielony test
→ git push (opcjonalnie, jeśli macie remote)

Jutro (Dzień 4):
→ Pełna implementacja Sinsay end-to-end
→ Frontend integration
→ Streaming: weryfikacja Vercel Data Stream format
→ Persistence: SQLite + JPA
```

---

## Appendix A — Linki na chat (gotowe do wklejenia)

```
=== LINKI DNIA 3 ===

CLAUDE.md 2.0:
https://code.claude.com/docs/en/memory#how-claude-md-files-load
https://code.claude.com/docs/en/memory#path-specific-rules
https://code.claude.com/docs/en/memory#agents-md

Sub-agents vs Agent Teams:
https://code.claude.com/docs/en/features-overview#subagent-vs-agent-team
https://code.claude.com/docs/en/agent-teams

Playwright MCP:
https://github.com/microsoft/playwright-mcp

PromptCowboy:
https://promptcowboy.ai

Skills (community):
https://github.com/anthropics/claude-code-skills

Git Worktrees (docs):
https://git-scm.com/docs/git-worktree
```

---

## Appendix B — Kickoff projektu (jeśli ktoś nie ma struktury)

💬 Prompt do wklejenia do claude:

```
Stwórz strukturę monorepo dla projektu Sinsay Chatbot.

Struktura:
sinsay-poc/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/sinsay/
│       ├── config/
│       ├── controller/
│       ├── model/
│       └── service/
├── frontend/
│   ├── package.json
│   └── src/
│       ├── components/
│       │   ├── ui/         (Shadcn)
│       │   └── ClaimsForm.tsx
│       └── app/
├── docs/
│   ├── PRD-Sinsay-PoC.md
│   └── ADR-Sinsay-PoC.md
├── assets/
├── CLAUDE.md
└── AGENTS.md

Stack backend: Java 21, Spring Boot 3.5, Spring AI (OpenAI), SQLite/JPA, Lombok
Stack frontend: React 19, TypeScript, Vite, Vercel AI SDK, assistant-ui, Shadcn/ui, Zod

Utwórz pliki: pom.xml (backend), package.json (frontend), CLAUDE.md (max 80 linii), AGENTS.md
Commit: "Init: monorepo structure for Sinsay Chatbot PoC"
```

---

## Appendix C — Vercel Data Stream Format (cheatsheet)

```
=== Vercel AI SDK Data Stream Protocol ===

Text chunk:
0:"Hello "\n
0:"world"\n

Tool/data:
8:[{"data":"any json"}]\n

Error:
3:"error message"\n

Finish:
d:{"finishReason":"stop","usage":{"promptTokens":10,"completionTokens":20}}\n

Spring Boot SSE → Vercel format:

@GetMapping(value = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody ChatRequest request) {
    return verificationService.streamVerdict(request)
        .map(text -> "0:\"" + escapeJson(text) + "\"\n");
}
```

---

## Appendix D — CLAUDE.md 2.0 przykład dla Sinsay

```markdown
# Sinsay Chatbot PoC — Agent Instructions

## Projekt
Chatbot do weryfikacji reklamacji i zwrotów dla Sinsay Fashion.
Multimodal AI: formularz → analiza zdjęcia → werdykt po polsku.

## Przed zmianami przeczytaj
- docs/PRD-Sinsay-PoC.md
- docs/ADR-Sinsay-PoC.md

@AGENTS.md

## Kluczowe zasady
- Streaming: SSE emituje Vercel Data Stream format (0:"text"\n)
- Endpoint: POST /api/chat → text/event-stream
- AI odpowiada zawsze po polsku
- Nigdy nie commituj API keys
- Zawsze pisz test przed implementacją (TDD)

## Komendy
Backend: ./mvnw spring-boot:run | ./mvnw test
Frontend: cd frontend && npm run dev | npm test | npm run lint
```

---

## Appendix E — Najczęstsze błędy dnia 3

| Problem | Przyczyna | Rozwiązanie |
|---|---|---|
| CLAUDE.md ignoruje część instrukcji | Plik ma ponad 200 linii | Skróć do <200, przenieś do AGENTS.md |
| Agent nie stosuje reguł path-based | Brak frontmatter w .claude/rules/*.md | Dodaj `---\nglobs: ["pattern"]\n---` |
| SSE zwraca raw JSON zamiast Vercel format | Brak adaptera w ChatController | Mapuj przez `"0:\"" + text + "\"\n"` |
| Git worktree conflict | Próba checkout tego samego brancha | Każdy worktree = osobny branch |
| Agent zmienia za dużo podczas refactoring | Zbyt ogólny prompt | Definiuj scope: "TYLKO X, NIE zmieniaj Y" |
| Test zielony ale agent dodał martwy kod | Context rot w długiej sesji | /compact lub nowa sesja + nowy commit |

---

## Appendix F — Plan B (timing)

Jeśli pójdziemy za wolno z ćwiczeniami:

- **09:10–09:55** CLAUDE.md 2.0 — skróć do 30 min, pomiń demo path rules (powiedz tylko że istnieją)
- **09:55–10:40** Sub-agents — skróć do 20 min, pomiń ćwiczenie 3.1 (zrób jako homework)
- **11:15–12:30** Moduł 3.1 — jeśli Playwright MCP nie działa u uczestników, prowadź tylko jako demo
- **13:30–14:30** Moduł 3.2 — TDD Green jest priorytet, Git Worktrees możesz skrócić do 10-minutowego demo
- **14:45–15:30** Moduł 3.3 — jeśli nie ma czasu, zamień na Q&A + debugging konkretnych problemów uczestników

Minimum Day 3:
✅ CLAUDE.md 2.0 wiedza (teoria)
✅ Przynajmniej jeden zielony test w repo każdego uczestnika
✅ Wiadomo co jutro implementować (Task Plan Matrix)
