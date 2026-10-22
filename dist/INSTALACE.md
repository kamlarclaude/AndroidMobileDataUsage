# Spotřeba dat — instalace do telefonu

Aplikace ukazuje vedle hodin, kolik procent měsíčních mobilních dat je spotřebováno
(např. `49%`). Po stažení lišty je vidět podrobnosti: spotřeba z limitu, kolik zbývá,
dny do konce měsíce, průměr za den a odhad na konci měsíce. Data měří sám telefon,
stejně jako Nastavení → Využití dat.

| | |
|---|---|
| Soubor | `SpotrebaDat-1.1.apk` (2,1 MB) |
| Verze | 1.1 |
| Požadavky | Android 13 nebo novější |
| Vyzkoušeno na | Samsung Galaxy S23, Android 16 (One UI) |
| Jediný oficiální zdroj | <https://github.com/kamlarclaude/AndroidMobileDataUsage/releases> |
| Kontrolní součet SHA-256 | `5fa58d563c03af4aae40d01adf21f48e6f84341fbe803ba519bd6252cfab46fc` |
| Otisk podpisového certifikátu (SHA-256) | `7bd00f44dc23c4727a92bb1f0fd8f2fe0fbe237bbbf7d99523b247758dec736b` |

Aplikace není v Google Play, instaluje se ze souboru. Celý postup trvá asi 10 minut
**včetně posledního kroku, kdy vrátíte ochrany telefonu zpět — ten prosím nevynechávejte.**

---

## Než začnete: co aplikace v telefonu smí a co ne

Instalace mimo Google Play znamená, že aplikaci důvěřujete vy, ne Google. Proto stručně,
co dostane:

| Oprávnění | Proč ho potřebuje | Co s ním dělá |
|---|---|---|
| Přístup k údajům o využití | Jen tak jde zjistit spotřebu mobilních dat | Čte jediné číslo — celkovou spotřebu telefonu za měsíc. Nic neukládá ani neodesílá. |
| Notifikace | Zobrazení procent vedle hodin | Jedna trvalá notifikace |
| Baterie bez omezení | Aby ji telefon neuspával | Obnovuje číslo jednou za minutu, jen při rozsvíceném displeji |

Aplikace **nemá přístup k internetu** — i kdyby chtěla, nemůže nic odeslat. Nemá přístup
k poloze, kontaktům, SMS, fotkám, souborům ani k usnadnění přístupu. Zdrojový kód je
veřejný na GitHubu (odkaz výše).

---

## 1. Zkontrolujte verzi Androidu

**Nastavení → O telefonu → Informace o softwaru → Verze Android.** Musí být 13 nebo vyšší.

## 2. Stáhněte soubor — jen z oficiálního zdroje

Otevřete v telefonu v prohlížeči (Chrome, Samsung Internet) odkaz

<https://github.com/kamlarclaude/AndroidMobileDataUsage/releases>

a u nejnovější verze v části **Assets** klepněte na **SpotrebaDat-1.1.apk**.

> **Proč ne přes WhatsApp nebo e-mail:** soubor přeposlaný v chatu může být podvržený
> a při první instalaci to telefon nepozná. Podvodníci šíří škodlivé aplikace právě
> jako „APK od známého“. Pokud vám soubor někdo pošle, nainstalujte raději verzi
> z odkazu výše.

## 3. Jen Samsung: dočasně vypněte Automatické blokování

Samsung jinak instalaci ze souboru zablokuje.

**Nastavení → Zabezpečení a soukromí → Automatické blokování → vypnout.**

Je to jen dočasné — v kroku 7 ho zase zapnete. Pokud tuto položku v telefonu nemáte,
krok přeskočte.

## 4. Nainstalujte

1. Po stažení klepněte v prohlížeči na **Otevřít** (nebo najděte soubor v aplikaci
   **Moje soubory → Stažené soubory**).
2. Telefon napíše, že z tohoto zdroje nesmí instalovat aplikace → **Nastavení** →
   zapněte **Povolit z tohoto zdroje** → vraťte se zpět.
   **Zapamatujte si, které aplikaci jste to povolili** (např. Chrome nebo Moje soubory) —
   v kroku 7 to vypnete.
3. Klepněte na **Instalovat**.
4. **Google Play Protect:**
   - Nabídne-li **kontrolu aplikace** (odeslání ke skenování), **povolte ji** — je to
     nezávislé ověření, že soubor neobsahuje nic škodlivého.
   - Varování o **neznámé aplikaci** se zobrazuje u každé aplikace mimo Google Play.
     U této aplikace z oficiálního zdroje můžete zvolit **Další podrobnosti → Přesto
     nainstalovat**.
   - Pokud Play Protect napíše, že aplikace je **škodlivá** nebo ji **zablokoval**,
     **neinstalujte ji**, soubor smažte a dejte vědět autorovi.

   > Varování Play Protect obecně neobcházejte. Postup výše platí jen pro aplikaci,
   > o které víte, odkud pochází — ne pro soubory z odkazů v SMS, e-mailech nebo reklamách.
5. Klepněte na **Otevřít**.

## 5. První nastavení v aplikaci

Nahoře je karta **Dokončete nastavení** se třemi kroky. Projděte je postupně:

