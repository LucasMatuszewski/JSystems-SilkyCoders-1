# Dzień 4 — Pełny scenariusz prowadzenia
**Temat: Jakość, testy, cloud agenci i przygotowanie pod CI/CD**
**Godz. 9:00–16:00 | Szkolenie stacjonarne / online | Uczestnicy: senior Java / full stack / architekci**

> 🎬 = co mówię
> 📺 = co pokazuję
> 💬 = wklejam na chat
> 🏋️ = ćwiczenie
> ⏱️ = czas
> 💡 = uwaga dla prowadzącego
> ⚠️ = ważne ograniczenie / nie zgadujemy, mówimy tylko to, co zweryfikowane

---

## AGENDA DNIA

💬 WKLEJ NA CHAT:
```text
Dzień 4 – agenda:

09:00  Reset po weekendzie + status appki
09:10  Dokańczamy appkę wg planu agentów
09:40  Weryfikacja “advanced plan”: zależności, blokery, równoległość
10:00  Debugging + refactoring z agentami
11:00  ☕ PRZERWA

11:15  Test review: czy testy stworzone przez agentów naprawdę coś dają?
11:45  TDD i antywzorce: jak nie pozwolić agentowi “naprawiać testów”
12:15  Testy integracyjne z seedem bazy danych
13:00  🍽️ PRZERWA

13:30  Skill do testów integracyjnych albo workflow “manual-first”
14:15  Claude Code Web + cloud agents + admin settings + shared rules
15:00  GitHub Copilot coding agent i code review w GitHub
15:30  CI/CD preview: headless agenci, GH Actions, co zostawiamy na dzień 5
15:55  Podsumowanie i zadanie domowe
16:00  Koniec
```

---

## Założenie dnia

To jest dzień:
1. domknięcia aplikacji, jeśli ktoś nie zdążył po weekendzie,
2. nauczenia uczestników jak odzyskać kontrolę nad jakością,
3. przejścia od “agent coś wygenerował” do “agent pracuje w procesie, który umiem sprawdzić”.

💡 Z notatek wynika, że główne problemy grupy to:
- halucynacje,
- spam i za długie odpowiedzi,
- gubienie kontekstu,
- słabe testy,
- testy integracyjne z realną bazą,
- brak zaufania, że agent znajdzie prawdziwy błąd zamiast zamaskować problem.

Dlatego dzień 4 zaczynamy od jakości i weryfikacji, a nie od nowych fajnych feature’ów.

---

## 09:00–09:10 — Reset po weekendzie
⏱️ 10 min

🎬 **CO MÓWIĘ:**

„Na start chcę szybki reality check, nie teorię. Mieliśmy po piątku plan i zadanie domowe: skończyć appkę z agentami. Zakładam, że część osób to zrobiła, ale nie zakładam, że wszyscy.

Dajcie proszę na chat:
- ✅ jeśli appka działa end-to-end
- 🟡 jeśli działa częściowo
- ❌ jeśli plan jest, ale implementacja nie jest skończona
- 📄 jeśli macie też advanced plan z dependency mapą i macierzą agentów”

🎬 **CO MÓWIĘ DALEJ:**

„Pierwszy blok robimy pragmatycznie. Najpierw kończymy aplikację i sprawdzamy, czy plan był wystarczająco dobry. Dopiero potem przechodzimy do testów i cloud agentów.”

---

## 09:10–09:40 — Weryfikacja advanced planu
⏱️ 20 min

🎬 **CO MÓWIĘ:**

„W piątek prosiliśmy agentów nie tylko o plan z listą tasków, ale o plan bardziej dojrzały: zależności, blokery, równoległość, fazy, briefy dla sub-agentów. Jeśli ktoś tego nie ma, to nie jest kosmetyka. To jest powód, dla którego agent później pracuje chaotycznie.”

🎬 **CO MÓWIĘ DALEJ:**

„Dobry plan dla multi-agent workflow powinien zawierać:
- fazy,
- dependency mapę,
- co blokuje co,
- co może iść równolegle,
- jaki kontekst ma dostać każdy sub-agent,
- jaki jest warunek zakończenia tasku,
- czym weryfikujemy wynik.”

