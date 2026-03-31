## Szybkie podsumowanie

Spotkanie dotyczyło przeglądu postępów i wyzwań związanych z projektami rozwoju agentów AI. JSystems poprowadził dyskusję, podczas której członkowie zespołu podzielili się swoimi doświadczeniami z pracy z agentami Claude’a, w tym problemami z komunikacją backendową, limitami tokenów i problemami implementacyjnymi. Kluczowe zagadnienia obejmowały prawidłową walidację planu przed wykonaniem agenta, znaczenie wykorzystania odpowiednich umiejętności i kontekstu dla agentów oraz techniki rozwiązywania problemów, gdy agenci osiągają limity tokenów. Zespół omówił najlepsze praktyki dotyczące dokumentacji, w tym oddzielenie dokumentacji projektowej od plików specyficznych dla agentów oraz prawidłową konfigurację modeli lokalnych w porównaniu z rozwiązaniami opartymi na chmurze. Kilku uczestników, w tym Łukasz, Sebastian i Tomasz, podzieliło się konkretnymi wyzwaniami, jakie napotkali, takimi jak problemy z uzależnieniami oraz komunikacją między agentami frontendu a backendem. JSystems dostarczył wskazówek dotyczących technik rozwiązywania problemów, w tym sposobu kompresji historii sesji i prawidłowej walidacji planów agentów przed ich wykonaniem. Dyskusja dotyczyła również zaleceń dotyczących wyboru modelu i zarządzania tokenami, ze szczególnym uwzględnieniem wykorzystania odpowiednich modeli takich jak GPT-3.5 i GPT-4 do różnych zadań.

## Kolejne kroki

- JSystems: Przygotuj i zademonstruj dla zespołu World Flow oraz powiązane testy, w tym integrację z Atlasem (np. Jira/Confluence), jak wspomniano już jutro rano.
- JSystems: Prześlij zespołowi linki i wskazówki dotyczące administracji i ustawień łączników do integracji z GitHubem, o czym wspomniano podczas spotkania.
- JSystems: Wysyłaj notatki i dokumentację skrótów/komend do zespołu, jak wspomniano na końcu spotkania.
- Członkowie zespołu (zwłaszcza ci, którzy mają problemy): Kontynuuj rozwiązywanie problemów i konfigurowanie aplikacji / środowiska oraz powiadom JSystems, jeśli potrzebna jest dalsza pomoc w uruchomieniu aplikacji.
- JSystems: Pokaż, jak jutro zintegrować agenty recenzujące kod (np. Atlas, Jira/Confluence), zgodnie z obietnicą.
- JSystems: Przeglądanie i rozwiązywanie problemów z uruchamianiem środowiska oraz tajemnic (np. EXT 0, poprawki skryptów), aby zapewnić prawidłowe uruchomienie środowisk na potrzeby testów zespołowych.
- Zespół: Rozważ przetestowanie funkcji weryfikacji kodu na kontach prywatnych, zgodnie z zaleceniami JSystems, i oceń wartość potencjalnego wdrożenia w całej firmie.
- JSystems: Aktualizacja i ulepszenie bieżącego monitu dla agenta, aby uwzględnić wyspecjalizowane podprompty dla różnych ścieżek (reklamacja / zwrot pieniędzy), zgodnie z sugestią w celu poprawy wydajności agenta.

## Podsumowanie

### Przegląd postępów rozwoju agenta

Zespół omówił postępy w realizacji zadań weekendowych i przeanalizował aktualizacje repozytorium, w tym nowe umiejętności i agentów dodanych przez JSystems. Łukasz poinformował o uruchomieniu agenta backendowego, ale napotkał problemy z inicjacją projektu i zależnościami OpenAI, które zostały rozwiązane. Sebastian podzielił się swoim doświadczeniem w pracy z wieloma agentami jednocześnie, ale zauważył problemy komunikacyjne między agentami frontendowymi i backendowymi, prowadzące do problemów z limitem tokenów. Tomasz wspomniał o rozpoczęciu planu, ale napotkał problemy z jego wdrożeniem, które rozwiązał poprzez dostosowanie ustawień planu. JSystems podkreślił znaczenie walidacji planów agentów i dokumentacji przed wdrożeniem w celu uniknięcia błędów i zasugerował przegląd i opis oczekiwanych obiektów dla lepszej komunikacji z agentami. Zespół uzgodnił kolejne kroki, w tym przegląd planów, ulepszenie dokumentacji agenta oraz kontynuację prac nad rozwojem i testowaniem aplikacji.

### Umiejętności i agenci Planowanie wdrożenia

