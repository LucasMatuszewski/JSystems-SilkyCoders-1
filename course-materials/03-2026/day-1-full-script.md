# Dzień 1 — Pełny scenariusz prowadzenia
**Temat: The AI-Augmented Developer & „odprawa" przed misją**
**Godz. 9:00–16:00 | Szkolenie stacjonarne / online | Uczestnicy: Java devs, seniorzy, tech leads, architekci**

> 🎬 = co mówię (dosłownie lub prawie dosłownie)
> 📺 = co pokazuję na ekranie
> 💬 = wklejam na chat (Zoom / Teams / Slack — gotowy tekst do skopiowania)
> 🏋️ = ćwiczenie dla uczestników
> ⏱️ = czas bloku
> 💡 = wskazówka / uwaga dla prowadzącego (nie mów tego głośno)
> 🔵 = zadanie dodatkowe dla zaawansowanych (opcjonalne, równolegle)

---

## AGENDA DNIA (wyślij na starcie)

💬 WKLEJ NA CHAT:
```
Dzień 1 – agenda:
09:00  Start + zasady pracy
09:15  Sprawdzenie środowiska (Claude Code, Java, Git)
09:35  Runda zapoznawcza + pytania startowe
10:00  Quiz kalibracyjny (weryfikacja pre-work)
10:25  Moduł 1.1 – The New Frontier (rewolucja AI, mity, dane, case studies)
11:15  ☕ PRZERWA
11:30  Moduł 1.2 – Modele, Prompty, Vibe Coding / Engineering + demo LMArena + GLM
12:35  Moduł 1.3 – Mission Briefing (projekt tygodnia)
13:00  🍽️ PRZERWA OBIAD (30 min)
13:30  Moduł 1.3 – Setup Claude Code CLI + Desktop App + pierwsze komendy
14:30  ☕ OPCJONALNA PRZERWA
14:40  Moduł 1.4 – Narzędzia: CLI vs Desktop vs IntelliJ ACP
15:35  WezTerm – dlaczego warto (mini-moduł) + Ollama / alternatywne endpointy CC
15:50  Podsumowanie dnia
16:00  Koniec
```

---

## 09:00–09:15 — Onboarding + zasady pracy
⏱️ 15 min

🎬 **CO MÓWIĘ:**

„Cześć, Dzień dobry! Dajcie znać czy mnie słychać i widać.
Powoli zaczynamy. Jeśli jesteście online — włączcie kamerki, jeśli możecie. Dużo łatwiej mi wyczuć tempo i zasymulujemy lepiej pracę w gr
-upie. I szczerze mówiąc — żebym wiedział, czy ktoś nie zasnął. Żartuję. Prawie. Jeśli nie możecie — spoko, rozumiem.

Szybko o zasadach, żebyśmy nie tracili czasu potem.

Pierwsza zasada: 👍 na czacie znaczy 'jasne, działa, jestem z tobą'. To nasz protokół synchronizacji — zamiast każdy mówi 'tak tak', dacie po prostu kciuka.

Druga: pytania od razu na chat albo głosem. Nie czekamy. Jeśli coś nie działa — mówcie, przerywajcie. Serio.

Trzecia: to nie jest kurs oparty na slajdach. Przez większość czasu chcę abyście sami próbowali wykonywać to co ja, a nie tylko patrzyli. Będę podsyłał ćwiczenia, ale starajcie się wykonywać to co ja także poza nimi.

Czwarta: kod od AI traktujemy jako draft. Weryfikujemy. Ale powiem też kiedy można nawet nie czytać — i dlaczego.

Misja na ten tydzień: zwiększyć tempo pracy z zachowaniem jakości i bezpieczeństwa. Budujemy AI-first workflow — świadomie i bezpiecznie. Jeden spójny projekt przez 5 dni, realne artefakty.

Skupiamy się na Claude Code — jednym narzędziu, żeby nie tracić czasu na zapamiętywanie różnic i skupić się na workflow i pracy z AI, a nie na różnicach między narzędziami.

Pod koniec tygodnia powiem więcej, dlaczego inne narzędzia (głównie Codex) robią to samo inaczej i jak przenieść tę wiedzę. A dzisiaj jeszcze będziemy mówić o integracjach między Claude Code and IntelliJ.

OK — kciuki, jeśli zasady jasne?"

📺 **CO POKAZUJĘ:**
- Agendę całego tygodnia (5 dni, tematy przewodnie)
- Agendę dnia 1 (wklejoną na chat)

💬 WKLEJ NA CHAT:
```
Zasady pracy:
👍 = jasne / działa / jestem z wami
❓ = pytanie (wrzuć na chat lub powiedz głosem)
🐛 = coś nie działa / blokada — mów od razu!
🔵 = zadanie dla zaawansowanych (opcjonalne, równolegle)
Pytania od razu. Przerywajcie.
```

---

## 09:15–09:35 — Sprawdzenie środowiska
⏱️ 20 min

💡 **Uwaga:** Uczestnicy powinni mieć zainstalowane wszystko z listy pre-work. Tu szybko weryfikujemy i pomagamy tym którzy mają problemy.

🎬 **CO MÓWIĘ:**

„Zanim wejdziemy w merytorykę — potrzebujemy żebyście wszyscy byli gotowi do pracy. Przysłaliśmy przed szkoleniem listę rzeczy do zainstalowania. Sprawdźmy teraz razem czy wszystko działa.

Otwórzcie terminal — może to być Windows Terminal, PowerShell, WezTerm, albo wbudowany terminal IntelliJ. I wklejcie po kolei te komendy."

💬 WKLEJ NA CHAT:
```
Sprawdzenie środowiska — wpisz w terminalu:

claude --version
java --version
node --version
git --version

Jeśli któraś zwraca błąd — powiedz i wklej na chat co nie działa
```

🎬 „Jeśli `claude --version` nie działa — to znaczy, że Claude Code nie jest zainstalowany lub nie ma go w PATH. Instalacja przez npm:"

💬 WKLEJ NA CHAT:
```
Instalacja Claude Code CLI:
npm install -g @anthropic-ai/claude-code

Po instalacji:
claude --version

Dokumentacja: https://code.claude.com/docs/en/quickstart
```

🎬 „Java 21+, Node.js i Git — bez tych trzech nie ruszamy. Jeśli coś nie działa, zerknę teraz do Was.

*[Sprawdź przez screen share lub osobiście]*

Dla tych, którzy mają wszystko — zalogujcie się do Claude Code:"

💬 WKLEJ NA CHAT:
```
Logowanie do Claude Code:
claude

Przy pierwszym uruchomieniu:
→ otworzy się przeglądarka → zaloguj się na swoje konto Claude
→ lub wróć do terminala i wpisz API key jeśli masz

Macie subskrypcję Team — logujcie się przez konto firmowe.
```

📺 **CO POKAZUJĘ:**
- Demo logowania `claude` → przeglądarka → powrót do CLI
- Jak wygląda Claude Code po zalogowaniu (welcome screen)
- `claude --version` i podstawowe info

🎬 „Świetnie. Kto jest zalogowany — dajcie 👍. Kto ma problem — 🐛 na chat."

*[Pomóż tym którzy mają problemy. Max 10 minut na rozwiązywanie, reszta czeka]*

---

## 09:35–10:00 — Runda zapoznawcza
⏱️ 25 min

🎬 **CO MÓWIĘ:**

„OK — środowisko działa. Teraz potrzebuję Was poznać lepiej. Wiem z ankiety przed-szkoleniowej że jesteście Java developerami — seniorzy, tech leads, architekci. Ale doświadczenie z AI jest różne i chcę to usłyszeć bezpośrednio.

Maks 2-3 minuty na osobę. Cztery pytania — wklejam na chat:"

💬 WKLEJ NA CHAT:
```
Runda intro (2-3 min / osoba):
1) Rola + stack (Java stack, czy też frontend/DevOps?)
2) Jak dziś używasz AI? (narzędzia, jak często, do czego)
3) Największa frustracja lub rozczarowanie z AI
4) Co chcesz wynieść z tego tygodnia?
```

📺 **CO POKAZUJĘ:**
- Te cztery pytania na ekranie
- Notuję pain pointy uczestników — wracam do nich przez tydzień

🔵 **Dla zaawansowanych — pytanie otwierające dyskusję (wrzuć na chat przed rundą):**

💬 WKLEJ NA CHAT:
```
Pytanie otwarte — wrócimy do niego na koniec tygodnia:

"Czy 20 000 linii kodu w 3 dni to dla Ciebie nowa norma?
A może już jest? Co by to zmieniło w Twoim zespole?"
— inspiracja: Kubryński, DevTalk Trio
```

🎬 „To nie jest autoprezentacja. To jest szybki radar dla mnie — żebym wiedział gdzie zwolnić, gdzie przyspieszyć, i które przykłady będą dla Was najbardziej trafne.

*[Słuchaj aktywnie. Notuj konkretne problemy. Np.: 'halucynacje w Spring Boot', 'agent nadpisał mi konfigurację', 'nie wiem jak dawać mu kontekst'. Wróć do tych przykładów w ciągu dnia.]*

*[Po rundzie:]* Dziękuję. Słyszę że główne use case to ChatGPT do researchu i GitHub Copilot do autocomplete. Przez tydzień wyjdziemy znacznie dalej: zamiast kopiowania odpowiedzi z chata, agent będzie pracował bezpośrednio w Waszym repozytorium, pisał testy, robił code review, budował architekturę. Zobaczycie różnicę."

---

## 10:00–10:25 — Quiz kalibracyjny
⏱️ 25 min

🎬 **CO MÓWIĘ:**

„Teraz mały quiz. To nie egzamin — nikt nie ocenia. Celem jest żebym wiedział które pojęcia z materiałów pre-work wymagają więcej omówienia, a które możemy przeskoczyć.

Dostaliście przed szkoleniem materiały z podstawowymi pojęciami AI — zakładam, że część z Was przeczytała, część przejrzała, część... no, tutaj jesteśmy. Bez oceniania.

Zaznaczajcie proszę intuicyjnie — nie szukajcie w Google. Jeśli czegoś nie wiecie — super, właśnie po to tu jesteście."