💬 WKLEJ NA CHAT:
```text
Prompt — ocena jakości planu

Oceń jakość naszego planu implementacji.

Sprawdź, czy plan zawiera:
- fazy / etapy,
- dependency map,
- blokery,
- taski możliwe do wykonania równolegle,
- brief dla każdego sub-agenta,
- warunek zakończenia tasku,
- sposób weryfikacji tasku.

Jeśli czegoś brakuje, popraw plan.

Format:
1. Co jest OK
2. Czego brakuje
3. Poprawiona wersja planu
4. Krótka macierz:
   | Task | Agent | Depends on | Parallel | Verification |
5. Krótki graph / dependency map w Mermaid
```

### Jeśli ktoś nie ma advanced planu

💬 WKLEJ NA CHAT:
```text
Prompt — wygeneruj plan w wersji advanced

Na podstawie PRD, ADR i aktualnego kodu przygotuj advanced implementation plan.

Wymagania:
- podziel plan na fazy,
- dla każdej fazy pokaż taski,
- pokaż zależności między taskami,
- pokaż co może być robione równolegle,
- wskaż co blokuje kolejne etapy,
- napisz jaki dokładnie kontekst i pliki trzeba dać sub-agentowi na każdym tasku,
- dodaj verification step dla każdego tasku,
- dodaj dependency graph w Mermaid,
- dodaj macierz:
  | Phase | Task | Agent | Input context | Depends on | Output | Verification |

Zapisz jako docs/day-4-advanced-plan.md
```

💡 Tu warto pokazać związek z oficjalną rekomendacją Claude: „explore first, then plan, then code” oraz „let Claude interview you” dla większych zadań.

---

## 09:40–10:00 — Dokańczamy appkę według planu
⏱️ 30 min

🎬 **CO MÓWIĘ:**

„Jeśli aplikacja nie działa, nie wymyślamy nowego planu od zera. Najpierw każemy agentowi kontynuować istniejący plan, z naciskiem na weryfikację po każdym kroku.”

💬 WKLEJ NA CHAT:
```text
Prompt startowy — dokończenie appki

Przeczytaj najpierw:
- docs/PRD-Product-Requirements-Document.md
- docs/ADR/000-main-architecture.md
- docs/ADR/001-backend.md
- docs/ADR/002-frontend.md
- docs/task-plan-matrix.md lub docs/day-3-PLAN-dependency-matrix-pararelizm-map.md jeśli istnieje

Następnie:
1. oceń aktualny stan implementacji, odpal aplikację (BE i FE) i sprawdź co działa, odpal testy e2e i inne,
2. porównaj stan implementacji z planem,
3. wypisz co jest skończone, co jest zablokowane, co można zrobić równolegle, jakie są błędy, co działa a co nie,
4. zaproponuj najkrótszą ścieżkę do działającego MVP,
5. wykonuj zadania krokami, po każdym kroku uruchamiaj odpowiednią weryfikację (testy, linting lub/i manaualne sprawdzenie np. z Playwright).

Zasady:
- nie zmieniaj testów tylko po to, żeby przechodziły,
- jeśli test jest zły, najpierw uzasadnij dlaczego,
- jeśli czegoś nie wiesz, sprawdź dokumentację albo Context7,
- commit po każdym logicznym kroku.
```

📺 **CO POKAZUJĘ:**
- uruchomienie tego promptu,
- agent w trybie plan + wykonanie,
- weryfikacja po każdym kroku.

💡 Jeśli większość grupy utknęła, prowadź to jako wspólny live debugging/finish session.

---

## 10:00–11:00 — Debugging + refactoring
⏱️ 60 min

🎬 **CO MÓWIĘ:**

„To jest domknięcie punktu 3.3 z dnia 3. Teraz nie chodzi już o samo generowanie kodu, tylko o to, czy umiemy znaleźć problemy, udowodnić problem i poprawić root cause.”

### Zasada

Nie mówimy agentowi:
- „napraw appkę”

Mówimy:
- jaki jest objaw,
- jak odtworzyć problem,
- co już sprawdziliśmy,
- jak zweryfikujemy poprawkę.

