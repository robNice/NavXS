# Polityka prywatności NavXS

Ostatnia aktualizacja: 14 września 2026

## 1. Informacje ogólne

NavXS to aplikacja Android, która może wyświetlać nakładkę nawigacyjną ze stałymi przyciskami dla wybranych aplikacji.

Deweloper tej aplikacji zbiera, przechowuje lub przetwarza dane osobowe tylko w zakresie technicznie niezbędnym do działania aplikacji.

## 2. Lokalne przetwarzanie na urządzeniu

NavXS przetwarza informacje zasadniczo lokalnie na urządzeniu.

Obejmuje to w szczególności:

- nazwę pakietu aplikacji aktualnie działającej na pierwszym planie
- wybór aplikacji, dla których nakładka powinna być włączona
- ustawienia nakładki zdefiniowane przez użytkownika, takie jak pozycje, kolory, rozmiary, przezroczystość i motywy
- status włączenia usługi ułatwień dostępu
- URI obrazu, który użytkownik opcjonalnie wybiera jako tło edytora pozycjonowania, wraz z ustawionym dla niego kryciem

## 3. Cel przetwarzania

Informacje te są przetwarzane wyłącznie w celu:

- wykrycia, czy nakładka powinna być wyświetlana dla aktualnie otwartej aplikacji
- wyświetlania lub ukrywania nakładki
- udostępniania skonfigurowanych akcji nawigacji
- lokalnego zapisywania i przywracania ustawień wybranych przez użytkownika

## 4. Usługa ułatwień dostępu

NavXS korzysta z interfejsu API usługi ułatwień dostępu Androida (AccessibilityService). NavXS nie jest narzędziem ułatwień dostępu dla osób z niepełnosprawnościami; usługa jest wymagana do działania podstawowej funkcji aplikacji, czyli nakładki nawigacyjnej.

### 4.1 Zgoda

Przed poproszeniem o usługę ułatwień dostępu NavXS wyświetla informację wyjaśniającą, w jaki sposób usługa jest wykorzystywana. Użytkownik wybiera jeden z dwóch przycisków: „Odrzuć” lub „Zgadzam się”. Dopiero po dotknięciu „Zgadzam się” NavXS otwiera ustawienia ułatwień dostępu Androida, w których użytkownik musi samodzielnie włączyć usługę. „Odrzuć” zamyka aplikację bez włączania czegokolwiek. Opuszczenie informacji w inny sposób nie jest traktowane jako zgoda. Usługę można w każdej chwili wyłączyć w ustawieniach ułatwień dostępu Androida.

### 4.2 Do czego służy usługa

Usługa jest wykorzystywana wyłącznie do:

- rozpoznawania nazwy pakietu aplikacji działającej aktualnie na pierwszym planie
- decydowania, czy nakładka ma być wyświetlana dla tej aplikacji
- wyświetlania pływających przycisków nawigacyjnych nad wybranymi aplikacjami
- wykonywania działań nawigacyjnych Wstecz, Ekran główny i Ostatnie aplikacje, gdy użytkownik dotknie odpowiedniego przycisku

### 4.3 Czego usługa nie robi

Usługa ułatwień dostępu ma technicznie dostęp do informacji o oknach aplikacji na pierwszym planie. NavXS wykorzystuje ten dostęp wyłącznie do ustalenia nazwy pakietu tej aplikacji. NavXS nie odczytuje, nie zapisuje ani nie przesyła wpisywanego tekstu, haseł, treści wiadomości ani żadnych innych treści wyświetlanych na ekranie. NavXS nie wykonuje żadnych działań w imieniu użytkownika poza wymienionymi działaniami nawigacyjnymi i tylko wtedy, gdy użytkownik dotknie przycisku.

Usługa nie jest wykorzystywana do celów reklamowych, śledzenia ani analityki, a dane nie są sprzedawane ani udostępniane.

## 5. Brak przekazywania do zewnętrznych serwerów

Obecnie NavXS nie przekazuje wybranych aplikacji, nazw pakietów aplikacji działających na pierwszym planie ani ustawień nakładki na zewnętrzne serwery ani inne zewnętrzne usługi.

Przetwarzanie odbywa się lokalnie na urządzeniu.

## 6. Przechowywanie danych

Ustawienia i wybory aplikacji przechowywane przez NavXS są zapisywane lokalnie na urządzeniu.

Dane te pozostają na urządzeniu do momentu zmiany przez użytkownika, usunięcia przez wyczyszczenie danych aplikacji lub skasowania przez odinstalowanie aplikacji.

Jeśli użytkownik wybierze zrzut ekranu jako tło edytora pozycjonowania, NavXS zapisuje wyłącznie URI tego obrazu wraz z przyznanym przez system prawem odczytu. Sam plik obrazu nie jest kopiowany, nie jest zapisywany w katalogu aplikacji, nie jest przekazywany usłudze ułatwień dostępu ani przesyłany. Zresetowanie tła usuwa zapisany URI i ponownie zwalnia prawo odczytu.

## 7. Uprawnienia

NavXS wymaga w szczególności uprawnienia do ułatwień dostępu jawnie włączonego przez użytkownika, aby aplikacja mogła zapewnić funkcjonalność nakładki.

Ponadto używane są tylko te dostępy, które są technicznie niezbędne do działania aplikacji.

## 8. Podmioty trzecie

Obecnie NavXS nie korzysta z usług podmiotów trzecich, narzędzi analitycznych ani reklam do przetwarzania danych opisanych powyżej.

## 9. Zmiany w niniejszej polityce prywatności

Niniejsza polityka prywatności może być aktualizowana w razie potrzeby, aby odzwierciedlić zmiany w aplikacji lub wymogi prawne.

## 10. Kontakt

W przypadku pytań dotyczących prywatności można skontaktować się z deweloperem.

E-mail: navxs@robnice.de