💡 **Wskazówka:** Przeprowadź w rundce — zapytaj 2-3 osoby o każde pojęcie. Cel: wyczuć gdzie jest luka, nie sprawdzić wszystkich ze wszystkiego. Nie musisz przejść przez każde z 8 punktów — skup się na tych, gdzie był rozstrzał lub cisza.

💬 WKLEJ NA CHAT:
```
Quiz kalibracyjny — odpowiedz własnymi słowami:

1) Token — co to jest i dlaczego ma znaczenie dla kosztów?
2) Context Window — czym różni się od pamięci długoterminowej?
3) Hallucination — dlaczego LLM halucynuje z definicji?
4) Prompt Engineering vs Context Engineering — jaka różnica?
5) Agent vs Assistant — jaka fundamentalna różnica?
6) Sandbox w Claude Code — co izoluje i dlaczego ważne?
7) MCP (Model Context Protocol) — do czego służy?
8) Vibe Coding vs Vibe Engineering — jeden przykład różnicy?

Odpowiadajcie na chat lub głos — nie ma złych odpowiedzi!
```

🎬 *[Po zebraniu odpowiedzi — omów TYLKO pojęcia gdzie widzisz największy rozjazd. Nie rób pełnego wykładu — 1 zdanie + 1 przykład na pojęcie.]*

🎬 **DEFINICJE — użyj tylko tych które quiz wypadły słabo:**

„**Token** — to nie słowo. To fragment tekstu. 'programowanie' to 4-5 tokenów. Model myśli tokenami, nie słowami. Dlatego limity i koszty są w tokenach. W praktyce: długi plik źródłowy = dużo tokenów = wyższy koszt i ryzyko context rot.

**Context window** — tymczasowa pamięć robocza modelu. Wszystko co model 'widzi' podczas jednej sesji. Claude ma 200 tysięcy tokenów — to ok. gruba książka. Ale uwaga: więcej nie znaczy lepiej. Pojawia się 'context rot' — model gubi się w zbyt długim kontekście. Jakość i pozycja informacji są ważniejsze niż ilość.

**Halucynacja** — model generuje tekst który brzmi pewnie ale jest nieprawdziwy. Może wymyślić nieistniejącą bibliotekę, zły endpoint, niepoprawną sygnaturę metody. Dlatego weryfikujemy generated code — nie dlatego że AI jest głupie, ale że tak działają modele autoregresywne: przewidują kolejny token, nie sprawdzają faktów.

**Agent vs Assistant** — Assistant odpowiada na pytania. Agent działa autonomicznie: planuje, wykonuje komendy w terminalu, edytuje pliki, uruchamia testy, iteruje. Claude Code to agent. ChatGPT to assistant. Różnica: agent ma narzędzia i środowisko; assistant ma tylko chat.

**Context Engineering** — nie chodzi o to jak pytasz (to prompt engineering), ale co model widzi. CLAUDE.md, konkretne pliki, zakres zadania. Snajper, nie shotgun. W 2026 to jest ważniejsze niż prompt engineering.

**MCP** — Model Context Protocol. Standard od Anthropic (teraz Linux Foundation) pozwalający agentom używać zewnętrznych narzędzi: IntelliJ IDE, bazy danych, GitHub, Jira itp. Wrócimy do tego na Dzień 2."

---

## 10:25–11:15 — Moduł 1.1: The New Frontier
⏱️ 50 min

🎬 **CO MÓWIĘ:**

„OK, zaczynamy właściwy dzień.

Zanim uruchomimy agenta i zaczniemy pisać — potrzebuję żebyście zrozumieli *dlaczego*. Nie żebym Was przekonał do AI — wy już tu jesteście. Ale żebyście mieli właściwy mental model: co AI robi dobrze, co robi źle, i skąd to wszystko się bierze.

Zacznijmy od pytania: Czy AI Was zastąpi?

*[Pause, poczekaj na reakcje — 15 sekund ciszy to OK]*

Słyszę że odpowiedzi są różne. Prawda jest bardziej zniuansowana.

GitHub i Stanford zbadali w 2025 roku 120 tysięcy developerów na 14 platformach przez rok. Wiecie co wyszło?"

💬 WKLEJ NA CHAT:
```
Badania AI w programowaniu:

Stanford/GitHub (120K devs):
https://www.youtube.com/watch?v=JvosMkuNxF8

METR (07.2025 — doświadczeni devs, OS projekty):
https://metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/

SSRN (12.2025 — ukryte koszty AI):
https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5842302
```

🎬 „Produktywność w wielu przypadkach spadła. Doświadczeni developerzy przy złożonych, nieznajomych zadaniach byli *wolniejsi* z AI niż bez AI. W zadaniach prostych i dobrze znanych — AI przyspieszało.

Ale — to są badania z 2025 roku. W grudniu 2025 i lutym 2026 nastąpiły duże skoki jakości modeli i narzędzi. Więc te liczby są już trochę nieaktualne. Ale nie całkowicie.

Ważna konkluzja: AI nie jest magicznym turbo-doładowaniem. Jest narzędziem z charakterystyką użycia. Sprawdza się w konkretnych kontekstach i spowalnia w innych.

Jest też coś ważnego — **Productivity J-Curve**. Kiedy zaczynasz uczyć się nowego narzędzia, najpierw jesteś wolniejszy. Przez 1-2 miesiące możecie być mniej produktywni. Potem, jeśli nauczycie się tego dobrze, przyspieszacie.

Dlatego ten tydzień nie jest o natychmiastowym turbo. Jest o tym, żebyście wyszli stąd z właściwym workflow — takim który po kilku tygodniach praktyki realnie przyspieszy Waszą pracę.

Żeby nie było za pesymistycznie — są też przykłady naprawdę ekstremalnego przyspieszenia."

💬 WKLEJ NA CHAT:
```
Case study: Peter Steinberger + OpenClaw

- 1000–1500 commitów dziennie z agentami AI
- 50 równoległych agentów do triażu PR
- 220K gwiazdek na GitHubie w kilka tygodni
- Dołączył do OpenAI w lutym 2026

Artykuł: https://steipete.me/posts/2025/shipping-at-inference-speed
GitHub (wykres commitów): https://github.com/steipete
```

🎬 „Człowiek z Wiednia, iOS developer. Zaczął używać agentów AI w październiku 2025. 26 października — 1374 commity w jeden dzień. Nie pisał ich ręcznie — orkiestrował flotę agentów. Każdy agent w osobnym git worktree, każdy pracuje nad osobnym feature.

To nie jest typ który zniknie z rynku. To jest typ który wie jak korzystać z narzędzia.

I drugi przykład — bliżej dużych firm."

💬 WKLEJ NA CHAT:
```
Case study: Microsoft + AI

"Moim celem jest wyeliminowanie każdej linii C i C++
z Microsoftu do 2030 roku."
— Galen Hunt, Principal Software Engineer, CoreAI Microsoft

North Star: "1 inżynier, 1 miesiąc, 1 milion linii kodu"
Strategia: kombinacja AI + algorytmów do przepisywania codebase'ów.

LinkedIn: https://www.linkedin.com/posts/galenh_principal-software-engineer-coreai-microsoft-activity-7407863239289729024-WTzf
```

🎬 „I trzeci case study — celowo zostawiłem go na koniec, bo to jest z waszego podwórka.

Jakub Kubryński, Łukasz Szydło i Jakub Pilimon. Ekipa DNA — Droga Nowoczesnego Architekta. Jeśli jesteście w świecie Java enterprise — znacie ich. Na ich szkolenia czeka się z rocznym wyprzedzeniem.

Trzy tygodnie temu nagrali live: '6 mitów o AI w SDLC'.

Kubryński powiedział wprost: w jego firmie Skill Panel od ponad dwóch lat **100% kodu powstaje z pomocą AI. Żadna linijka nie jest pisana ręcznie przez programistę**.

Jak to możliwe bez chaosu? Bo mają dojrzały workflow. Spec-driven development, dobre CLAUDE.md, code review, testy. Nie 'vibe coding' — świadomy proces. Dokładnie to co budujemy przez ten tydzień.

Nie możecie im zarzucić braku doświadczenia. To są osoby które uczyły was jak robić architekturę porządnie."

💬 WKLEJ NA CHAT:
```
Case study PL: Kubryński, Szydło, Pilimon — Architekt Jutra

"6 mitów o AI w SDLC" (live, 2026):
https://www.youtube.com/watch?v=ioJkmqNFjwk

Skill Panel (Kubryński): 100% kodu z AI od >2 lat.
Podejście: spec-driven + Claude + code review.

Architekt Jutra: https://architektjutra.pl
```

🔵 **Dla zaawansowanych:** Obejrzyjcie live przed jutrem — szczególnie sekcję o micie "agentowi wystarczy dostęp do danych i narzędzi". Wrócimy do tego w kontekście CLAUDE.md na Dzień 2.

🎬 „A teraz wpływ na szerszy ekosystem. Co się dzieje z narzędziami które wygrywały przez *wizyty* programistów?"

💬 WKLEJ NA CHAT:
```
Realny wpływ AI na ekosystem IT:

- TailwindCSS: spadek ruchu na docs → problem z przychodami OSS
  https://www.youtube.com/watch?v=luhgjBrRulk

- StackOverflow: umierający
  https://www.youtube.com/watch?v=Gy0fp4Pab0g

- 60% wyszukiwań Google bez kliknięcia (AI search)

- Kursy video: Udemy i podobne tracą użytkowników
  https://www.youtube.com/watch?v=WCGTQBCE3FA
```

🎬 „TailwindCSS ma problem — programiści przestali odwiedzać dokumentację, bo pytają AI. A ich model biznesowy opierał się na ruchu na stronie docs. Pierwszy przykład jak AI pośrednio niszczy projekty open source.

StackOverflow. 20 lat byliśmy z nim. Teraz odpada.

60% wyszukiwań Google nie kończy się kliknięciem — AI odpowiada bezpośrednio.

Ale to nie znaczy że wystarczy tylko AI. Jakość i niezawodność nadal mają znaczenie. AI generuje dużo kodu, ale też dużo bałaganu. Dlatego mamy ten kurs — żebyście wiedzieli jak robić to dobrze.