💬 WKLEJ NA CHAT:
```text
Prompt — debugging z root cause analysis

Mamy problem z aplikacją. Najpierw zrób analizę, nie naprawiaj od razu.

Wykonaj:
1. odtwórz problem,
2. znajdź root cause,
3. zaproponuj minimalną poprawkę,
4. napisz albo popraw test, który łapie ten problem,
5. dopiero potem wdroż poprawkę,
6. uruchom weryfikację.

Zasady:
- nie usuwaj asercji tylko po to, żeby test przeszedł,
- nie zmieniaj kontraktu API bez uzasadnienia,
- jeśli problem dotyczy biblioteki lub frameworka, sprawdź oficjalną dokumentację / Context7,
- na końcu opisz: problem, przyczyna, poprawka, dowód że działa.
```

### Konkretne prompt-y do pokazania

💬 WKLEJ NA CHAT:
```text
Backend:
"Odtwórz i napraw problem ze streamingiem odpowiedzi. Sprawdź czy backend naprawdę zwraca format Vercel data stream wymagany przez frontend. Dodaj test, który łapie zły format streamu."

Frontend:
"Sprawdź czy po odświeżeniu strony sesja jest poprawnie wznawiana z localStorage i backendu. Jeśli nie, odtwórz błąd, napisz failing test i napraw root cause."

Full-stack:
"Przejdź cały flow: formularz -> analiza -> czat -> wznowienie sesji. Znajdź pierwsze miejsce, gdzie flow się psuje. Napraw tylko to miejsce i pokaż dowód."
```

### Refactoring

🎬 **CO MÓWIĘ:**

„Refactoring robimy dopiero wtedy, gdy mamy zielony dowód zachowania. Najpierw test albo inna weryfikacja, potem porządki.”

💬 WKLEJ NA CHAT:
```text
Prompt — refactoring po stabilizacji

Kod już działa. Teraz zrób refactoring tylko w wybranym zakresie.

Zakres:
- uprość zbyt długie metody,
- nazwij lepiej klasy / metody / zmienne,
- dodaj lepszy error handling,
- usuń duplikację, DRY,
- zachowaj obecne zachowanie i kontrakty.

Nie zmieniaj:
- publicznego API,
- tekstów user-facing,
- semantyki testów.

Po refactoringu:
- uruchom testy,
- pokaż diff w 5 punktach,
- wyjaśnij, co zyskał kod.
```

💡 Celem tego bloku jest dojście do działającej appki przed 11:00. Jeśli to się nie uda, obetnij refactoring i skup się na dowiezieniu działania.

---

## 11:00–11:15 — ☕ PRZERWA

---

## 11:15–11:45 — Review wygenerowanych testów
⏱️ 30 min

🎬 **CO MÓWIĘ:**

„Teraz najważniejszy moment dnia. Nie pytamy ‘czy mamy testy’. Pytamy: ‘czy te testy naprawdę coś łapią?’

To, że agent wygenerował 40 testów, nie znaczy, że dają one pewność, że wszystko działa.
Bardzo często mamy teatr bezpieczeństwa.”

### Co sprawdzamy

1. Czy test sprawdza zachowanie istotne biznesowo.
2. Czy da się łatwo zepsuć aplikację tak, że test dalej jest zielony.
3. Czy asercje są mocne, a nie symboliczne.
4. Czy test nie jest flaky.
5. Czy test nie jest zbyt sprzężony z implementacją.

💬 WKLEJ NA CHAT:
```text
Prompt — review testów

Zrób review testów w tym repo.

Oceń:
- czy testy łapią prawdziwe regresje,
- czy asercje są wystarczająco mocne,
- czy testy nie są flaky,
- czy nie testują szczegółów implementacji zamiast zachowania,
- czy są luki w coverage najważniejszych flow,
- czy testy sprawdzają edge cases (nie tylko happy path).

Chcę wynik w formacie:
1. Krytyczne problemy
2. Słabe testy do poprawy
3. Brakujące testy
4. 5 najważniejszych testów do dopisania teraz
```

```
Użyj komendy /review.
```

### Ćwiczenie: celowe psucie aplikacji

🎬 **CO MÓWIĘ:**

„Najlepszy sposób na sprawdzenie testu to spróbować celowo zepsuć kod. Jeśli test dalej przechodzi, to test jest słaby.”