1. **Přístup k údajům o využití → Povolit.** Otevře se systémové nastavení, kde u položky
   **Spotřeba dat** zapněte přepínač a vraťte se zpět (šipkou zpět).
   - Pokud je přepínač šedý a telefon hlásí **Omezené nastavení**: Android tak chrání
     citlivá oprávnění u aplikací instalovaných ze souboru. Vraťte se do
     **Nastavení → Aplikace → Spotřeba dat**, vpravo nahoře klepněte na **⋮** →
     **Povolit omezená nastavení**, potvrďte a krok 1 zopakujte.
   - Omezená nastavení tím odemknete **jen pro tuto jednu aplikaci**. U aplikací,
     o jejichž původu si nejste jistí, to nedělejte — podvodné aplikace tak často
     získávají přístup k telefonu.
2. **Notifikace → Povolit** → v dialogu **Povolit**.
3. **Baterie bez omezení → Povolit** → potvrďte. Bez toho může telefon aplikaci
   na pozadí uspávat a číslo by přestalo přibývat.

Pak:

4. Zkontrolujte **Měsíční limit** — přednastaveno je 12 GB. Přepište ho na velikost
   svého tarifu (např. `10` nebo `7,5`); uloží se sám.
5. Zapněte **Zobrazovat na liště**.

Vedle hodin se objeví procenta a karta s oprávněními se sbalí dolů jako
„Všechna oprávnění povolena“.

## 6. Ověřte, že čísla sedí

Porovnejte číslo v aplikaci s **Nastavení → Připojení → Využití dat** (na jiných
telefonech **Nastavení → Síť a internet → Využití dat**). Mělo by se shodovat
s přesností na setiny GB.

## 7. Vraťte ochrany telefonu zpět — nevynechávejte

Aplikace už je nainstalovaná a tyto ochrany ke své činnosti nepotřebuje. Pokud je
necháte vypnuté, příští podvržená aplikace se do telefonu dostane mnohem snáz.

1. **Instalace neznámých aplikací:** **Nastavení → Aplikace → ⋮ (vpravo nahoře) →
   Zvláštní přístup → Instalovat neznámé aplikace** → u aplikace z kroku 4 (Chrome,
   Moje soubory…) **vypněte** povolení.
   (Pokud cestu nenajdete, napište do vyhledávání v Nastavení „neznámé aplikace“.)
2. **Jen Samsung — Automatické blokování:** **Nastavení → Zabezpečení a soukromí →
   Automatické blokování → zapnout.**
3. Stažený soubor `SpotrebaDat-1.1.apk` můžete z **Moje soubory → Stažené soubory**
   smazat — už není potřeba.

Hotovo.

---

## Důležité: závazné je vyúčtování operátora

Aplikace ukazuje, co naměřil telefon. Operátor může počítat jinak, a pokud se na
číslo spoléháte, můžete tarif nečekaně přečerpat:

- Aplikace počítá vždy **od 1. dne v měsíci**. Pokud vám operátor účtuje od jiného dne
  (např. od 15.), čísla se budou lišit — tato verze jiný začátek období neumí.
- Při **dvou SIM** ukazuje součet za obě.
- Operátor může některé přenosy počítat jinak (např. roaming, služby bez započítání dat).

Skutečný stav ověříte v aplikaci nebo na webu svého operátora.

## Soukromí na zamčené obrazovce

Notifikace s textem „Mobilní data: 9,05 GB z 12 GB“ je ve výchozím stavu vidět
i na zamčené obrazovce. Pokud to nechcete:
**Nastavení → Oznámení → Oznámení na zamčené obrazovce** → skrýt obsah.

---

## Co dělat, když…

**Číslo vedle hodin není vidět.**
Samsung může ukazovat jen několik posledních ikon oznámení.
**Nastavení → Oznámení → Rozšířená nastavení → Zobrazit ikony oznámení** → zvolte
**Všechna oznámení**. Ověřte také, že je v aplikaci zapnuté **Zobrazovat na liště**.

**Vedle hodin je „?“.**
Chybí přístup k údajům o využití — otevřete aplikaci a dokončete krok 1 nastavení.

**Číslo se přestalo měnit.**
Otevřete aplikaci a zkontrolujte, že všechna oprávnění jsou povolená (hlavně
Baterie bez omezení). Číslo se obnovuje jednou za minutu, jen při rozsvíceném displeji.

**Po restartu telefonu.**
Aplikace se spustí sama, nic není potřeba dělat.

**Wi-Fi.**
Data přes Wi-Fi se nepočítají — jen mobilní data, stejně jako u tarifu.

## Aktualizace na novější verzi

Aplikace se neaktualizuje sama. Novou verzi stáhněte opět jen z oficiálního zdroje
(odkaz nahoře) a nainstalujte stejným postupem přes stávající aplikaci — včetně
kroku 7. Nastavení i povolená oprávnění zůstanou zachovaná.

Android novou verzi nainstaluje jen tehdy, když je podepsaná stejným klíčem jako
ta stávající. Pokud instalace skončí chybou typu **„Aplikace nebyla nainstalována —
balíček je v konfliktu“**, soubor nepochází od stejného autora: **neinstalujte ho
a stávající aplikaci neodinstalovávejte.**

## Pro pokročilé: ověření pravosti souboru

- **Kontrolní součet SHA-256** souboru a **otisk podpisového certifikátu** jsou
  v tabulce nahoře. Na počítači je ověříte např. příkazem
  `certutil -hashfile SpotrebaDat-1.1.apk SHA256` (Windows), otisk certifikátu nástrojem
  `apksigner verify --print-certs` z Android SDK.
- Otisk certifikátu je u všech verzí stejný; kontrolní součet se mění s každou verzí.

## Odinstalace

Podržte prst na ikoně **Spotřeba dat** → **Odinstalovat**. Odinstalace odstraní vše;
aplikace v telefonu nic nenechává.