Teraz — gdzie na tej skali jesteście Wy?"

💬 WKLEJ NA CHAT:
```
Poziomy AI-Assisted Programming:

0 – Manual       (piszesz wszystko ręcznie)
1 – Autocomplete (Copilot tab)
2 – Chat         (pytasz, kopiujesz odpowiedzi)
3 – Agent        (edytuje pliki, używa narzędzi, CLAUDE.md)
4 – Multi-Agent  (flota agentów, async, CI/CD)
5 – Dark Factory (w pełni autonomiczne; ludzie zarządzają specami)

Gdzie jesteś TERAZ? Gdzie chcesz być po tym kursie?
Wrzuć na chat: "jestem na X, chcę Y"
```

🎬 „'Dark Factory' — zapożyczone z produkcji. Fabryka która działa w ciemności bo nie ma tam ludzi. W softwearze: agenty shipują features 24/7, ludzie ustawiają kierunek i robią końcowe sprawdzenie jakości.

Nie twierdzę że Dark Factory jest dobre dla wszystkich. W środowisku bankowym, regulowanym — kontrola i audyt są kluczowe. Ale warto wiedzieć że ten kierunek istnieje, bo zmienia reguły gry.

Teraz — trzy nowe role w erze agentowej. Jako seniorzy i architekci — to Was dotyczy szczególnie."

💬 WKLEJ NA CHAT:
```
3 nowe role IT w erze AI-agentów:

1. ORCHESTRATOR
   Zarządza agentami, definiuje zadania, review i sterowanie.
   Pisze CLAUDE.md, PRD, zasady. "Nowy tech lead."

2. SYSTEM/INFRA BUILDER
   Buduje scaffolding: CI/CD pipelines, MCP servers,
   sandboxes, observability, kontrola kosztów.
   Deep tech rola wymagająca myślenia architektonicznego.

3. DOMAIN EXPERT AS PROGRAMMER
   Prawnik, analityk, lekarz który opisuje co chce
   i dostaje działający software.
   Nie wie że stał się programistą.
   Najbardziej disruptywna zmiana: nowa podaż "programistów"
   którzy nigdy nie napisali linii kodu.
```

🎬 „Ta trzecia rola jest najbardziej disrupcyjna. Zupełnie nowa podaż 'programistów' którzy nigdy nie napisali linii kodu. To zmienia rynek pracy — ale nie tak jak myślicie. Nie eliminuje programistów. Eliminuje barierę wejścia dla ekspertów domenowych.

I tu macie przewagę jako seniorzy: zrozumienie biznesu, architektury, bezpieczeństwa — tego AI nie zastąpi szybko. Ale zdolność do orkiestrowania agentów stanie się tak samo ważna jak umiejętność pisania kodu.

Pytanie: który z Was widzi siebie jako Orchestratora? To właśnie ten kurs."

🏋️ **MINI-ĆWICZENIE (3 min):**

💬 WKLEJ NA CHAT:
```
Mini-ćwiczenie:
Napisz na czacie jedno zdanie:
• Na którym poziomie (0–5) jesteś teraz
• Jaka rola (Orchestrator / Builder / Domain Expert)
  najbardziej pasuje do Twojego kontekstu pracy

Np: "Poziom 2, chcę być Orchestratorem dla naszego
     zespołu 8 devs, bo właśnie planujemy AI workflow"
```

---

## 11:15–11:30 — PRZERWA ☕
**Przypomnij przed przerwą:**

💬 WKLEJ NA CHAT:
```
☕ Przerwa 15 min → wracamy 11:30
Po przerwie: Modele AI, Prompty, Vibe Coding
+ demo LMArena i porównanie modeli open source
```

---

## 11:30–12:35 — Moduł 1.2: Modele, Prompty, Vibe Coding + Demo
⏱️ 65 min

🎬 **CO MÓWIĘ:**

„Zanim uruchomimy agenta — musimy zrozumieć z czym pracujemy. Jaka jest różnica między modelami, jak działa prompt, co to właściwie znaczy 'Vibe Coding', i — co mnie ostatnio bardzo wciąga — jak szybko rosną modele open source.

Zacznijmy od modeli."

💬 WKLEJ NA CHAT:
```
Główne modele LLM (marzec 2026):

Anthropic:  Claude Opus 4.6, Sonnet 4.6, Haiku 4.5
OpenAI:     o4, o3-mini, GPT-5.2
Google:     Gemini 2.5 Pro, Flash 2.0
Meta:       Llama 4 (open source)
Open source: GLM-5 (ZhipuAI), GLM-4.7, Minimax M2.1

Benchmarki do wyboru modelu:
- SWE-Bench (coding tasks): https://www.swebench.com/
- Terminal-Bench: https://www.tbench.ai/leaderboard/terminal-bench/2.0
- Tool Calling: https://gorilla.cs.berkeley.edu/leaderboard.html
- LMArena WebDev (głosowanie ludzi!): https://arena.ai/leaderboard/code
```

🎬 „Co jest praktycznie ważne:

**Opus 4.6** — najlepszy dla architektury i złożonych decyzji. Kosztowny.
**Sonnet 4.6** — nasza codzienna 'robocza' bestia. Dobry balans ceny i jakości.
**Haiku 4.5** — szybki i tani, dobry do prostych powtarzalnych tasków.

I kluczowa obserwacja z badań OpenAI:"

💬 WKLEJ NA CHAT:
```
Kluczowy insight z badań OpenAI (Engineering Blog):

"Ten sam model w różnych środowiskach:
 78% vs 42% na tym samym benchmarku.
 Różnica 36 punktów procentowych
 — z samego środowiska, nie modelu."

Źródło: https://openai.com/index/harness-engineering/

Wniosek: ŚRODOWISKO i WORKFLOW mają większe znaczenie
niż wybór modelu. Dlatego konfiguracja i CLAUDE.md są kluczowe.
```

🎬 „To jest może najważniejsza techniczna rzecz z tego modułu. Nie model decyduje o wyniku — decyduje środowisko w którym pracuje. Właśnie dlatego skupiamy się na workflow, nie na porównywaniu modeli.

Teraz — lecimy do LMArena. To jest narzędzie które naprawdę lubię bo jest inne od standardowych benchmarków."

📺 **CO POKAZUJĘ:**
- Otwieram https://arena.ai/leaderboard/code

🎬 „LMArena to leaderboard oparty o głosowanie ludzi, nie sztuczne benchmarki. Działa tak: wpisujesz prompt, dostajesz odpowiedzi od dwóch anonimowych modeli, głosujesz który jest lepszy. Na podstawie milionów takich głosowań budowany jest ranking ELO — taki jak w szachach.

Dlaczego to jest interesujące? Bo tu można znaleźć świeże modele zanim zostaną oficjalnie ogłoszone. Słyszałem o jednym modelu który przez kilka tygodni bił Opusa w bitwach 1:1 zanim Google się przyznało że to był Gemini Nano — pojawił się jako 'Banan' w eksperymentalnej arenie.

Pokażę Wam teraz konkretne demo. Zaraz zrobimy małe ćwiczenie."

🏋️ **ĆWICZENIE — LMArena (10 min):**

💬 WKLEJ NA CHAT:
```
Ćwiczenie: Vibe Coding Battle w LMArena

1. Wejdźcie na: https://arena.ai/leaderboard/code
2. Kliknijcie "Battle" (lub "Arena")
3. Wklejcie ten prompt:

---
Build a HTML website for Steve's PC Repair.
High-contrast dark mode + bold condensed headings +
animated ticker + chunky category chips + magnetic CTA.

Include the disclaimer that Steve will not work on Macs,
he hates them and their users, and include an origin story
as to why.
---

4. Porównajcie dwie wersje które dostaniecie
5. Zagłosujcie na lepszą — a POTEM zobaczcie które modele to były
6. Wrzućcie na chat co wybraliście i dlaczego
```

🎬 *[Poczekaj 5-7 min, potem omów wyniki]*

🎬 „Świetnie. Widzicie kilka rzeczy naraz:

Jeden — jak bardzo różnią się modele przy tym samym prompcie. Nawet jeśli oba 'działają', jakość UI, kreatywność, humor z disclaimerem Steve'a — to różni się diametralnie.

Dwa — to jest Vibe Coding w czystej postaci. Prompt po angielsku, zero technicznych detali, model samodzielnie decyduje o strukturze HTML, CSS, treści. I dla takiego przypadku — strona Steve'a, throwaway demo — to jest idealne zastosowanie.

Trzy — to jest też świetny przykład że wiedza o modelach przechodzi szybko. Modele które są na topie dzisiaj za pół roku mogą nie być.

Teraz pokażę Wam coś co mnie naprawdę imponuje w ostatnim czasie — wzrost open source modeli."

📺 **CO POKAZUJĘ:**
- Otwieram linki do GLM z kolejnymi wersjami

💬 WKLEJ NA CHAT:
```
Ewolucja modeli open source — ten sam prompt, 3 modele:

GLM-4.6 (starszy): https://chat.z.ai/c/9dc51018-2bc6-4fa1-b6d7-7b447f42a593
GLM-4.7 (nowszy):  https://chat.z.ai/c/d6cbcc65-2bc2-443a-8521-68662509d61e
GLM-5 (najnowszy): https://chat.z.ai/c/2d931c0f-6917-4877-9a94-72750222f5e4

Ten sam prompt co Steve's PC Repair — zobaczcie różnicę jakości.
GLM-5 zbliża się do Claude Opus w wielu zadaniach.

A cena? GLM Lite plan: ~3$/miesiąc
(3x większy usage limit niż Claude Pro za 20$)
```

🎬 „Patrzcie na te trzy wyniki. Między GLM 4.6 a GLM 5 jest przepaść jakościowa. A to ten sam dostawca, w ciągu kilku miesięcy. To jest tempo wzrostu modeli open source w 2025-2026.

Dlaczego to ważne dla Was? Bo za rok lub dwa, modele open source mogą być wystarczająco dobre żeby zastąpić chmurowe modele w wielu zadaniach — bez wysyłania kodu firmowego do Anthropic czy OpenAI. Dla środowisk regulowanych, bankowych — to jest ważna obserwacja.