💬 WKLEJ NA CHAT:
```text
Prompt — mutation-style sanity check

Przejrzyj testy i wskaż 3 miejsca, w których można celowo zepsuć aplikację, a istniejące testy mogą tego nie złapać.

Dla każdego przypadku opisz:
- co zepsuć,
- który test powinien to złapać,
- czy obecnie to złapie,
- jaka poprawka testu jest potrzebna.
```

---

## 11:45–12:15 — TDD i antywzorce
⏱️ 30 min

🎬 **CO MÓWIĘ:**

„Najgroźniejszy antywzorzec przy pracy z agentem wygląda tak: test jest czerwony, agent zmienia test, a nie system. I formalnie wszystko jest zielone. Tylko że produkt dalej jest zepsuty.”

### Reguły, które warto uczestnikom narzucić

💬 WKLEJ NA CHAT:
```text
Reguły pracy z agentem przy testach:

1. Najpierw uzgodnij zachowanie z PRD/ADR.
2. Potem napisz failing test.
3. Agent nie może zmieniać testu bez uzasadnienia.
4. Zmiana testu musi wynikać z błędu w specyfikacji albo błędu w teście, nie z wygody.
5. Jeśli test jest czerwony i zachowanie jest poprawnie opisane w PRD/ADR, poprawiamy aplikację.
6. Po naprawie uruchamiamy testy, lint i build.
```

### Prompt do bezpiecznego TDD

💬 WKLEJ NA CHAT:
```text
Prompt — TDD bez psucia testów

Pracujemy w TDD.

Kroki:
1. przeczytaj PRD/ADR dla tego flow,
2. napisz test, który odzwierciedla wymaganie,
3. uruchom go i pokaż, że failuje z właściwego powodu,
4. napraw produkcyjny kod minimalnym kosztem,
5. uruchom test ponownie,
6. na końcu uruchom odpowiedni szerszy zestaw testów.

Ważne:
- nie zmieniaj testu po jego napisaniu bez wyraźnego uzasadnienia,
- jeśli uważasz, że test jest zły, najpierw zatrzymaj się i wyjaśnij dlaczego,
- jeśli zachowanie nie jest jasne, zapytaj albo wróć do specyfikacji.
```

---

## 12:15–13:00 — Testy integracyjne z seedem bazy danych
⏱️ 45 min

🎬 **CO MÓWIĘ:**

„To jest dokładnie temat, który wrócił w notatkach od kilku osób. Uczestnicy chcą testów integracyjnych z prawdziwymi danymi, nie z mockami. I mają rację.

Jeżeli aplikacja używa bazy, to test integracyjny powinien umieć:
- przygotować bazę,
- wstawić znane dane,
- wykonać flow,
- sprawdzić wynik i stan bazy.”

### Co chcę pokazać

1. Seed danych jako jawny krok.
2. Test z realnym repozytorium / DB.
3. Mały, powtarzalny dataset.
4. Czytelny teardown lub reset między testami.

### Pomysł na ćwiczenia dla tej appki

💬 WKLEJ NA CHAT:
```text
Pomysły na testy integracyjne dla naszej aplikacji:

1. Session resume
- seed sesji i wiadomości do SQLite
- GET /api/sessions/{id}
- oczekujemy pełnej historii w dobrej kolejności

2. Initial analysis persistence
- wysyłamy poprawny formularz
- sprawdzamy, że Session i pierwsze ChatMessage są zapisane

3. Chat continuation persistence
- seed istniejącej sesji
- POST nowej wiadomości
- po zakończeniu streamu sprawdzamy, że USER i ASSISTANT message są zapisane

4. Wrong intent / wrong policy context
- dla RETURN test sprawdza, że nie użyto reklamacje.md
- dla COMPLAINT test sprawdza, że nie użyto zwrot-30-dni.md

5. Edge case
- lokalStorage ma sessionId, ale sesji nie ma w DB
- frontend powinien wyczyścić sessionId i wrócić do formularza
```

### Prompt do wygenerowania testu integracyjnego