Zespół omówił instalację i wdrożenie umiejętności i agentów w swoim systemie. JSystems wyjaśnił, że umiejętności to po prostu pliki, które można pobrać i zainstalować ręcznie, nie wymagające programu do instalacji. Karol podzielił się swoimi doświadczeniami z realizacji planu, zauważając pewne wyzwania związane z połączeniami agentów i kluczowymi problemami uwierzytelniania. JSystems rekomendował konkretne modele do analizy obrazu i zadań multimedialnych, w tym Gemma3 do opisu obrazu i Qwen3.5 do bardziej zaawansowanych zadań rozumowania. Dyskusja zakończyła się wytycznymi dotyczącymi tworzenia kompleksowych planów wdrożenia, które powinny zawierać listy agentów, mapy zależności i kryteria walidacji, z naciskiem na dodanie konkretnych instrukcji dla agentów w celu lepszej zgodności.

### Dokumentacja projektu i proces wireframingu

Zespół omówił proces tworzenia dokumentacji projektowej oraz planów aplikacji, koncentrując się na opisach funkcjonalnych i technicznych. JSystems wyjaśnił, jak strukturować pliki do różnych celów, rozróżniając dokumentację projektową dla ludzi i opisy specyficzne dla agentów. Zademonstrowali również, jak używać Gemini do generowania obrazów szkieletowych na podstawie dokumentu PRD. Dyskusja obejmowała szczegóły dotyczące referencji wizualnych potrzebnych do wdrożenia i walidacji interfejsu użytkownika, w tym wireframesów i odniesień do spójności marki. JSystems podzielił się swoimi spostrzeżeniami na temat pracy z agentami, w tym ręcznej kontroli dokumentacji oraz znaczenia sprawdzania elementów wizualnych pod kątem wytycznych projektowych.

### Dyskusja na temat rozwiązań Token Limit

Tomasz napotkał problem z dziennym limitem wykorzystania tokena i omówił potencjalne rozwiązania z JSystems. JSystems zasugerował użycie różnych modeli, takich jak GPT-3.5 lub GPT-4, konfigurację Open Routera i wykorzystanie kodu Claude'a jako alternatywy do bardziej efektywnego zarządzania limitami tokenów. Omówiono również znaczenie prawidłowej konfiguracji zmiennych środowiskowych i kluczy w celu uniknięcia zagrożeń bezpieczeństwa. JSystems zalecił monitorowanie sesji w celu zapewnienia dokładnego wykonania zadań oraz zaproponował informowanie głównego agenta o wszelkich problemach napotkanych podczas specjalistycznych sesji.

### Claude Demonstracja zarządzania sesjami

JSystems zademonstrował w Claude, jak zarządzać i kompresować historię sesji, zachowując ważne informacje, takie jak plany i historia wiadomości, jednocześnie usuwając niepotrzebne szczegóły. Pokazał proces walidacji zgodności dokumentacji z Assistant UI przy użyciu konkretnych umiejętności i plików architektonicznych oraz podkreślił znaczenie delegowania zadań zamiast ich bezpośredniego wdrażania. Dyskusja dotyczyła również statystyk i monitorowania wykorzystania tokenów, a Tomasz zauważył rozbieżności między liczbą tokenów lokalnych i routerowych, osiągając prawie 8 milionów tokenów. Zespół omówił ograniczenia modeli zewnętrznych w zakresie wyświetlania kompletnych historii poleceń i zgodził się na potrzebę lepszego generowania linii statusu w celu poprawy kontroli pracy.

### Wydajność agenta AI i tokeny

Zespół omówił problemy z wydajnością agenta AI i zużyciem tokenów w ich systemie. JSystems wyjaśnił, że agenci doświadczali opóźnień i czasu oczekiwania w kolejkach, czasami przetwarzanie żądań trwało do 12 minut, choć często było to spowodowane oczekiwaniem na dostępne zasoby, a nie zablokowaniem. Dyskusja dotyczyła różnych planów abonamentowych i ich kosztów, a uczestnicy zauważyli, że wyższe plany, takie jak Code Yy, szybciej zużywają tokeny, ale oferują większą moc obliczeniową. Zespół rozwiał również obawy dotyczące przypadkowych przerw powodujących utratę pracy oraz potrzebę lepszych monitów potwierdzania w interfejsie.

### Dyskusja na temat subskrypcji modelu AI

Spotkanie koncentrowało się na omówieniu abonamentów modeli AI oraz narzędzi do programowania. JSystems wyjaśnił swoją aktualną konfigurację subskrypcji i zademonstrował, jak używać różnych narzędzi AI, takich jak Claude i Codex. Dyskusja dotyczyła technicznych aspektów pracy z różnymi modelami AI, w tym sposobu obsługi tokenów autoryzacyjnych i zarządzania sesjami. Karol wspomniał o ich zainteresowaniu pracą z istniejącymi bazami kodu i zapytał o implementację nowych funkcji w istniejących projektach. JSystems przedstawił swoje podejście do pracy z istniejącym kodem, w tym tworzenie PRD i analizę różnych frameworków przed wdrożeniem. Rozmowa zakończyła się porównaniem Claude’a z Codexem, podkreślającym różnice w wydajności i otwartości.