Teraz — Prompt Engineering i Context Engineering."

💬 WKLEJ NA CHAT:
```
Prompt Engineering — fundamenty:

ZERO-SHOT:
"Napisz test jednostkowy dla metody calculateInterest()"

FEW-SHOT:
"Napisz test jednostkowy.
Przykład dobrego testu u nas: [przykład]
Teraz napisz dla: calculateInterest()"

CHAIN-OF-THOUGHT:
"Pomyśl krok po kroku zanim odpiszesz.
Co robi ta metoda? Co można przetestować?
Jakie edge cases są ważne w kontekście bankowym?"

ROLE + KONTEKST:
"Jesteś doświadczonym Java developerem pracującym
z Spring Boot 3.2 w środowisku bankowym z wysokimi
wymaganiami bezpieczeństwa. Napisz..."
```

🎬 „Ale prompt engineering w 2026 roku to już nie to co w 2023. Jest ważny — ale nie najważniejszy. Teraz kluczowe jest **Context Engineering**."

💬 WKLEJ NA CHAT:
```
Prompt Engineering vs Context Engineering:

PROMPT ENGINEERING — jak formułujesz zapytanie:
• zero-shot, few-shot, chain-of-thought, role prompting
• techniki budowania pojedynczego zapytania
• = JAK rozmawiasz z modelem

CONTEXT ENGINEERING — co wkładasz do okna kontekstowego:
• CLAUDE.md — stały kontekst projektu dla agenta
• tylko to co potrzebne i relevantne (snajper, nie shotgun)
• unikanie "context rot" — za dużo śmieci = model się gubi
• = CO model widzi i z czym pracuje

W 2026: Context Engineering > Prompt Engineering

Artykuł Anthropic:
https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents
```

🎬 „Wyobraźcie sobie dwóch snajperów. Jeden ma krótki i precyzyjny brief — strzela w punkt. Drugi dostał 50 plików, historię projektu od 3 lat i paste ze StackOverflow. Który trafi?

Context Engineering to bycie tym pierwszym snajperem. To jest główna praca inżynierów budujących narzędzia jak Claude Code — jak dobrze dostarczyć modelowi kontekst.

Pilimon z ekipy Architekta Jutra mówił o tym wprost: 'Agentowi wystarczy dać dostęp do danych i narzędzi' — to jest jeden z największych mitów. Bez opisu projektu, konwencji, wcześniejszych decyzji i ograniczeń — agent strzela. Pewnie, płynnie, z przekonaniem — ale strzela. CLAUDE.md to wasze antidotum na tę entropię.

Wrócimy do tego praktycznie, kiedy będziemy tworzyć CLAUDE.md dla projektu.

Ale najpierw — pułapka, na którą wpadają wszyscy przy dłuższej pracy z agentem: **Context Rot**."

💬 WKLEJ NA CHAT:
```
Context Rot — pułapka długich sesji:

Co to: model gubi się w bardzo długim kontekście.
"Lost in the Middle" — instrukcje z początku sesji
bywają ignorowane po 50+ wiadomościach.

Objawy:
• agent "zapomina" o wcześniejszych ustaleniach
• zaczyna robić rzeczy których zakazaliście
• wyniki gorsze mimo że nic nie zmieniliście

Rozwiązania:
• /compact w Claude Code — kompresuje historię sesji
• nowa sesja + CLAUDE.md — agent odbudowuje kontekst
  z pliku, nie z historii
• commituj regularnie → możesz wrócić do dobrego stanu
• małe zadania > jedna mega-sesja

Zasada: wolę 5 czystych sesji niż 1 mega-sesję.
```

🎬 „CLAUDE.md to nasz główny mechanizm przeciw context rot. Agent czyta go na początku każdej sesji — więc nawet przy nowej sesji, od razu wie o projekcie. Do tego wrócimy szczegółowo na Dzień 2.

Teraz — Vibe Coding vs Vibe Engineering. To jest serce tego kursu."

💬 WKLEJ NA CHAT:
```
Vibe Coding (Andrej Karpathy, 2025):
https://x.com/karpathy/status/1886192184808149383

"Fully give in to the vibes, embrace exponentials,
forget that the code even exists."

KIEDY OK:
✅ jednorazowy skrypt do obróbki danych
✅ throwaway prototyp UI do demonstracji
✅ nauka nowej technologii
✅ strona Steve'a z PC Repair ;)

KIEDY NIE:
❌ kod produkcyjny bankowy
❌ logika autoryzacji i uwierzytelniania
❌ przetwarzanie danych osobowych (RODO)
❌ cokolwiek co przechodzi przez audit
❌ legacy system który ktoś będzie utrzymywać
```

💬 WKLEJ NA CHAT:
```
Vibe Engineering — 4 filary:

1. WERYFIKACJA
   Czytasz i rozumiesz co AI wygenerowało.
   Nie każdą linię — ale architekturę i krytyczną logikę.
   Podpisujesz swoim imieniem.

2. SYSTEM WALIDACJI
   Dajesz AI narzędzia do samoweryfikacji:
   testy które agent sam uruchamia, linter, CI/CD.
   "Napisz kod, uruchom testy, napraw błędy, commit."

3. PRECYZYJNY KONTEKST
   CLAUDE.md, konkretne pliki, zakres zadania.
   Snajper, nie shotgun.

4. KONTROLA SCOPE
   Jasne granice co agent może, a czego nie.
   Sandbox, approvals, dedykowany branch.
```

🎬 „Inaczej mówiąc: w Vibe Engineering budujesz system. AI nie jest luzem puszczonym na produkcję. Agent może zrobić błąd — ale system to wyłapie zanim dojdzie do maina.

Szydło z ekipy Architekta Jutra powiedział o tym świetnie: 'AI to nie junior developer któremu możesz zaufać po 6 miesiącach. To ekspert który może napisać 1000 linii idealnego kodu i jedną linię która zniszczy cały system — i nawet nie mrugnął okiem.' Dlatego weryfikacja jest konieczna — nie dlatego że AI jest głupie, ale dlatego że tak działają systemy probabilistyczne.

To jest różnica między juniorem który 'pisze kod' a seniorem który 'projektuje system wytwarzania kodu'. I to jest właśnie wasza przewaga.

Jeszcze krótko — CLAUDE.md. To jest context engineering w praktyce, będziemy go tworzyć dzisiaj po południu."

💬 WKLEJ NA CHAT:
```
CLAUDE.md — plik w repozytorium który mówi agentowi:
• co to jest za projekt
• jak pracować (stack, konwencje, styl)
• jakie zasady bezpieczeństwa
• co wolno, a czego nie

Standard: https://agents.md/
Artykuł Anthropic: https://claude.com/blog/using-claude-md-files

Przykład (Microsoft, Java Spring Boot):
https://github.com/microsoft/mcp-for-beginners/blob/main/AGENTS.md
```

🏋️ **MINI-ĆWICZENIE (5 min):**

💬 WKLEJ NA CHAT:
```
Mini-ćwiczenie: Prompt improvement

Popraw ten prompt tak żeby nadawał się do agenta:
"Napisz kod do obsługi reklamacji"

Dodaj:
1) rolę i kontekst techniczny (Java, Spring Boot, bank)
2) konkretny scope i zasady
3) ograniczenia bezpieczeństwa
4) format outputu

Wrzućcie swoje wersje na chat — porównamy 2-3 przykłady.
```

🎬 *[Omów 2-3 wersje. Pokaż jak różne są wyniki.]*

---

## 12:35–13:00 — Moduł 1.3 (część 1): Mission Briefing
⏱️ 25 min

🎬 **CO MÓWIĘ:**

„Teraz Mission Briefing — co będziemy budować przez ten tydzień.

Projekt tygodnia: **AI Chat do weryfikacji reklamacji na podstawie zdjęcia**.

Jak to działa: użytkownik wgrywa zdjęcie uszkodzonego produktu, AI analizuje uszkodzenie przy pomocy modelu vision, system weryfikuje reklamację i zwraca decyzję z uzasadnieniem.

Backend w Java Spring Boot 3.2, Java 21, integracja z modelem vision (Claude Haiku vision — tani, szybki, dobry do analizy obrazów). Prosty frontend — może być thymeleaf lub cokolwiek prostego.

Dlaczego ten projekt? Bo dotyka wszystkich etapów SDLC które będziemy przerabiać: wymagania, design, kod, testy, deploy. I bo jest realistyczny — ten typ systemu naprawdę istnieje w bankach i ubezpieczalniach."

📺 **CO POKAZUJĘ:**
- Diagram high-level architektury (narysuj na tablicy lub pokaż gotowy):

💬 WKLEJ NA CHAT:
```
Projekt tygodnia: AI Claim Validator

FLOW:
Użytkownik → [upload zdjęcie + opis] →
  Spring Boot API →
    [Claude Haiku Vision — analiza obrazu] →
      [Decyzja: zatwierdzić / odrzucić / eskalować] →
        [Odpowiedź z uzasadnieniem]

STACK:
• Java 21 + Spring Boot 3.2
• Spring AI (integracja z modelami LLM)
• REST API + prosty frontend
• OpenRouter API Key (dostarczone przez trenera)
• Git — nowe repo od dzisiaj

CEL NA KONIEC TYGODNIA:
• Działający PoC
• Testy jednostkowe i integracyjne
• Podstawowy CI/CD workflow
```

🎬 „Kilka ważnych zasad projektu:

Jeden — aplikacja jest PRETEKSTEM. Główny cel to nauczyć się workflow z AI, nie zbudować idealny system.

Dwa — zbudujemy ją prostą. Bez RAG, bez vector DB — te rzeczy są na osobny kurs. Prosta integracja REST z vision modelem.

Trzy — każdy pracuje na własnym repozytorium. Możecie adaptować projekt do swojego kontekstu — jeśli ktoś myśli 'u nas to byłoby dla ubezpieczeń' albo 'dla logistyki' — super, adaptujcie.

Cztery — OpenRouter API key dostaniecie ode mnie. To klucz do używania modeli w waszej aplikacji. Do samego Claude Code używacie swojej subskrypcji Team.

Zróbmy teraz repo i pierwsze pliki — ale najpierw obiad."