💬 WKLEJ NA CHAT:
```text
Prompt — test integracyjny z seedem

Napisz test integracyjny z realną bazą danych dla tego scenariusza:
[tu wklej konkretny scenariusz]

Wymagania:
- żadnych mocków dla repozytoriów i bazy,
- dane testowe przygotuj jawnie przed testem,
- jeśli trzeba, dodaj helper / seed factory / skrypt seedujący,
- test ma weryfikować zarówno odpowiedź API, jak i stan bazy,
- test ma być deterministyczny i niezależny od kolejności uruchamiania.

Na początku opisz:
1. jaki dataset seedujesz,
2. dlaczego taki,
3. jakie asercje są krytyczne.
```

💡 Jeśli uczestnicy narzekają, że agent nie umie tego napisać sam, to właśnie o to chodzi w następnym bloku: skill albo workflow manual-first.

---

## 13:00–13:30 — 🍽️ PRZERWA

---

## 13:30–14:15 — Skill do testów integracyjnych albo workflow manual-first
⏱️ 45 min

🎬 **CO MÓWIĘ:**

„Są tu dwa dobre podejścia i oba są poprawne.

Podejście A: budujemy skill, który zmusza agenta do pracy dokładnie według naszego procesu.

Podejście B: najpierw każemy agentowi napisać test tak, jak my byśmy go napisali ręcznie, a dopiero potem każemy z tego zrobić skill.”

### Podejście A — skill

💬 WKLEJ NA CHAT:
```text
Prompt — stwórz skill do testów integracyjnych

Stwórz skill do pisania testów integracyjnych dla tego projektu.

Skill ma wymuszać workflow:
1. przeczytaj PRD/ADR i istniejący kod,
2. zdefiniuj scenariusz biznesowy,
3. przygotuj jawny seed danych,
4. użyj realnej bazy / realnego repo, bez mocków jeśli nie są konieczne,
5. napisz test z mocnymi asercjami,
6. uruchom test,
7. jeśli test nie działa, napraw aplikację albo test tylko z uzasadnieniem,
8. na końcu uruchom szerszą weryfikację.

Dodaj:
- checklistę dla agenta,
- listę antywzorców,
- wskazówki kiedy NIE zmieniać testu,
- miejsce na skrypt seedujący bazę.
```

### Podejście B — manual-first

💬 WKLEJ NA CHAT:
```text
Prompt — napisz test tak, jak zrobiłby to senior ręcznie

Chcę napisać test integracyjny w stylu manual-first.

Nie twórz od razu finalnego kodu.
Najpierw:
1. zaproponuj strukturę testu,
2. opisz seed danych,
3. opisz dokładne asercje,
4. pokaż gdzie agent zwykle popełnia błąd przy takim teście,
5. dopiero po akceptacji wygeneruj kod.

Chcę wynik jak od senior developera, nie “jak najszybciej zielony test”.
```

### Jeśli chcemy dołożyć skrypt seedujący

🎬 **CO MÓWIĘ:**

„Jeśli seed danych jest powtarzalny, warto mieć helper albo skrypt. Wtedy nie opisujemy agentowi od nowa całego setupu przy każdym teście.”

💬 WKLEJ NA CHAT:
```text
Prompt — seed script / helper

Przygotuj mechanizm seedowania danych testowych do testów integracyjnych.

Wymagania:
- ma być prosty i deterministyczny,
- ma dawać dokładnie te rekordy, których potrzebujemy do testów,
- ma być łatwy do uruchomienia przed testem i łatwy do resetu,
- dodaj przykład użycia w teście.

Jeśli najlepszy będzie helper w Javie, zrób helper w Javie.
Jeśli lepszy będzie skrypt uruchamiany przed testem, przygotuj skrypt i opisz wpięcie.
```

💡 Tu nie obiecuj jednego “magicznego” rozwiązania. Chodzi o proces, nie o konkretną technologię.

---

## 14:15–15:00 — Claude Code Web, cloud agenci, shared rules, admin settings
⏱️ 45 min

🎬 **CO MÓWIĘ:**

„Teraz przechodzimy do pracy w chmurze. Ale tylko to, co mam zweryfikowane w dokumentacji i co ma sens dla waszego workflow.”

### Co jest zweryfikowane

