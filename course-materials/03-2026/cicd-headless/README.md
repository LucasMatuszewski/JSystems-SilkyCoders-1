# CI/CD, Headless Mode, Jenkins, Jira, Confluence

Ten folder jest dodatkiem do dnia 4 i dnia 5. Zawiera:
- przewodnik jak realnie używać Claude Code w CI/CD,
- przykłady workflowów GitHub Actions,
- przykłady Jenkinsfile,
- skrypty do Bitbucket, Jira i Confluence,
- przykład konfiguracji Atlassian MCP do pracy z Jira i Confluence.

## Najważniejsze wnioski po analizie źródeł

### 1. “Headless mode” to dziś w praktyce `claude -p`

Oficjalna dokumentacja Claude nazywa to obecnie programmatic usage, a nie “headless mode”, ale komenda pozostaje ta sama:

```bash
claude -p "your prompt"
```

Źródła:
- Claude docs: `https://code.claude.com/docs/en/headless`
- SFEIR reference: `https://institute.sfeir.com/en/claude-code/claude-code-headless-mode-and-ci-cd/command-reference/`

### 2. Dla CI najbezpieczniejszy wzorzec to:

1. checkout kodu,
2. przygotowanie diffów, logów albo wyników testów,
3. uruchomienie `claude -p`,
4. wynik w `json`,
5. dalsze akcje zwykłym skryptem: komentarz do PR, task w Jira, update Confluence, PR z poprawkami.

### 3. `--bare` jest bardzo dobre do CI, ale nie zawsze

`--bare` wyłącza auto-load:
- CLAUDE.md,
- hooks,
- skills,
- plugins,
- MCP,
- auto memory.

To jest świetne dla:
- review diffów,
- generowania raportów,
- regularnych audytów konfiguracji.

To nie jest dobre, jeśli pipeline ma korzystać z:
- repozytoryjnego `.mcp.json`,
- pluginów,
- MCP do Jira i Confluence,
- custom skills.

Wtedy uruchamiaj bez `--bare`, ale tylko w kontrolowanym runnerze.

Źródło:
- Claude docs: `https://code.claude.com/docs/en/headless`

### 4. Atlassian MCP: do pracy interactive/local jest super, do CI trzeba uważać

Najważniejsze ustalenia:
- stare SSE/OAuth flow było problematyczne do automatyzacji,
- w artykule Builder jest ważna aktualizacja: do machine-to-machine lepsze jest API token auth,
- nowy endpoint to `/v1/mcp`,
- jeśli potrzebujesz stabilnej automatyzacji w CI, REST API albo MCP z API tokenem jest lepsze niż stare OAuth.

Źródła:
- Builder.io: `https://www.builder.io/blog/claude-code-with-jira`

### 5. Jenkins + Jira: build/deployment metadata najlepiej robić oficjalnym pluginem Atlassian

Do tego służą:
- `jiraSendBuildInfo()`
- `jiraSendDeploymentInfo(...)`

To nie jest komentarz do ticketu ani transition workflow. To jest przesyłanie build i deployment telemetry do Jira.

Jeśli chcesz:
- dodać komentarz do issue,
- przejść ticket do `In Review`,
- opublikować release notes w Confluence,

to najprościej robić to osobnym skryptem przez REST API.

Źródła:
- Atlassian article: `https://www.atlassian.com/blog/it-teams/how-we-built-the-jenkins-to-jira-cloud-integration-and-what-you-can-do-with-it`
- Jenkins plugin docs: `https://plugins.jenkins.io/atlassian-jira-software-cloud/`

### 6. “Claude by comment” jak GitHub Copilot?

Stan praktyczny:
- GitHub Copilot ma oficjalne flow `@copilot` w PR commentach,
- dla Claude nie znalazłem publicznie udokumentowanego, oficjalnego odpowiednika `@claude` w PR comment -> start cloud coding session -> push fix,
- da się to emulować przez GitHub Actions, Jenkins, webhook i `claude -p`,
- da się też pushować eventy do otwartej sesji przez Channels, ale to nie jest to samo co nowa cloud sesja CI/CD.

Wniosek:
- na GitHubie natywny review-by-comment jest dziś lepiej dopracowany przez Copilota,
- dla Claude najlepiej budować własny workflow automatyzacji.

Źródła:
- GitHub Copilot docs
- Claude channels docs: `https://code.claude.com/docs/en/channels`

### 7. `/schedule` i cotygodniowe utrzymanie CLAUDE.md, rules i skills

Tak, to warto robić.

Oficjalne fakty:
- `/loop` jest session-scoped i znika po zamknięciu sesji,
- docs wprost mówią, że dla trwałego schedulingu trzeba używać:
  - Cloud scheduled tasks,
  - Desktop scheduled tasks,
  - albo GitHub Actions.
- Cloud scheduled tasks działają w chmurze, bez otwartej lokalnej sesji, na świeżym clone repo.

Ale:
- publiczna dokumentacja, którą zweryfikowałem, nie opisuje szczegółowo centralnego, admin-managed org-wide flow typu “admin ustawia jeden weekly task dla wszystkich repo i wszystkich userów”.
- dlatego jako wzorzec zespołowy polecam GitHub Actions `schedule`.
- Cloud scheduled tasks potraktuj jako świetną opcję dla konkretnego repo i zadania, ale nie będę udawał, że mam publicznie zweryfikowaną instrukcję na org-wide admin rollout.

Źródło:
- Claude docs: `https://code.claude.com/docs/en/scheduled-tasks`

## Kiedy używać czego

### Wariant A — bez MCP, czysty CI/CD

Najlepszy do:
- review PR diffów,
- audytów jakości,
- generowania changelogów,
- tygodniowego przeglądu konfiguracji agentów.