---

## 13:00–13:30 — PRZERWA OBIAD 🍽️

💬 WKLEJ NA CHAT:
```
🍽️ Przerwa obiadowa 30 min → wracamy 13:30

Po przerwie:
• Logowanie do Claude Code CLI
• Claude Code Desktop App — kiedy używać
• Pierwsze komendy i pierwsze zadanie
• CLAUDE.md dla naszego projektu

Zostawcie otwarty terminal + ulubiony edytor.
```

---

## 13:30–14:30 — Moduł 1.3 (część 2): Setup Claude Code + pierwsze komendy
⏱️ 60 min

🎬 **CO MÓWIĘ:**

„OK — wracamy. Teraz naprawdę wchodzimy w Claude Code. Będziemy to robić razem, krok po kroku. Jeśli coś nie działa — mówcie od razu.

Najpierw — kto jest już zalogowany do `claude`? Kciuki."

*[Sprawdź kto ma problem i szybko pomóż — do 5 min]*

🎬 „Świetnie. Zanim zaczniemy pisać — chcę Wam pokazać DWA tryby pracy z Claude Code, bo to jest ważna decyzja którą będziecie podejmować przy każdym zadaniu.

Mamy Claude Code CLI — w terminalu. I Claude Code Desktop App — aplikacja z GUI.

Które jest lepsze? Odpowiedź jak zawsze: zależy."

💬 WKLEJ NA CHAT:
```
Claude Code CLI vs Desktop App:

CLI (terminal):
✅ Pełna moc — bash, skrypty, pipe'y, automatyzacja
✅ Multi-agent i git worktrees
✅ Integracja z IDE przez ACP (o tym za chwilę!)
✅ Lepszy do długich zadań i CI/CD
✅ Konfiguracja przez CLAUDE.md, MCP w pliku
⚠️ Tylko tekst w terminalu

Desktop App:
✅ Wygodniejszy UI — foldery, drag & drop plików
✅ Analiza codebase przez GUI
✅ Łatwiejszy start dla nowych użytkowników
✅ Sandbox przez Hyper-V (Windows Pro/Enterprise)
⚠️ Wymaga Windows Pro/Enterprise (nie Home!)
⚠️ Mniej elastyczny niż CLI

Rekomendacja: CLI jest głównym narzędziem.
Desktop App jako uzupełnienie do eksploracji i analizy.
```

🎬 „Zanim przejdziemy do komend — ważna kwestia bezpieczeństwa: **sandbox**. Szczególnie ważna dla środowisk regulowanych jak wasze.

Co to jest sandbox w kontekście agenta? To izolacja środowiska w którym agent uruchamia komendy. Bez sandboxa — agent ma dostęp do wszystkich plików na dysku, może uruchamiać dowolne procesy. Z sandboxem — jest ograniczony do katalogu projektu i nie może przypadkowo (ani złośliwie) wyjść poza."

💬 WKLEJ NA CHAT:
```
Sandbox — Claude Code CLI vs Desktop App:

CLI na Windows (bez WSL2):
⚠️ BRAK pełnego sandbox — AppContainer ogranicza sieć,
   ale agent ma dostęp do plików poza projektem!
   → Używajcie na projektach bez wrażliwych credentiali
   → Lub: osobne konto OS / dedykowana maszyna

CLI na Windows (z WSL2):
✅ bubblewrap (bwrap) — izolacja przez nowe namespace'y Linuxa
   → włącz: claude --sandbox

Desktop App (Windows Pro/Enterprise):
✅ Hyper-V VM — najlepsza izolacja, pełna separacja
   → Działa tylko na Windows Pro/Enterprise, nie Home!

Kiedy szczególnie ważne:
❗ Projekty z kluczami API, credentialami, danymi klientów
❗ Środowiska bankowe / regulowane
❗ YOLO mode (--dangerously-skip-permissions) — TYLKO w VM/kontenerze!

Rekomendacja dla Was:
• Desktop App jeśli macie Windows Pro → najlepszy sandbox
• CLI + WSL2 jeśli zostajecie w terminalu
• W CI/CD: zawsze w dedykowanym kontenerze Docker
```

🎬 „W środowisku bankowym, gdzie pracujecie — sandbox nie jest opcją, jest wymaganiem. Zapamiętajcie tę zasadę: im wyższy poziom autonomii agenta, tym ważniejsza izolacja.

Wrócimy do tego praktycznie na Dzień 4 przy CI/CD. Na razie zapamiętajcie: Desktop App = bezpieczniejszy start na Windowsie jeśli macie Pro.

Teraz — zacznijmy od CLI, bo to jest fundament.

Stwórzcie katalog projektu i zainicjujcie repo:"

💬 WKLEJ NA CHAT:
```
Inicjalizacja projektu:

mkdir ai-claim-validator
cd ai-claim-validator
git init
git branch -M main

Uruchom Claude Code:
claude
```

🎬 „Kiedy Claude Code się uruchamia — zobaczcie kilka rzeczy na ekranie. Pokażę Wam najważniejsze komendy."

💬 WKLEJ NA CHAT:
```
Claude Code CLI — kluczowe komendy:

# Uruchomienie:
claude                   ← interaktywny chat
claude "zadanie"         ← od razu z taskiem
claude --help            ← pełna lista opcji

# W sesji (zacznij od /):
/help                    ← lista wszystkich komend
/model                   ← zmień model (opus/sonnet/haiku)
/cost                    ← ile wydałeś w tej sesji
/clear                   ← wyczyść kontekst (nowa sesja)
/compact                 ← skompresuj kontekst (gdy długi)
/memory                  ← zarządzaj CLAUDE.md
/config                  ← ustawienia

# Shell commands (! prefix):
!ls -la                  ← uruchom komendę shell
!git status              ← git bez wychodzenia

# Nowa linia bez wysyłania:
Shift+Enter              ← nowa linia (WezTerm, nowoczesne terminale)
\ + Enter                ← nowa linia (Windows Terminal / PowerShell)

# Anulowanie:
Escape                   ← soft cancel (dokończ bieżący krok)
Ctrl+C                   ← hard cancel (zatrzymaj wszystko)
```

🎬 „Teraz najważniejsza komenda na dzisiaj — `/model`. Macie subskrypcję Team, więc macie dostęp do wszystkich modeli. Domyślnie Claude Code używa Sonnet — to dobry balans. Dla złożonych zadań architektonicznych możecie przestawić na Opus. Dla szybkich i tanich zadań — Haiku.

Sprawdźcie teraz aktualny model:"

💬 WKLEJ NA CHAT:
```
W sesji claude — sprawdź i zmień model:
/model

Dostępne modele:
• claude-opus-4-6    ← najlepszy, dla architektury
• claude-sonnet-4-6  ← domyślny, codzienna praca  ✅
• claude-haiku-4-5   ← szybki i tani, proste taski

Sprawdź koszty sesji:
/cost
```

🎬 „Teraz pierwsze zadanie — zainicjujemy strukturę projektu Spring Boot.

Ale najpierw — stworzymy CLAUDE.md. To jest plik który będzie 'briefem' dla agenta przez cały tydzień. Bez niego agent nie wie nic o naszym projekcie i musi to tłumaczyć przy każdej sesji."

💬 WKLEJ NA CHAT:
```
Prompt do Claude Code — stwórz CLAUDE.md:

(wklej w sesji claude, użyj Shift+Enter dla nowych linii)

Stwórz plik CLAUDE.md dla projektu AI Claim Validator.

Projekt: aplikacja webowa do weryfikacji reklamacji
na podstawie zdjęcia uszkodzonego produktu.

Stack techniczny:
- Java 21
- Spring Boot 3.2
- Spring AI (integracja z LLM)
- Maven
- REST API
- Środowisko: bankowe/regulowane

Zasady pracy agenta:
- Pisz testy dla nowej logiki biznesowej
- Nie zmieniaj konfiguracji bezpieczeństwa bez aprobaty
- Commit messages po angielsku, conventional commits
- Zawsze waliduj input od użytkownika
- Nie hardcoduj kluczy API — używaj zmiennych środowiskowych
- Przed każdą dużą zmianą — zadaj mi pytania jeśli masz wątpliwości

Format: użyj sekcji: Project Overview, Tech Stack,
Development Guidelines, Security Requirements.
```

🎬 *[Poczekaj aż Claude wygeneruje CLAUDE.md. Przejrzyj razem z uczestnikami.]*

🎬 „Dobra. Teraz struktura projektu Spring Boot:"

💬 WKLEJ NA CHAT:
```
Prompt do Claude Code — struktura projektu:

Stwórz strukturę projektu Java Spring Boot 3.2
dla aplikacji AI Claim Validator opisanej w CLAUDE.md.

Potrzebuję:
1. Standardowa struktura Maven (src/main/java, src/test/java)
2. pom.xml z zależnościami: spring-boot-starter-web,
   spring-ai-openai-spring-boot-starter, spring-boot-starter-validation
3. Główna klasa aplikacji
4. Placeholder controller: ClaimController z endpointem
   POST /api/claims/validate (przyjmuje multipart/form-data: zdjęcie + opis)
5. README.md z opisem projektu i instrukcją uruchomienia

Tylko struktura i konfiguracja — bez implementacji logiki AI.
Skomentuj placeholder'y jako TODO.
```

🎬 *[Pokaż co generuje Claude. Przejdź przez pliki razem z uczestnikami. Sprawdź czy się kompiluje: `!mvn compile`]*

🏋️ **ĆWICZENIE 1 — Pierwsze zadanie w Claude Code (10 min):**

💬 WKLEJ NA CHAT:
```
Ćwiczenie 1: Samodzielna praca z Claude Code

Macie strukturę projektu. Teraz dodajcie do niego:

1. Model danych — ClaimRequest:
   - imageUrl (String)
   - description (String)
   - claimType (enum: DAMAGE, MISSING_ITEM, WRONG_ITEM)

2. Model odpowiedzi — ClaimResponse:
   - decision (enum: APPROVED, REJECTED, ESCALATED)
   - confidence (double, 0.0-1.0)
   - reasoning (String)
   - timestamp (LocalDateTime)

Użyjcie Claude Code — napiszcie prompt który to wygeneruje.
Przetestujcie: !mvn compile
Wyniki wrzućcie na chat: co działało, co wymagało korekty?
```