1. Claude Code on the web uruchamia taski w zarządzanej przez Anthropic infrastrukturze.
2. Każda sesja działa w izolowanym środowisku.
3. Można uruchamiać wiele tasków równolegle.
4. Są ograniczenia sieci i filesystemu.
5. Można ustawić custom network configuration / allowed domains.
6. Na Team / Enterprise admin może włączyć lub wyłączyć web sessions / remote access.
7. Dla dużych zespołów można mieć centralnie zarządzany `CLAUDE.md`.
8. Można współdzielić rules między projektami przez symlinki.
9. Pluginy mogą zawierać skills, agents, hooks, MCP config i settings.

### Co mówię

🎬 **CO MÓWIĘ:**

„Claude Code Web ma sens wtedy, gdy chcemy delegować zadania równolegle bez odpalania wielu lokalnych terminali. To jest dobre do:
- dobrze zdefiniowanych bugfixów,
- rutynowych poprawek,
- pytań o repo,
- backendowych tasków, gdzie agent może sam się weryfikować testami.

Nie traktowałbym tego jako zamiennika lokalnej pracy nad wszystkim. Bardziej jako warstwę delegacji.”

### Prompt do pokazania

💬 WKLEJ NA CHAT:
```text
Prompt do cloud agenta:

Read the repository, identify why session resume can fail, propose the smallest safe fix, implement it, run the relevant tests, and open a PR with a short summary.

Constraints:
- do not change public API contracts,
- do not weaken tests,
- explain root cause clearly,
- if documentation is needed, use official docs only.
```

### Shared rules, symlinki i managed CLAUDE.md

🎬 **CO MÓWIĘ:**

„Dla zespołów to jest ważniejsze niż pojedyncze prompty. Jeśli macie 20 repozytoriów, nie chcecie kopiować tych samych zasad ręcznie wszędzie.

Mamy trzy poziomy:
1. wspólny centralny `CLAUDE.md`,
2. współdzielone rules przez symlinki,
3. plugin jako paczka ze skillami, agentami, hookami i MCP configiem.”

💬 WKLEJ NA CHAT:
```text
Prompt — zaprojektuj shared AI team setup

Zaproponuj strukturę dla zespołu, który ma wiele repozytoriów i chce współdzielić:
- common CLAUDE.md rules,
- skills,
- sub-agent definitions,
- hooks,
- MCP config.

Porównaj 3 opcje:
1. centralny managed CLAUDE.md,
2. repo współdzielone + symlinki,
3. plugin Claude Code.

Dla każdej opcji napisz:
- kiedy warto,
- plusy,
- minusy,
- jak wygląda rollout w zespole.
```

---

## 15:00–15:30 — GitHub Copilot coding agent i code review
⏱️ 30 min

🎬 **CO MÓWIĘ:**

„Tutaj proponuję prostą kolejność: najpierw GitHub Copilot coding agent w GitHub, potem dopiero porównanie z Claude.”

### Co jest zweryfikowane

1. Copilot coding agent działa w ephemerycznym środowisku opartym o GitHub Actions.
2. Można zlecić zadanie przez issue, agents panel, chat, CLI i narzędzia z MCP.
3. Można poprosić Copilota o zmiany do istniejącego PR przez komentarz `@copilot`.
4. Copilot wtedy tworzy child PR zamiast ruszać oryginalny PR bezpośrednio.
5. Można skonfigurować automatyczne code review dla PR.

### Co pokazuję

📺 **CO POKAZUJĘ:**
- issue przypisane do Copilota,
- komentarz `@copilot` na istniejącym PR,
- gdzie włączyć automatic review w repo ruleset.

💬 WKLEJ NA CHAT:
```text
Przykład komentarza do PR:

@copilot
Please review the changes around session persistence and streaming.
Focus on:
- regressions in session resume,
- weak tests,
- API contract mismatches between frontend and backend,
- places where the code can silently fail.

If you propose code changes, keep them minimal and explain why.
```

💬 WKLEJ NA CHAT:
```text
Przykład komentarza proszącego o zmianę w istniejącym PR:

@copilot
Please update this PR.
Add a regression test for the session resume bug and fix the implementation without weakening existing assertions.
Keep the API contract unchanged.
```

🎬 **CO MÓWIĘ:**

