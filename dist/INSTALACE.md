# Spotřeba dat — instalace do telefonu

Aplikace ukazuje vedle hodin, kolik procent měsíčních mobilních dat je spotřebováno
(např. `49%`). Po stažení lišty je vidět podrobnosti: spotřeba z limitu, kolik zbývá,
dny do konce měsíce, průměr za den a odhad na konci měsíce. Data měří sám telefon,
stejně jako Nastavení → Využití dat. Nic se nikam neodesílá a aplikace nepotřebuje internet.

| | |
|---|---|
| Soubor | `SpotrebaDat-1.1.apk` (2,1 MB) |
| Verze | 1.1 |
| Požadavky | Android 13 nebo novější |
| Vyzkoušeno na | Samsung Galaxy S23, Android 16 (One UI) |
| Kontrolní součet SHA-256 | `5fa58d563c03af4aae40d01adf21f48e6f84341fbe803ba519bd6252cfab46fc` |

Aplikace není v Google Play, instaluje se ze souboru. Celý postup trvá asi 5 minut.

---

## 1. Zkontrolujte verzi Androidu

**Nastavení → O telefonu → Informace o softwaru → Verze Android.** Musí být 13 nebo vyšší.

## 2. Dostaňte soubor do telefonu

Jakkoli vám to vyhovuje: e-mailem, přes WhatsApp nebo Google Disk, případně kabelem
z počítače do složky **Download** (Stažené soubory). Uložte soubor do telefonu.

## 3. Jen Samsung: dočasně vypněte Automatické blokování

Samsung jinak instalaci ze souboru zablokuje.

**Nastavení → Zabezpečení a soukromí → Automatické blokování → vypnout.**

Po dokončení instalace ho můžete zase zapnout — na běh aplikace nemá vliv.
Pokud tuto položku v telefonu nemáte, krok přeskočte.

## 4. Nainstalujte

1. Otevřete aplikaci **Moje soubory** (Samsung) nebo **Soubory** a v ní **Stažené soubory**.
   (Případně klepněte přímo na soubor v e-mailu nebo v chatu.)
2. Klepněte na **SpotrebaDat-1.1.apk**.
3. Telefon napíše, že z tohoto zdroje nesmí instalovat aplikace → **Nastavení** →
   zapněte **Povolit z tohoto zdroje** → vraťte se zpět.
4. Klepněte na **Instalovat**.
5. Pokud se objeví **Google Play Protect** s varováním o neznámé aplikaci, zvolte
   **Další podrobnosti → Přesto nainstalovat**. Varování se zobrazuje u každé aplikace,
   která není z Google Play. Nabídku na odeslání aplikace ke kontrole můžete odmítnout.
6. Klepněte na **Otevřít**.

## 5. První nastavení v aplikaci

Nahoře je karta **Dokončete nastavení** se třemi kroky. Projděte je postupně:

1. **Přístup k údajům o využití → Povolit.** Otevře se systémové nastavení, kde u položky
   **Spotřeba dat** zapněte přepínač a vraťte se zpět (šipkou zpět).
   - Pokud je přepínač šedý a telefon hlásí **Omezené nastavení**: vraťte se do
     **Nastavení → Aplikace → Spotřeba dat**, vpravo nahoře klepněte na **⋮** →
     **Povolit omezená nastavení**, potvrďte a krok 1 zopakujte.
2. **Notifikace → Povolit** → v dialogu **Povolit**.
3. **Baterie bez omezení → Povolit** → potvrďte. Bez toho může telefon aplikaci
   na pozadí uspávat a číslo by přestalo přibývat.

Pak:

4. Zkontrolujte **Měsíční limit** — přednastaveno je 12 GB. Přepište ho na velikost
   svého tarifu (např. `10` nebo `7,5`); uloží se sám.
5. Zapněte **Zobrazovat na liště**.

Hotovo — vedle hodin se objeví procenta a karta s oprávněními se sbalí dolů jako
„Všechna oprávnění povolena“.

## 6. Ověřte, že čísla sedí

Porovnejte číslo v aplikaci s **Nastavení → Připojení → Využití dat** (na jiných
telefonech **Nastavení → Síť a internet → Využití dat**). Mělo by se shodovat
s přesností na setiny GB.

Aplikace počítá vždy **od 1. dne v měsíci**. Pokud vám operátor účtuje od jiného dne
(např. od 15.), čísla se budou lišit — tato verze jiný začátek období neumí.

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

**Dvě SIM karty.**
Aplikace ukazuje součet mobilních dat za obě SIM.

**Wi-Fi.**
Data přes Wi-Fi se nepočítají — jen mobilní data, stejně jako u tarifu.

## Aktualizace na novější verzi

Nový soubor `.apk` nainstalujte stejným postupem přes stávající aplikaci.
Nastavení i povolená oprávnění zůstanou zachovaná.

## Odinstalace

Podržte prst na ikoně **Spotřeba dat** → **Odinstalovat**.