🎬 *[Chodź po uczestnikach (lub sprawdzaj przez screen share). Obserwuj podejście — kto daje precyzyjne prompty, kto zbyt ogólne. Daj feedback.]*

---

## 14:30–14:40 — OPCJONALNA PRZERWA ☕

💬 WKLEJ NA CHAT:
```
☕ Opcjonalna przerwa 10 min

Po przerwie:
• Moduł 1.4: Narzędzia — porównanie CLI vs Desktop vs IntelliJ
• Nowość: Claude Code w IntelliJ przez ACP Registry
• Jak wybrać właściwe narzędzie dla właściwego zadania
```

---

## 14:40–15:35 — Moduł 1.4: Narzędzia — CLI, Desktop App, IntelliJ ACP
⏱️ 55 min

🎬 **CO MÓWIĘ:**

„Teraz — narzędzia. Ten moduł odpowiada na pytanie które dostałem przed szkoleniem: 'Czy kurs to tylko nauka jednego narzędzia?' Odpowiedź: skupiamy się na Claude Code, ale pokażę Wam jak to narzędzie integruje się z Waszymi obecnymi narzędziami — IntelliJ, IDE, terminal — i dlaczego to podejście jest lepsze niż skakanie po 5 różnych agentach.

Zacznijmy od Claude Code Desktop App — bo to jest coś co warto znać nawet jeśli głównie używacie CLI."

📺 **CO POKAZUJĘ:**
- Otwieram Claude Code Desktop App

🎬 „Desktop App wygląda zupełnie inaczej niż CLI. Mamy tu GUI — możemy przeciągać pliki, przeglądać katalogi, widzieć historię sesji. Kilka rzeczy które robię tu chętnie zamiast w CLI:"

💬 WKLEJ NA CHAT:
```
Claude Code Desktop App — kiedy używam:

✅ Analiza dużego nieznanego codebase
   (drag & drop repo do okna, "wyjaśnij mi ten projekt")

✅ Eksploracja struktury plików przez GUI
   (łatwiej niż w terminalu dla nowych projektów)

✅ Kiedy chcę izolację przez Hyper-V VM
   (bezpieczniejszy sandbox — ale wymaga Windows Pro!)

✅ Kiedy pokazuję coś osobom mniej oswojonym z terminalem

NIE używam Desktop App gdy:
❌ Chcę użyć bash pipe'ów i skryptów
❌ Pracuję w CI/CD lub automatyzacji
❌ Chcę integrację z IDE przez ACP
❌ Mam Windows Home (nie działa — wymaga Hyper-V)
```

🎬 „Ważna uwaga o Desktop App i sandboxie na Windows: Desktop App zawsze uruchamia Hyper-V VM jako sandbox. To świetna izolacja — ale wymaga Windows Pro lub Enterprise. Na Windows Home nie zadziała. Sprawdźcie jaką macie wersję.

CLI na Windows nie używa Hyper-V — używa AppContainer, który jest lżejszy.

Teraz — największa nowość z ostatniego tygodnia. Integracja Claude Code z IntelliJ przez ACP."

📺 **CO POKAZUJĘ:**
- Otwieram IntelliJ IDEA
- AI Assistant panel

💬 WKLEJ NA CHAT:
```
Nowość (marzec 2026): Claude Code w IntelliJ przez ACP

ACP = Agent Client Protocol
— otwarty standard (JetBrains + Zed)
— działa jak LSP ale dla agentów AI
— jeden agent, wiele IDE

Co to daje:
✅ Claude Code CLI pracuje WEWNĄTRZ IntelliJ AI Chat
✅ Wspólna sesja, historia, configs z CLI
✅ Bez osobnej subskrypcji JetBrains AI!
✅ Przekazuje IntelliJ MCP server → agent widzi Twój kod w IDE
✅ Dostępne od IntelliJ 2025.3+

Zainstaluj z ACP Registry:
IntelliJ AI Chat → dropdown → "Install From ACP Registry"
→ wybierz "Claude Code" → OK
```

📺 **CO POKAZUJĘ:**
- Demo: AI Chat w IntelliJ → dropdown → ACP Registry → Claude Code
- Jak wygląda rozmowa z Claude Code Agent przez IntelliJ

🎬 „To jest dużo lepsza opcja niż stara integracja Anthropic SDK w AI Assistant — tamta wymagała subskrypcji JetBrains AI i oddzielnego klucza API, i nie miała wspólnej sesji z CLI.

Teraz: logujesz się raz do `claude` w terminalu, i ta sama sesja, te same configs, ten sam CLAUDE.md jest dostępny przez IntelliJ AI Chat. Jedno narzędzie, dwa interfejsy — terminal i IDE.

Pokażę Wam jak to skonfigurować:"

💬 WKLEJ NA CHAT:
```
Konfiguracja Claude Code w IntelliJ przez ACP:

Opcja 1 — z ACP Registry (najłatwiejsza):
1. Otwórzcie AI Chat (View → Tool Windows → AI Assistant)
2. Kliknijcie dropdown przy nazwie modelu
3. "Install From ACP Registry"
4. Znajdźcie "Claude Code" → zainstalujcie
5. Opcjonalnie: zaznaczcie "Pass IntelliJ MCP server"
   (agent będzie widział kod przez IDE, nie tylko przez pliki)

Opcja 2 — ręczna konfiguracja (jeśli Registry nie działa):
Stwórzcie: ~/.jetbrains/acp.json

{
  "agent_servers": {
    "Claude Code": {
      "command": "npx",
      "args": ["@zed-industries/claude-code-acp"],
      "env": {},
      "use_idea_mcp": true
    }
  }
}

Wymaga: npm install -g @zed-industries/claude-code-acp
```

🎬 „'Pass IntelliJ MCP server' — to jest ważna opcja. Gdy ją włączycie, agent dostaje dostęp do IntelliJ MCP server, czyli może 'widzieć' Wasz kod przez IDE — wraz z diagnostyką, błędami kompilacji, inspekcjami. To jest dużo lepszy kontekst niż samo czytanie plików.

A teraz szybkie pytanie: wiecie co to jest MCP? Powiedzieliśmy o tym w quizie. Właśnie zobaczyliście go w praktyce — IntelliJ MCP server to serwer który wystawia narzędzia IDE dla agenta AI. Wrócę do MCP szczegółowo na Dzień 2.

Teraz — porównanie wszystkich opcji pracy z Claude Code dla Java developera w IntelliJ:"

💬 WKLEJ NA CHAT:
```
Claude Code dla Java dev w IntelliJ — opcje:

1. CLI w terminalu (wbudowany lub zewnętrzny)
   Pros: pełna moc, bash, automatyzacja
   Cons: brak IDE context (chyba że MCP)

2. ACP agent w IntelliJ AI Chat  ← REKOMENDOWANE
   Pros: IDE context, jedna subskrypcja, wspólna sesja z CLI
   Cons: wymaga IntelliJ 2025.3+

3. Claude Code [Beta] plugin (osobny)
   Pros: inline suggestions w edytorze
   Cons: inny zakres funkcji niż ACP agent, osobna konfiguracja
   Uwaga: to NIE jest to samo co ACP agent!

4. Claude Code Desktop App
   Pros: GUI, analiza codebase, dobry do eksploracji
   Cons: brak integracji z IDE workflow, wymaga Win Pro
```

🎬 „Moja rekomendacja dla Was: używajcie ACP agent w IntelliJ AI Chat do codziennej pracy z projektem — bo macie IDE context. CLI w terminalu do automatyzacji, skryptów, git worktrees, i zaawansowanych zadań.

A teraz — chwila o GitHub Copilot. Wspomnę o nim bo dostałem to pytanie przed szkoleniem."

💬 WKLEJ NA CHAT:
```
GitHub Copilot vs Claude Code — krótkie porównanie:

GitHub Copilot:
✅ Najlepsza integracja z GitHub (PR reviews, issues, Actions)
✅ Świetny autocomplete (IntelliJ, VSCode)
✅ Claude Opus 4.5 dostępny w planach!
⚠️ Pro+: $39/m dla 1500 premium requests

Claude Code:
✅ Lepszy agent do złożonych wieloplikowych zadań
✅ Max plan: $100/m ale ogromne limity
✅ Bardziej zaawansowane MCP, sub-agents, Skills
✅ Wasz Team plan — macie już dostęp

Praktyczne: Claude Code jako główny agent,
GitHub Copilot warto mieć do integracji z GitHub
jeśli to jest Wasz główny CI/CD hub.
```

🏋️ **ĆWICZENIE 2 — ACP w IntelliJ (10 min):**

💬 WKLEJ NA CHAT:
```
Ćwiczenie 2: Podłącz Claude Code do IntelliJ

1. Otwórzcie IntelliJ IDEA (wersja 2025.3+)
2. Sprawdźcie wersję: Help → About
   (jeśli niższa niż 2025.3 — zostańcie przy CLI, to OK)

3. AI Chat → dropdown → Install From ACP Registry → Claude Code

4. Przetestujcie: wklejcie w AI Chat:
   "Przejrzyj obecny projekt i powiedz mi co to za aplikacja,
    jaki stack techniczny i jakie endpointy API są dostępne."

5. Porównajcie wyniki z tym samym pytaniem w CLI:
   (w terminalu: claude "Przejrzyj obecny projekt...")

Podzielcie się: czy IDE context robi różnicę?
```

🎬 *[Omów różnice w odpowiedziach — z IDE context agent powinien mieć lepsze info o projekcie]*

🎬 „Zanim przejdziemy dalej — szybka lista materiałów które warto znać jeśli chcecie głębiej w narzędzia:"

💬 WKLEJ NA CHAT:
```
Inne narzędzia AI do programowania (dla ciekawych):

CLI agenci:
• Gemini CLI (open source, darmowy dla Google AI Pro)
• Codex CLI (OpenAI, wymaga ChatGPT Plus)
• OpenCode (open source, open standard)
• Goose by Block (open source, Mac/Linux)

IDE:
• Cursor (najlepszy autocomplete, RAG na docs)
• Zed (open source, Rust, ACP native)
• GitHub Copilot (najlepsza integracja z GitHub)

Filozofia: wszystkie te narzędzia robią to samo inaczej.
Wiedza z Claude Code przenosi się 1:1.
CLAUDE.md → AGENTS.md (ten sam format!)
MCP → ten sam standard
Sub-agents → wszędzie podobny mechanizm
```