„To jest ważna różnica: Copilot coding agent działa w środowisku GitHub Actions i może od razu wrócić z PR-em. To jest świetne do backlogowych tasków i iteracji na PR-ach.”

### Porównanie z Claude

🎬 **CO MÓWIĘ:**

„Moja opinia: Copilot ma dziś bardzo naturalną przewagę tam, gdzie workflow już żyje w GitHubie i chcecie delegować przez issue/PR/comment. Claude ma mocny workflow lokalny i dobry model pracy przez CLI, skills, hooks i własne zasady zespołowe. Nie musicie wybierać jednej religii. Warto dobrać narzędzie do etapu pracy.”

---

## 15:30–15:55 — CI/CD preview: co robimy dziś, co zostawiamy na dzień 5
⏱️ 25 min

🎬 **CO MÓWIĘ:**

„Tutaj chcę być uczciwy: pełne, dobre przykłady Jenkins + Claude headless + Jira/Confluence to jest materiał bardziej na dzień 5 niż na końcówkę dzisiejszego dnia. Dziś robimy preview i ustawiamy sobie grunt.”

### Co jest zweryfikowane i warto pokazać

1. Claude Code można uruchamiać programistycznie przez `claude -p`.
2. Do CI/CD rekomendowany jest tryb `--bare`, żeby nie ładować lokalnych hooków, skills, pluginów, MCP i pamięci użytkownika.
3. Można używać `--output-format json` albo `stream-json`.
4. Są przykłady użycia w skryptach i CI.
5. Można użyć hooków lokalnie do automatycznych weryfikacji.
6. Claude docs wprost odsyłają do użycia Agent SDK w GitHub Actions.

📺 **CO POKAZUJĘ:**
- osobny pakiet materiałów:
  `course-materials/03-2026/cicd-headless/README.md`
- gotowe pliki do wysłania uczestnikom:
  - workflow GitHub Actions,
  - Jenkinsfile,
  - skrypty do Jira, Confluence i Bitbucket,
  - przykład konfiguracji Atlassian MCP

### Komendy do pokazania

💬 WKLEJ NA CHAT:
```bash
claude -p "What does the auth module do?"

claude --bare -p "Summarize this file" --allowedTools "Read"

claude -p "Run the test suite and fix any failures" \
  --allowedTools "Bash,Read,Edit"

claude -p "Summarize this project" --output-format json
```

### Prompt do code review w pipeline

💬 WKLEJ NA CHAT:
```text
Prompt do headless review:

Review this diff for:
- correctness,
- regressions,
- weak tests,
- missing edge cases,
- security concerns.

Do not suggest speculative architecture changes.
Return:
1. blocking issues
2. non-blocking issues
3. missing tests
4. short summary
```

### Co mówię o webhookach i delegacji do cloud agentów z CI/CD

🎬 **CO MÓWIĘ:**

„Dziś nie będę udawał, że mamy gotowy, produkcyjny wzorzec: CI/CD odpala webhook, cloud agent robi task, pipeline czeka, potem odbiera wynik. To jest ciekawy kierunek, ale chcę go przygotować lepiej na dzień 5, z realnym przykładem, a nie slajdem.”

### Krótkie mention-only

Możesz tylko wspomnieć:
- Qodo i CodeRabbit jako narzędzia stricte pod AI code review,
- Snyk jako warstwa security scanning,
- Sentry jako runtime errors / observability,
- ale bez głębokiego dema, jeśli nie masz wcześniej przygotowanego repo i scenariusza.

---

## 15:55–16:00 — Podsumowanie
⏱️ 5 min

🎬 **CO MÓWIĘ:**

„Dziś najważniejsza rzecz była taka: agent sam z siebie nie daje jakości. Jakość daje proces.

Jeśli macie:
- dobrą specyfikację,
- dobry plan,
- sensowne sub-agent briefy,
- testy, które naprawdę coś bronią,
- i weryfikację po każdym kroku,

to agent staje się bardzo mocny.

Jeśli tego nie ma, agent tylko szybciej produkuje chaos.”

