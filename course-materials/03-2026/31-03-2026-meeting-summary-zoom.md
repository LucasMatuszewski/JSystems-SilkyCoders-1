## Szybkie podsumowanie

Spotkanie koncentrowało się na omówieniu zagadnień związanych z testowaniem i integracją procesów przy użyciu agentów. JSystems poprowadził dyskusję na temat rozwiązywania problemów z planami testów i testami integracyjnymi, dzieląc się doświadczeniami z nieprawidłowymi blokami myślowymi oraz udzielając wskazówek dotyczących radzenia sobie z błędami sesji. Zespół zbadał sposoby prawidłowej konfiguracji testów, w tym wykorzystanie rzeczywistych danych zamiast danych symulowanych, oraz omówił znaczenie posiadania odpowiednich środowisk testowych odzwierciedlających ustawienia produkcyjne. Kamil zadał pytania dotyczące wdrożenia testów end-to-end w mikrousługach, sugerując inne podejście, w którym agent przeprowadzałby scenariusze testowe w wielu usługach. Grupa omówiła również wykorzystanie linii statusu i różnych narzędzi, takich jak GitLab Actions do automatyzacji, chociaż niektórzy uczestnicy wyrazili obawy dotyczące znaczenia konkretnych przykładów dla ich obecnych środowisk. Rozmowa zakończyła się planami kontynuacji dyskusji po przerwie, skupiając się na testach integracyjnych i procesach backendowych.

## Kolejne kroki