---

## 15:35–15:50 — WezTerm mini-moduł
⏱️ 15 min

🎬 **CO MÓWIĘ:**

„Ostatni techniczny temat dnia — WezTerm. To nie jest obowiązkowy element kursu. Wasz obecny terminal nadal działa. Ale chcę pokazać dlaczego warto go rozważyć przy dłuższej pracy z agentami.

Trzy konkretne powody:"

💬 WKLEJ NA CHAT:
```
WezTerm vs Windows Terminal — kiedy warto zmienić:

✅ WezTerm lepszy dla pracy agentowej:
1. Shift+Enter = nowa linia
   (Windows Terminal: musisz pisać \+Enter — frustrujące przy długich promptach)
2. Powiadomienia gdy agent skończy zadanie
   (Windows Terminal: brak — musisz patrzeć na terminal)
3. Stabilna praca z TUI przy długich sesjach
4. Ctrl+Backspace = usuń całe słowo
5. Splity paneli: kod / agent / logi równocześnie
6. Cross-platform (Windows/macOS/Linux) — ten sam config

⚠️ Zostań przy obecnym terminalu jeśli:
• Działa stabilnie i nie masz problemów z sesjami
• Nie chcesz kolejnej konfiguracji teraz

Oficjalne docs Claude Code o terminalach: https://code.claude.com/docs/en/terminal-config
Instalacja: https://wezfurlong.org/wezterm/installation.html
```

📺 **CO POKAZUJĘ:**
- Szybkie demo: split paneli (jeden panel: agent, drugi: kod, trzeci: logi)
- Shift+Enter dla nowej linii w długim prompcie
- Powiadomienie po zakończeniu długiego zadania

🎬 „Mam dla Was gotowe pliki konfiguracyjne — `.wezterm.lua` z moimi ustawieniami i motywem Omarchy który widzicie u mnie na ekranie. Są w materiałach szkoleniowych."

💬 WKLEJ NA CHAT:
```
WezTerm — gotowe konfiguracje z kursu:
• .wezterm.lua — pełna konfiguracja z splitami i skrótami
• omarchy.lua — motyw kolorystyczny
Lokalizacja: course-materials/WezTerm/

WezTerm — przydatne skróty:
Shift+Enter        = nowa linia bez wysyłania prompta
Ctrl+Backspace     = usuń całe słowo w lewo
Ctrl+Shift+D       = podziel panel pionowo
Ctrl+Shift+E       = podziel panel poziomo
Ctrl+Shift+Arrow   = przeskocz między panelami
```

---

## 🔵 Dla zaawansowanych: Ollama + przełączanie backendów Claude Code
⏱️ opcjonalne, równolegle z WezTerm lub jako praca domowa

💡 **Uwaga:** Ten blok tylko dla tych którzy już mają WezTerm gotowy lub nie potrzebują go. Reszta może poczekać do podsumowania.

🎬 „Dla tych którzy są już gotowi — pokażę jeszcze jedną rzecz: jak uruchomić Claude Code z alternatywnym backendem. Czyli zamiast Anthropic API — OpenRouter, GLM przez Z.ai albo lokalne modele przez Ollama. Dlaczego to ważne? Bo w środowiskach gdzie nie możecie wysyłać kodu firmowego do Anthropic — możecie użyć lokalnego modelu który zostaje w Waszej infrastrukturze.

Mam gotowy plik `.bashrc` z funkcjami bash do szybkiego przełączania między backendami."

💬 WKLEJ NA CHAT:
```
Przełączanie backendów Claude Code — funkcje bash:

Dostępne aliasy (z pliku .bashrc w materiałach szkoleniowych):

ccn  = claude-native   → Anthropic (Wasz Team plan, domyślny)
cco  = claude-or       → OpenRouter (dostęp do 200+ modeli)
ccz  = claude-zai      → Z.ai / GLM-4.7 (tani, mocny open source)
ccol = claude-ollama   → Ollama (lokalny model, zero danych w chmurze!)

Przykłady użycia:
cco                          → Claude Code przez OpenRouter
CLAUDE_CODE_MODEL="deepseek/deepseek-v3.2" cco   → konkretny model
ccz                          → Claude Code z GLM-4.7
ccol                         → Claude Code z lokalnym Ollama

Plik .bashrc z kursu: course-materials/.bashrc
(skopiujcie zawartość do swojego ~/.bashrc lub ~/.zshrc)
```

🎬 „Jak działa to pod spodem? Claude Code używa zmiennych środowiskowych `ANTHROPIC_BASE_URL` i `ANTHROPIC_AUTH_TOKEN` do komunikacji z API. Te funkcje po prostu ustawiają inne wartości tych zmiennych przed uruchomieniem claude — i agent trafia do innego backendu, ale interfejs CLI zostaje taki sam.

**Ollama** — to najciekawszy przypadek dla środowisk regulowanych. Instalujecie Ollama lokalnie, pobieracie model (np. `ollama pull qwen3-coder`), uruchamiacie `ollama serve`, i wtedy `ccol` połączy Claude Code z tym modelem. Żaden token kodu nie opuszcza Waszej maszyny."

💬 WKLEJ NA CHAT:
```
🔵 Ollama — szybki start (dla zaawansowanych):

Instalacja Ollama:
winget install Ollama.Ollama   # Windows
# lub: https://ollama.com/

Pobierz model do kodowania:
ollama pull qwen3-coder        # dobry coding model
ollama pull glm-5:cloud        # GLM-5 przez Ollama cloud endpoint

Uruchom jako lokalny serwer:
ollama serve
→ dostępny na: http://localhost:11434

Uruchom Claude Code z Ollama backendem:
ccol                           # użyj aliasu z .bashrc

Sprawdź aktywny model w Claude Code:
/status
```

🎬 „Jedna ważna uwaga: lokalne modele przez Ollama są na razie słabsze od chmurowych przy złożonym kodowaniu. Świetne do: prostych tasków, korekty tekstu, local CI na słabo połączonej maszynie, i testowania co możemy zrobić lokalnie. Do architektury i trudnych zadań — wciąż warto sięgnąć po Opus lub Sonnet."

---

## 15:50–16:00 — Podsumowanie dnia
⏱️ 10 min

🎬 **CO MÓWIĘ:**

„Dobra — zamykamy dzień pierwszy. Zrobiliśmy dziś naprawdę dużo. Podsumujmy."

💬 WKLEJ NA CHAT:
```
✅ Co mamy po Dniu 1:

□ Działające środowisko lokalne (Claude Code, Java, Git)
□ Claude Code CLI — zalogowany, pierwsze komendy
□ Sandbox — wiemy kiedy i jak używać (CLI vs Desktop)
□ Claude Code Desktop App — wiemy kiedy używać
□ Claude Code w IntelliJ przez ACP (dla tych z 2025.3+)
□ Projekt ai-claim-validator — struktura Spring Boot
□ CLAUDE.md — kontekst dla agentów przez cały tydzień
□ Modele danych: ClaimRequest, ClaimResponse
□ Wspólny mental model: Vibe Engineering nie Vibe Coding
□ 🔵 Ollama + .bashrc — backend switchers (dla zaawansowanych)

Jutro (Dzień 2): Od pomysłu do projektu
Temat: AI jako Twój PM i UX Designer
→ PRD, architektura, diagramy, wireframes, wybór stack'u
```

🎬 „Co jutro robimy?

Rano zacznijemy od wymagań — jak AI pomaga w tworzeniu PRD i user stories. Potem architektura — diagramy, ADR (Architecture Decision Record). Następnie MCP praktycznie — Context7 do dokumentacji bibliotek, IntelliJ MCP server, sub-agenci.

Nie ma obowiązkowej pracy domowej. Ale jeśli chcecie:"

💬 WKLEJ NA CHAT:
```
Opcjonalne do jutra (bez presji):

□ Uzupełnij CLAUDE.md jeśli nie było czasu
□ Upewnij się że projekt kompiluje się: mvn compile
□ Jeśli IntelliJ niżej niż 2025.3 — zaktualizuj
□ Zainstaluj WezTerm jeśli chcesz przetestować

Pytania wieczorem? Wrzucajcie — odpiszę.
```

🎬 „Jedno pytanie na koniec do Was: co z dzisiaj najbardziej Wam się przyda jutro w pracy — jedno zdanie na chat.

*[Poczekaj na odpowiedzi, skomentuj krótko 2-3]*

Świetnie. Do jutra!"

💬 WKLEJ NA CHAT:
```
Dzień 1 — koniec! 🎯
Materiały: w repo szkoleniowym
Jutro: 09:00 — Dzień 2 — Od pomysłu do projektu
Do jutra! 👋
```

---

## APPENDIX A — Linki (wszystko w jednym miejscu)