💬 WKLEJ NA CHAT:
```text
Dzień 4 — najważniejsze wnioski:

1. Najpierw działająca appka, potem nowe tematy.
2. Advanced plan > zwykła lista tasków.
3. Testy mają wykrywać regresje, a nie wyglądać dobrze na screenie.
4. Test integracyjny z seedem danych jest często ważniejszy niż 10 słabych unit testów.
5. Agent nie może “naprawiać testów”, gdy problem jest w aplikacji.
6. Cloud agenci i GitHub Copilot mają sens, jeśli są wpięci w proces review i weryfikacji.
7. CI/CD z agentami robimy rozsądnie: dziś preview, jutro / na dzień 5 porządne przykłady.
```

---

## Appendix A — Linki zweryfikowane przed dniem 4

```text
Claude Code docs:
- Best practices:
  https://code.claude.com/docs/en/best-practices
- Memory / AGENTS / rules / symlinks:
  https://code.claude.com/docs/en/memory
- Permissions / managed settings:
  https://code.claude.com/docs/en/permissions
- Plugins:
  https://code.claude.com/docs/en/plugins
- Hooks:
  https://code.claude.com/docs/en/hooks-guide
- Scheduled tasks:
  https://code.claude.com/docs/en/scheduled-tasks
- Programmatic / headless usage:
  https://code.claude.com/docs/en/headless

Claude:
- Claude Code on the web:
  https://claude.com/blog/claude-code-on-the-web

GitHub Copilot:
- About coding agent:
  https://docs.github.com/en/copilot/concepts/agents/coding-agent/about-coding-agent
- Ask Copilot to create a PR:
  https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/create-a-pr
- Ask Copilot to update an existing PR:
  https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/make-changes-to-an-existing-pr
- Configure automatic Copilot code review:
  https://docs.github.com/en/copilot/how-tos/use-copilot-agents/request-a-code-review/configure-automatic-review

Claude plugin example:
- https://github.com/pluginagentmarketplace/custom-plugin-java
```

---

## Appendix B — Fakty, które warto powiedzieć dokładnie

### Claude Code best practices

- Oficjalna rekomendacja: „Give Claude a way to verify its work.”
- Oficjalna rekomendacja: „Explore first, then plan, then code.”
- Dla większych feature’ów Claude sugeruje: „Let Claude interview you”.

### Memory / rules

- `CLAUDE.md` może importować pliki przez `@path`.
- Zbyt długi `CLAUDE.md` pogarsza adherence.
- Rules mogą być path-specific przez YAML `paths:`.
- Dokumentacja wspomina współdzielenie rules między projektami przez symlinki.
- Organizacja może wdrożyć centralnie zarządzany `CLAUDE.md`.

### Hooks

- `PreToolUse`, `PostToolUse`, `Stop`, `SessionStart`, `TaskCreated`, `TaskCompleted`, `WorktreeCreate` i inne są realnymi eventami w docs.
- Hook może blokować akcję albo dodać wymuszoną weryfikację.

### Scheduled tasks

- `/loop 5m ...` jest oficjalnie opisane.
- Scheduled tasks mają różne tryby: cloud, desktop, session `/loop`.

### Headless / CI

- Oficjalna komenda: `claude -p "..."`
- Dla CI dokumentacja rekomenduje `--bare`.

### Claude Code Web

- Publicznie zweryfikowane: izolowane środowiska, równoległe taski, secure proxy do repo, custom network configuration / allowed domains, admin toggle.
- Nie pokazuj krok po kroku “secrets/env vars UI” jako pewnik bez dostępu do własnego tenanta i live demo.

### GitHub Copilot coding agent

- Działa w ephemerycznym środowisku opartym o GitHub Actions.
- Można delegować przez issue, panel, chat, CLI i MCP tools.
- `@copilot` na istniejącym PR jest oficjalnie wspierane.
- Automatic code review można skonfigurować rulesetem repo.

---

## Appendix C — Plan minimum, jeśli rano grupa utknie

Jeśli o 10:15 większość nadal nie ma działającej appki:

1. Obetnij refactoring.
2. Skup się tylko na:
- dokończeniu appki,
- debugowaniu jednego realnego problemu,
- review testów,
- jednym dobrym teście integracyjnym z seedem.
3. Cloud agenci / Copilot / CI/CD zrób jako demo prowadzącego.
4. Głębszy CI/CD i Jenkins przenieś na dzień 5.