- JSystems: Sprawdź, dlaczego agenci przestali się pojawiać/działać poprawnie (działali wczoraj, ale nie dziś
- JSystems: Sprawdź, co spowodowało aktualizację/zmianę wpływającą na funkcjonalność agenta
- JSystems: Sprawdź w porze lunchu, dlaczego system przestał prawidłowo odczytywać nazwy agentów
- JSystems: Zbadaj problem ze znakiem dolara w nazwach agentów
- Wszyscy uczestnicy: Skonfiguruj zmienną planu w ustawieniach projektu wskazując folder planów (np. Dokumenty/plany
- Wszyscy uczestnicy: Dodaj plik ustawień niestandardowych (senior.plk.ouda) do głównego poziomu projektu w celu zarządzania zadaniami
- Wszyscy uczestnicy: Przenieś istniejące plany do folderu głównego, zmień ich nazwy i umieść je w folderze Doxplans
- Wszyscy uczestnicy: Skonfiguruj zmienną obszaru zadań w swoich projektach, aby ćwiczyć sesje ciągłe

## Podsumowanie

### Debugowanie aplikacji i rozwiązywanie problemów

Zespół omówił problemy z aplikacją, która działała sporadycznie, a niektórzy członkowie mieli problemy z rozpoznawaniem i reagowaniem agentów. JSystems zademonstrował, jak obsługiwać i debugować takie problemy, w tym przenosić pliki planu do odpowiedniej struktury katalogów oraz używać określonych zmiennych środowiskowych do kontrolowania zarządzania zadaniami. Dyskusja obejmowała kroki rozwiązywania problemów w przypadku, gdy agenci nie mogą przetwarzać lub gdy sesje są blokowane, a JSystems zaleca tworzenie odpowiedniej dokumentacji i używanie identyfikatora listy zadań w celu zachowania spójności między różnymi sesjami.

### Zarządzanie listą zadań i sesjami

Zespół omówił, jak obsługiwać listy zadań i zarządzanie sesjami w ich przepływie pracy. JSystems wyjaśnił, że w przypadku wystąpienia błędów lub przerwania sesji zadania można odzyskać, otwierając nową sesję i używając zmiennej CLAUDE_CODE_TASK_LIST_ID do przywrócenia listy zadań z poprzedniej sesji. Zademonstrowali, jak ręcznie skonfigurować tę zmienną i nawigować po historii zadań, w tym funkcje takie jak sesje kolorowania i zmiana ich nazw dla lepszej organizacji. Dyskusja dotyczyła również wykorzystania linii statusu do wizualnego śledzenia postępów pracy, a JSystems zapewnia linki do dokumentacji w celu uzyskania dalszych informacji.

### Konfiguracja i testowanie linii statusu

Spotkanie koncentrowało się na omówieniu konfiguracji linii statusu i podejść testowych. JSystems zademonstrował, jak tworzyć i dostosowywać linie statusu przy użyciu plików bash oraz wyjaśnił proces ich zapisywania w katalogu konfiguracyjnym użytkownika. Zespół omówił testy integracyjne i implementację CI/CD, a JSystems planuje pokazać przykłady ręcznej implementacji oraz konfiguracji Git Actions. Kamil i Tomasz wyrazili zainteresowanie bardziej koncepcyjnym podejściem niż konkretnymi szczegółami platformy, szczególnie w odniesieniu do porównań GitHub Actions i GitLab. Dyskusja zakończyła się planami kontynuowania po obiedzie kolejnych przykładów testowych i demonstracji przepływu pracy CI/CD.

### Demonstracja wyzwalaczy automatyzacji JSystems

JSystems zademonstrował różne wyzwalacze automatyzacji i opcje uruchamiania działań, w tym ręczne uruchamianie i automatyczne wyzwalacze, takie jak komentarze. Wyjaśnił, jak używać Claudii w trybie bezgłowym za pomocą narzędzi CI/CD takich jak Jenkins i GitLab oraz omówił możliwość połączenia poprzez abonamenty firmowe zamiast indywidualnych kont. Kamil wyraził zainteresowanie wdrożeniem tego rozwiązania, ale zauważył wyzwania związane z uzyskaniem niezbędnego modelu, sugerując, że w weekend mogą zbadać alternatywy. Dyskusja dotyczyła również podejść testowych i wyzwań związanych z integracją różnych systemów, a Karol podzielił się doświadczeniami na temat ograniczeń testowania w swoim środowisku.

### Omówienie strategii testowania aplikacji

Zespół omówił strategie testowania i środowiska do tworzenia aplikacji. JSystems podkreślił znaczenie wykorzystania rzeczywistych danych i środowisk w testach, szczególnie podkreślając problemy z wykorzystaniem fałszywych obrazów we wcześniejszych testach. Dyskusja obejmowała zarówno zautomatyzowane testy E2E, jak i ręczne testy przy użyciu Playwright MCP, a JSystems opowiada się za ręcznym przeglądaniem i weryfikacją zrzutów ekranu. Kamil zadał pytania dotyczące testowania mikrousług i zasugerował użycie oddzielnego procesu do uruchamiania testów w różnych systemach, przy czym agent weryfikuje wyniki zamiast zarządzać uwierzytelnianiem. Zespół zgodził się przeanalizować istniejące testy E2E, aby upewnić się, że wykorzystują one rzeczywiste punkty końcowe, odpowiednie obrazy i dokładną komunikację API między komponentami backendowymi i frontendowymi.

### Wdrożenie zautomatyzowanego testowania AI

Zespół omówił wdrożenie zautomatyzowanych procesów testowania i weryfikacji przy użyciu agentów AI. Firma JSystems wyjaśniła swoje podejście polegające na ręcznym testowaniu procesów krok po kroku przed ich automatyzacją oraz zademonstrowała, jak używać agentów do testowania backendowego przy użyciu CURL i weryfikacji rzeczywistych punktów końcowych. Dyskusja obejmowała plany analizy testów E2E we frontendzie i walidacji prawidłowej implementacji, a także przegląd konfiguracji agentów QA w celu zapewnienia, że mogą one wykonywać zarówno testy automatyczne, jak i ręczne za pomocą Playwright. Na spotkaniu omówiono również kwestie techniczne związane z ograniczeniami kontekstowymi modelu Claude’a Opus oraz zmianami cen.

### System weryfikacji plików konfiguracji agenta

JSystems zaproponował stworzenie zautomatyzowanego systemu do przeglądania i weryfikowania spójności plików konfiguracyjnych agentów, sprawdzania sprzeczności, duplikatów informacji i nieaktualnych treści. System porównuje te pliki z bieżącym stanem aplikacji i monitami systemowymi w celu zapewnienia dokładności i trafności. JSystems zaproponował wdrożenie tej umiejętności jako niestandardowej umiejętności do regularnego utrzymywania i aktualizowania plików agentów, zapewniając ich przydatność i dokładność bez przytłaczania agenta niepotrzebnymi informacjami.

### Najlepsze praktyki zarządzania plikami agentów

Zespół omówił zarządzanie plikami agentów oraz najlepsze praktyki organizacji projektu. JSystems wyjaśnił zalecaną strukturę plików agentów, w tym opisy projektów, ogólną wiedzę i specjalistyczne umiejętności, podkreślając jednocześnie, że pliki te powinny pozostać statyczne i nie być edytowane przez agenta automatycznie. Podczas dyskusji zwrócono uwagę na obawy dotyczące zachowania spójności w przypadku zmian logiki biznesowej, a Kamil zauważył potencjalne wyzwania związane z aktualizowaniem wiedzy agentów przy modyfikacjach projektu. Zespół zgodził się, że ręczne aktualizacje plików agentów byłyby lepsze niż automatyczne aktualizacje, a JSystems zalecał traktowanie kodu jako podstawowego źródła dokumentacji zamiast polegania wyłącznie na oddzielnych plikach konfiguracyjnych agenta.

### Zarządzanie kodem i testowanie integracji

Zespół omówił wyzwania związane z utrzymaniem szczytów kodu i struktur katalogowych, a JSystems wyjaśnił potrzebę równowagi prostoty z funkcjonalnością w dokumentacji. Zbadano możliwości tworzenia i zarządzania umiejętnościami w Claude, w tym implementacje lokalne i chmurowe, a także ustawienia na poziomie organizacyjnym. Grupa omówiła również podejścia do testowania integracji, a JSystems zaproponował sposoby konfigurowania środowisk baz danych i tworzenia skryptów testowych wielokrotnego użytku. Kluczowe działania obejmowały wdrożenie narzędzia do tworzenia umiejętności, skonfigurowanie testów integracyjnych dla łączności z bazami danych oraz zbadanie opcji konfiguracji na poziomie organizacyjnym w Claude.

### Plan odroczenia demonstracji ćwiczeń

JSystems zaproponował przeprowadzenie ćwiczeń w środowisku podobnym do warunków pracy uczestników, ale nie był w stanie w pełni przygotować się zgodnie z planem ze względu na złe samopoczucie od niedzieli. Zaproponował spotkanie przez dodatkową godzinę w kwietniu po Bożym Narodzeniu, aby zademonstrować, jak wykorzystać ćwiczenie i praktykować je razem z uczestnikami.

### Dyskusja na temat narzędzi i technik automatyzacji

JSystems omówił z uczestnikami różne narzędzia i techniki automatyzacji, w tym wykorzystanie podagentów, przetwarzanie CSV oraz funkcję szkła powiększającego do powtarzania działań. Wyjaśnił koncepcję Ralpha/Wigguma, narzędzia używanego do iteracji zadań lub plików, oraz wspomniał o jego ograniczeniach w porównaniu z nowoczesnymi sub-agentami. JSystems wprowadził również Husky hooks dla Gita, które mogą zautomatyzować działania po wystąpieniu określonych zdarzeń, takich jak zmiany plików lub powiadomienia. Dyskusja dotyczyła potencjalnych przypadków wykorzystania tych narzędzi, chociaż ograniczenia czasowe uniemożliwiły przeprowadzenie dogłębnych ćwiczeń. JSystems zaprosił uczestników do zaplanowania kolejnego spotkania, jeśli będą potrzebować dalszych wyjaśnień na te tematy.

### Demonstracja narzędzi automatyzacji CI/CD

JSystems zademonstrował różne narzędzia automatyzacji i integracje dla procesów CI/CD, w tym agenty Qodo, Claude i GitHub Actions. Wyjaśnił różnice między integracjami MCP (My Claude Protocol), Skill i Workflow, zauważając, że MCP i Skill są częściej używane w aplikacjach biznesowych, podczas gdy integracje Workflow są specyficzne dla Jenkinsa. Dyskusja obejmowała przykłady wykorzystania tych narzędzi do takich zadań, jak recenzja kodu, zakres testów i automatyzacja projektów. JSystems wspomniał również, że chociaż narzędzia te są potężne, mogą być złożone i czasami powielać istniejące możliwości agentów, choć pozostają przydatne do łączenia się z zewnętrznymi narzędziami i zapewniania specjalistycznej funkcjonalności.

### Claude CI/CD Dyskusja na temat możliwości

Spotkanie koncentrowało się na omówieniu możliwości Claude’a, szczególnie w zakresie GitHub Actions i trybu bezgłówkowego dla procesów CI/CD. JSystems udostępnia zasoby i przykłady, w tym szczegółową dokumentację integracji z GitLabem. Grupa omówiła również narzędzia i podejścia do testowania integracji, przy czym JSystems podkreśla znaczenie tworzenia skryptów wielokrotnego użytku dla uzyskania spójnych wyników. Pod koniec rozmowy przesunięto się na porównanie różnych platform projektowych opartych na chmurze, a JSystems polecił Framer jako dobrą opcję do tworzenia interfejsu użytkownika, szczególnie dla aplikacji React.

### Dyskusja na temat narzędzi do rozwoju chmury

Spotkanie koncentrowało się na zademonstrowaniu i omówieniu różnych narzędzi programistycznych opartych na chmurze, w szczególności Bolt i podobnych platform. JSystems wyjaśnił, w jaki sposób narzędzia te mogą być wykorzystywane do tworzenia i testowania aplikacji internetowych, w tym klonowania stron internetowych takich jak Allegro. Kamil podzielił się swoimi doświadczeniami z integracji jednego z tych narzędzi z GitLabem i omówił potencjalne strategie optymalizacji, w tym kompresję kontekstu w celu zarządzania kosztami. Grupa zbadała również możliwość wykorzystania tych narzędzi do zautomatyzowanego przeglądu i optymalizacji kodu. Tomasz wspomniał o testowaniu narzędzi na próbkach piosenek, a dyskusja dotyczyła wartości i opłacalności tych platform do automatyzacji zadań i deweloperskich.

### Demonstracja narzędzi programistycznych AI

JSystems zademonstrował zespołowi różne narzędzia i techniki rozwoju sztucznej inteligencji, w tym analizę kodu, refaktorowanie i tworzenie pulpitów nawigacyjnych przy użyciu Vercel i Next.js. Podał konkretne instrukcje dotyczące wdrożenia nowego pulpitu nawigacyjnego, który byłby zgodny z wytycznymi marki Sinsay i zaproponował tworzenie widoków dla różnych typów danych z odpowiednimi wykresami i komponentami interfejsu użytkownika. JSystems nakreślił również kompleksowe podejście do analizy i modernizacji istniejącego 14-letniego projektu Java, obejmujące tworzenie dokumentacji, przeprowadzanie testów, uruchomienie aplikacji oraz refaktoryzację do aktualnych standardów Java. Rozmowa zakończyła się dyskusją na temat narzędzi Claude vs Codeo, w której JSystems podzielił się swoją preferencją dla Codeo do mniejszych zadań ze względu na większą kontrolę, podczas gdy Claude lepiej sprawdza się przy większych projektach, gdzie pożądana jest większa autonomia.