| Co | Link |
|---|---|
| Claude Code docs | https://code.claude.com/docs/en/quickstart |
| Claude Code Desktop | https://code.claude.com/docs/en/desktop |
| Claude Code sandboxing | https://code.claude.com/docs/en/sandboxing |
| Context engineering (Anthropic) | https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents |
| CLAUDE.md / AGENTS.md standard | https://agents.md/ |
| CLAUDE.md blog Anthropic | https://claude.com/blog/using-claude-md-files |
| JetBrains ACP docs | https://www.jetbrains.com/help/ai-assistant/acp.html |
| JetBrains ACP Registry blog | https://blog.jetbrains.com/ai/2026/01/acp-agent-registry/ |
| Introducing Claude Agent in JetBrains | https://blog.jetbrains.com/ai/2025/09/introducing-claude-agent-in-jetbrains-ides/ |
| SWE-Bench leaderboard | https://www.swebench.com/ |
| Terminal Bench | https://www.tbench.ai/leaderboard/terminal-bench/2.0 |
| LMArena WebDev/Code | https://arena.ai/leaderboard/code |
| Tool Calling benchmark | https://gorilla.cs.berkeley.edu/leaderboard.html |
| Stanford AI dev study (YT) | https://www.youtube.com/watch?v=JvosMkuNxF8 |
| METR badanie 07.2025 | https://metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/ |
| Hidden costs AI (SSRN) | https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5842302 |
| OpenClaw case study | https://steipete.me/posts/2025/shipping-at-inference-speed |
| Vibe Coding def. (Karpathy) | https://x.com/karpathy/status/1886192184808149383 |
| GLM porównanie 4.6 | https://chat.z.ai/c/9dc51018-2bc6-4fa1-b6d7-7b447f42a593 |
| GLM porównanie 4.7 | https://chat.z.ai/c/d6cbcc65-2bc2-443a-8521-68662509d61e |
| GLM porównanie 5 | https://chat.z.ai/c/2d931c0f-6917-4877-9a94-72750222f5e4 |
| WezTerm instalacja | https://wezfurlong.org/wezterm/installation.html |
| Handy STT (voice input) | https://handy.computer/ |
| OpenRouter | https://openrouter.ai/ |
| Spring AI docs | https://docs.spring.io/spring-ai/reference/index.html |
| MCP standard (intro) | https://modelcontextprotocol.io/docs/getting-started/intro |
| Harness engineering (OpenAI) | https://openai.com/index/harness-engineering/ |
| Prompting techniques | https://www.promptingguide.ai/techniques |

---

## APPENDIX B — Prompty gotowe do skopiowania

### Prompt: stwórz CLAUDE.md
```
Stwórz plik CLAUDE.md dla projektu AI Claim Validator.

Projekt: aplikacja webowa do weryfikacji reklamacji
na podstawie zdjęcia uszkodzonego produktu.

Stack techniczny:
- Java 21, Spring Boot 3.2, Spring AI, Maven
- REST API, środowisko bankowe/regulowane

Zasady pracy agenta:
- Pisz testy dla nowej logiki biznesowej
- Nie zmieniaj konfiguracji bezpieczeństwa bez aprobaty
- Commit messages po angielsku, conventional commits
- Zawsze waliduj input od użytkownika
- Nie hardcoduj kluczy API
- Przed każdą dużą zmianą — zadaj mi pytania

Format: sekcje: Project Overview, Tech Stack,
Development Guidelines, Security Requirements.
```

### Prompt: struktura Spring Boot
```
Stwórz strukturę projektu Java Spring Boot 3.2
dla aplikacji AI Claim Validator opisanej w CLAUDE.md.

Potrzebuję:
1. Standardowa struktura Maven
2. pom.xml z: spring-boot-starter-web,
   spring-ai-openai-spring-boot-starter,
   spring-boot-starter-validation
3. Główna klasa aplikacji
4. ClaimController z endpointem POST /api/claims/validate
   (multipart/form-data: zdjęcie + opis)
5. README.md z opisem i instrukcją uruchomienia

Tylko struktura — bez implementacji logiki AI.
Skomentuj placeholder'y jako TODO.
```

### Prompt: modele danych
```
Dodaj modele danych do projektu:

ClaimRequest:
- imageUrl (String, URL lub base64)
- description (String, opis reklamacji)
- claimType (enum: DAMAGE, MISSING_ITEM, WRONG_ITEM)

ClaimResponse:
- decision (enum: APPROVED, REJECTED, ESCALATED)
- confidence (double, 0.0-1.0)
- reasoning (String, uzasadnienie decyzji)
- timestamp (LocalDateTime)

Dodaj walidację Bean Validation (@NotNull, @NotBlank itp.)
Wygeneruj też prosty test jednostkowy sprawdzający
czy walidacja działa poprawnie.
```

### Prompt: vibe coding demo (Steve's PC Repair)
```
Build a HTML website for Steve's PC Repair.
High-contrast dark mode + bold condensed headings +
animated ticker + chunky category chips + magnetic CTA.

Include the disclaimer that Steve will not work on Macs,
he hates them and their users, and include an origin story
as to why.
```

---

## APPENDIX C — Troubleshooting / najczęstsze problemy

| Problem | Rozwiązanie |
|---|---|
| `claude --version` nie działa | `npm install -g @anthropic-ai/claude-code` → sprawdź PATH |
| `claude` nie otwiera przeglądarki | Spróbuj `claude --api-key YOUR_KEY` lub skopiuj URL ręcznie |
| Claude Code Desktop wymaga Hyper-V | Tylko Windows Pro/Enterprise. Na Home — używaj CLI |
| ACP Registry nie widać w IntelliJ | Sprawdź wersję: wymaga 2025.3+. Help → About |
| `mvn compile` nie działa | Sprawdź JAVA_HOME: `java --version`, `echo $JAVA_HOME` |
| Model nie odpowiada / timeout | `/model` → zmień na `haiku` (szybszy), lub `/clear` i zacznij nową sesję |
| CLAUDE.md ignorowany przez agenta | Plik musi być w root katalogu projektu gdzie uruchamiasz `claude` |
| ACP agent nie widzi projektu | Włącz "Pass IntelliJ MCP server" w konfiguracji ACP |
| Windows Terminal: Shift+Enter nie działa | Znane ograniczenie — użyj `\` + Enter albo przełącz na WezTerm |

---

## APPENDIX D — IntelliJ ACP: szczegóły konfiguracji

```
Wymagania:
• IntelliJ IDEA 2025.3 lub nowszy
• Node.js zainstalowany (dla @zed-industries/claude-code-acp)
• Claude Code CLI zainstalowany i zalogowany

Instalacja z Registry:
1. AI Chat → dropdown (menu przy nazwie modelu)
2. "Install From ACP Registry"
3. Znajdź "Claude Code" → kliknij Install
4. Opcje MCP:
   ✅ "Pass IntelliJ MCP server" — agent widzi diagnostykę IDE
   ✅ "Pass custom MCP servers" — agent używa Twoich MCP serwerów
5. OK → restart AI Chat jeśli potrzeba

Weryfikacja działania:
W AI Chat wpisz: "Wymień wszystkie klasy Java w bieżącym projekcie"
Agent powinien odpowiedzieć na podstawie kodu w IDE, nie tylko plików.

Różnica vs Claude Code [Beta] plugin:
• ACP agent = pełny agent (jak CLI) wewnątrz IDE
• Claude Code [Beta] plugin = inline suggestions w edytorze (inna funkcja)
• Można używać obu — one się uzupełniają, nie wykluczają
```

---

## APPENDIX E — Sandbox: Claude Code CLI vs Desktop App

```
Claude Code CLI na Windows:
→ AppContainer (restricted access token)
→ Windows Filtering Platform (WFP) blokuje sieć
→ Nie wymaga Hyper-V
→ Działa na Home/Pro/Enterprise

Claude Code Desktop App na Windows:
→ Hyper-V VM — pełna izolacja
→ Wymaga Windows Pro/Enterprise/Education
→ Na Home edition: nie działa

Claude Code CLI na Linux:
→ bubblewrap (bwrap) — nowe namespace'y
→ Pełna izolacja gdy włączone (opt-in)
→ Sandbox OFF domyślnie — trzeba włączyć!

Praktycznie:
• CLI: używajcie na projektach bez wrażliwych credentiali
  lub włączcie sandbox: claude --sandbox
• Desktop App: najlepsza izolacja jeśli macie Win Pro+
• W CI/CD: uruchamiajcie agenta w dedykowanym kontenerze Docker
```

---

## APPENDIX F — Plan B: jeśli mocno za wolno (timing)

Jeśli quiz + runda zajęły za dużo i jesteś 20-30 min do tyłu:

| Co przyciąć | Jak |
|---|---|
| Moduł 1.1 | Pomiń case study Microsoft, skróć poziomy 0-5 do 2 min |
| GLM demo | Pokaż tylko wynik GLM-5, bez przeprowadzania ćwiczenia |
| LMArena | Pokaż sam, bez ćwiczenia uczestników (5 min zamiast 10) |
| Context Rot | Skróć do 1 bloku chat, bez omawiania |
| IntelliJ ACP | Pokaż demo, instalacja jako homework 🔵 |
| Pierwsze komendy | Pomijamy CLAUDE.md generation — robimy jutro rano jako rozgrzewkę |

**Minimum must-have na koniec Dnia 1:**
1. Claude Code CLI zainstalowany i zalogowany u wszystkich
2. Uczestnicy rozumieją agent vs assistant i poziomy 0-5
3. Uczestnicy wiedzą czym jest context rot i jak używać `/compact`
4. Katalog projektu z CLAUDE.md zainicjowany
5. Znają różnicę Vibe Coding vs Vibe Engineering

---

## APPENDIX G — FAQ dla prowadzącego

**Q: Czy musimy używać Claude Code czy możemy Cursor / Copilot?**
Na kursie skupiamy się na CC żeby nie tracić czasu na różnice między narzędziami. Wszystko się przenosi — AGENTS.md, MCP, sub-agents to te same koncepcje wszędzie. CC jest naszym referencyjnym przykładem.

**Q: Czy Claude Code zbiera nasz kod / dane firmowe?**
Anthropic nie trenuje modeli na danych API bez zgody (domyślnie wyłączone dla Pro/Team). Dla wrażliwego kodu produkcyjnego: sprawdźcie Enterprise tier lub modele lokalne przez Ollama.

**Q: Czym różni się Claude Code Web (claude.ai/code) od CLI?**
Web to cloud agent — działa w przeglądarce, ma własne środowisko w chmurze. Dobry do asynchronicznych zadań. CLI działa lokalnie, ma pełny dostęp do repo i jest lepszy do interaktywnej pracy.

**Q: Jak CC działa z dużym projektem Spring / monorepo?**
Nie otwierajcie całego repo naraz. Używajcie `@src/main/java/com/firma/modul/` zamiast `@src/`. Dobry CLAUDE.md z opisem architektury zastępuje konieczność ładowania całego kodu. To temat Dnia 5 (legacy code).

**Q: Czy możemy używać Codex zamiast Claude?**
Pokażę Codex porównawczo ostatniego dnia. Na szkoleniu jeden workflow — żeby nie było zamieszania. Wiedza przenosi się 1:1.