Przykłady:
- [github-actions/claude-pr-review.yml](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\github-actions\claude-pr-review.yml)
- [github-actions/claude-weekly-config-curation.yml](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\github-actions\claude-weekly-config-curation.yml)
- [scripts/claude_pr_review.sh](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\claude_pr_review.sh)
- [scripts/weekly_claude_config_audit.sh](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\weekly_claude_config_audit.sh)

### Wariant B — CI/CD + Jira/Confluence przez REST API

Najlepszy do:
- dodawania komentarzy do Jira,
- transition ticketu,
- publikacji release notes do Confluence,
- niezawodnej automatyzacji machine-to-machine.

Przykłady:
- [scripts/jira_comment_and_transition.py](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\jira_comment_and_transition.py)
- [scripts/confluence_upsert_page.py](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\confluence_upsert_page.py)
- [jenkins/Jenkinsfile.build-deploy-jira](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\jenkins\Jenkinsfile.build-deploy-jira)

### Wariant C — Jira/Confluence przez Atlassian MCP

Najlepszy do:
- interactive development,
- planowania i implementacji ticketów w terminalu,
- pobierania kontekstu z Jira i Confluence przed kodowaniem.

Dobre także do automatyzacji, ale tylko jeśli:
- używasz nowego `/v1/mcp`,
- używasz API token auth,
- kontrolujesz konfigurację runnera,
- nie używasz `--bare`.

Przykład:
- [mcp/atlassian-rovo-mcp.json.example](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\mcp\atlassian-rovo-mcp.json.example)

## Gotowe materiały

### GitHub Actions

- [github-actions/claude-pr-review.yml](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\github-actions\claude-pr-review.yml)
- [github-actions/claude-weekly-config-curation.yml](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\github-actions\claude-weekly-config-curation.yml)

### Jenkins

- [jenkins/Jenkinsfile.claude-bitbucket-pr-review](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\jenkins\Jenkinsfile.claude-bitbucket-pr-review)
- [jenkins/Jenkinsfile.build-deploy-jira](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\jenkins\Jenkinsfile.build-deploy-jira)

### Scripts

- [scripts/claude_pr_review.sh](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\claude_pr_review.sh)
- [scripts/weekly_claude_config_audit.sh](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\weekly_claude_config_audit.sh)
- [scripts/bitbucket_post_pr_comment.py](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\bitbucket_post_pr_comment.py)
- [scripts/jira_comment_and_transition.py](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\jira_comment_and_transition.py)
- [scripts/confluence_upsert_page.py](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\scripts\confluence_upsert_page.py)

### MCP

- [mcp/atlassian-rovo-mcp.json.example](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\mcp\atlassian-rovo-mcp.json.example)

### Cloud schedule prompt

- [cloud-schedule/weekly-claude-config-curation-prompt.md](D:\DEV\COURSES\JSystems-SilkyCoders-1\course-materials\03-2026\cicd-headless\cloud-schedule\weekly-claude-config-curation-prompt.md)

## Minimalne wymagania do przykładów

### GitHub Actions

- secret `ANTHROPIC_API_KEY`
- możliwość pisania komentarzy do PR
- brak fork-secrets exposure albo odpowiednie zabezpieczenie workflow

### Jenkins + Bitbucket

- agent z `git`, `node`, `npm`, `python3`
- credential:
  - `anthropic-api-key`
  - `bitbucket-api-credentials` jako `usernamePassword`
- webhook albo multibranch PR build z `CHANGE_ID`, `CHANGE_TARGET`

### Jenkins + Jira i Confluence

- Jenkins plugin `Atlassian Jira Software Cloud`
- Jira app `Jenkins for Jira`
- credential:
  - `jira-cloud-api-user` jako `usernamePassword`
  - `confluence-api-user` jako `usernamePassword`

## Źródła przeanalizowane przed przygotowaniem materiałów

- SFEIR command reference:
  `https://institute.sfeir.com/en/claude-code/claude-code-headless-mode-and-ci-cd/command-reference/`
- SFEIR tutorial:
  `https://institute.sfeir.com/en/claude-code/claude-code-headless-mode-and-ci-cd/tutorial/`
- SFEIR common mistakes:
  `https://institute.sfeir.com/en/claude-code/claude-code-headless-mode-and-ci-cd/errors/`
- SFEIR examples:
  `https://institute.sfeir.com/en/claude-code/claude-code-headless-mode-and-ci-cd/examples/`
- Angelo Lima:
  `https://angelo-lima.fr/en/claude-code-cicd-headless-en/`
- Medium Jenkins + Bitbucket:
  `https://medium.com/@sayan.nandi/building-an-ai-powered-automated-code-reviewer-with-claude-code-jenkins-and-bitbucket-b0e600d27b25`
- Reddit discussion:
  `https://www.reddit.com/r/ClaudeCode/comments/1rpweoy/using_jira_mcp_with_claude_code_completely/`
- Builder Jira + MCP:
  `https://www.builder.io/blog/claude-code-with-jira`
- Atlassian Jenkins ↔ Jira:
  `https://www.atlassian.com/blog/it-teams/how-we-built-the-jenkins-to-jira-cloud-integration-and-what-you-can-do-with-it`
- Official Claude docs:
  `https://code.claude.com/docs/en/headless`
  `https://code.claude.com/docs/en/scheduled-tasks`
  `https://code.claude.com/docs/en/channels`
- Atlassian / Bitbucket / Confluence API docs:
  `https://plugins.jenkins.io/atlassian-jira-software-cloud/`
  `https://developer.atlassian.com/cloud/bitbucket/rest/api-group-pullrequests/`
  `https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/`
  `https://developer.atlassian.com/cloud/confluence/rest/v2/api-group-page/`
