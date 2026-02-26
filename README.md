# AI w Programowaniu: Od Pomysłu do MVP
### Szkolenie JSystems dla Silky Coders

---

**Prowadzący:** [Łukasz Matuszewski](https://devpowers.com/) | [JSystems](https://jsystems.pl)

**Opis szkolenia:** [AI dla Programistów — Od Pomysłu do MVP](https://jsystems.pl/szkolenia-ai;ai_dla_programistow_od_pomyslu_do_mvp.szczegoly)

---

## O repozytorium

To repozytorium zawiera materiały do kursu **AI w Programowaniu** prowadzonego przez JSystems dla grupy Silky Coders.

### Gałęzie (Branches)

| Branch | Opis |
|--------|------|
| `master` | **Punkt startowy** — bazowa konfiguracja projektu Java/Spring Boot z Maven. Użyj tej gałęzi jako startu do własnych ćwiczeń. |
| `lucas-ralph-wiggum` | Implementacja techniki **Ralph Wiggum Bash Loop** — metoda zapobiegania context rot przy długo działających agentach. Zawiera adaptację dla Gemini CLI (oryginał w `docs/how-to-ralph-wiggum` jest dedykowany dla Claude Code). |

> **Inne gałęzie** zawierają wersje aplikacji budowane w trakcie kursu.

---

## Materiały kursu

Główne notatki i zasoby kursu znajdziesz w folderze `/docs`:

- 📓 [**Course Notes — AI in Programming**](./docs/Course%20Notes%20-%20AI%20in%20Programming.md) — Główne notatki kursu: trendy, narzędzia, benchmarki, metodologie agentic coding, case studies (OpenClaw, Microsoft), best practices i wiele więcej.

- Więcej materiałów (podsumowania dni, specjalistyczne tematy) znajdziesz w folderze [`/docs`](./docs).

---

## Technologie

- **Java 17** + **Spring Boot** + **Spring AI**
- **Maven** (wrapper `./mvnw`)
- **H2** (lokalna baza danych, plik na dysku via R2DBC)
- **OpenAI API** (GPT-4o, Codex)

### Uruchomienie

```sh
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./mvnw spring-boot:run
```

### Testy

```sh
./mvnw test
```

---

## Kontakt

- **JSystems:** [jsystems.pl](https://jsystems.pl)
- **Prowadzący:** [Łukasz Matuszewski](https://devpowers.com/)