### Porównanie Codex vs Claude

Dyskusja koncentrowała się na porównaniu Codexu i Claude’a, w szczególności ich wbudowanych funkcji i różnic operacyjnych. JSystems wyjaśnił, że Codex ma specjalne wbudowane narzędzia do przeglądania i edycji, podczas gdy Claude oferuje większą elastyczność, ale wymaga większej ręcznej konfiguracji. Kluczowe omówione różnice dotyczyły trybu piaskownicy Codexu, który domyślnie zapewnia lepszą kontrolę bezpieczeństwa, wykorzystania przez Claude’a poleceń Basha oraz ich odpowiednich podejść do zarządzania agentami. Rozmowa dotyczyła również takich funkcji, jak tryb automatyczny w Claude (dostępny tylko w planie Teams) i eksperymentalna funkcja kolapsu. JSystems zauważył, że chociaż wiele funkcji jest podobnych między obiema platformami, istnieją znaczne różnice w implementacji i podejściu.

### Porównanie narzędzi do kodowania AI

JSystems przedstawił szczegółowe porównanie dwóch narzędzi do kodowania AI, koncentrując się na Codex i Claude. Podkreślił, że Codex jest bardziej odpowiedni dla programistów ze względu na lepszą integrację, szybkość i brak kompromisów dla użytkowników nietechnicznych, podczas gdy Claude jest lepszy dla użytkowników nietechnicznych dzięki funkcjom współpracy w chmurze i Claude Cowork. Firma JSystems zademonstrowała możliwości Codex, w tym pracę lokalną i opartą na chmurze, integrację Git oraz niestandardowe konfiguracje środowisk. Rozmowa zakończyła się planami kontynuacji dyskusji o 14:10 po przerwie, a JSystems wspomniał, że w międzyczasie będzie pracował nad zakończeniem projektu i testami.

### Demonstracja integracji z chmurą Claude'a

JSystems zademonstrował możliwości integracji Claude’a z chmurą, koncentrując się w szczególności na połączeniach repozytoriów GitHub i funkcjonalnościach związanych z kodem. Dyskusja dotyczyła konfigurowania złączy, zarządzania uprawnieniami dostępu do repozytorium oraz konfiguracji zmiennych środowiskowych do wykonywania kodu. Tomasz pytał o narzędzia do debugowania aplikacji backendowych, a JSystems zasugerował wykorzystanie Chrome DevTools MCP jako potencjalnego rozwiązania do wykrywania i analizowania błędów konsoli. Podczas spotkania poruszono również kwestie administracyjne związane z wdrożeniem tych integracji na poziomie przedsiębiorstwa.

### Dyskusja na temat wdrożenia zdalnych agentów rozwoju

Zespół omówił wykorzystanie agentów do zdalnych zadań programistycznych i debugowania. JSystems zademonstrował, jak konfigurować i używać agentów do pracy ze środowiskami GitHub, w tym uruchamiania aplikacji i analizowania logów. Przeanalizowali różne narzędzia, takie jak Codex i GitHub Copilot, do integracji agentów z przepływami pracy programistycznej. Dyskusja obejmowała najlepsze praktyki dotyczące pracy z dużymi plikami dzienników oraz konfigurowania uprawnień agenta w celu zapobiegania nieautoryzowanemu dostępowi do poufnych danych. Zespół poruszył również kwestię wykorzystania aplikacji mobilnych do zdalnego monitorowania i delegowania pracy agentom.

### Dyskusja na temat narzędzi automatyzacji weryfikacji kodu

Zespół omówił wykorzystanie agentów Code Review do zautomatyzowanych procesów recenzji kodu, ze szczególnym uwzględnieniem środowisk GitHub i GitLab. Firma JSystems zademonstrowała, jak używać agentów Code Review do usprawnienia przepływów pracy związanych z recenzją kodu, w tym automatycznych komentarzy dotyczących żądań pull i analizy kodu. Dyskusja podkreśliła korzyści płynące z tych narzędzi w znajdowaniu problemów, które mogą zostać pominięte podczas ręcznych recenzji, chociaż Kamil wyraził obawy dotyczące praktyczności wdrożenia takich narzędzi w ich obecnej konfiguracji GitLab. Zespół omówił również wyzwania związane z uruchomieniem i konfiguracją aplikacji, przy czym niektórzy członkowie doświadczali problemów technicznych. JSystems obiecał przygotować szczegółowy proces na sesję następnego dnia i zasugerował zbadanie alternatywnych narzędzi, takich jak Qodo.ai, w celu potencjalnej implementacji.
