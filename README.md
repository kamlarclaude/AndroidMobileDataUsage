# AndroidDataUsage

Android aplikace (cíleno na Samsung S23, Android 13+), která ve stavovém řádku vedle
hodin ukazuje **procento spotřebovaných mobilních dat** z měsíčního limitu tarifu.
Data měří sám telefon (`NetworkStatsManager`), stejně jako Nastavení → Připojení →
Využití dat.

- Zúčtovací období: vždy od 1. dne v měsíci.
- Limit: nastavitelný v aplikaci, výchozí 12 GB.
- Název aplikace v telefonu: **Spotřeba dat**.
- Ikona vedle hodin: velké číslo s malým „%“ vpravo nahoře (např. `73%`), obnova
  každých 60 s při rozsvíceném displeji.
- Jednotky vždy jako Samsung (1 GB = 1000 MB) — čísla odpovídají Nastavení → Využití dat.
- Rozbalená notifikace: spotřeba z limitu, proužek (modrý / oranžový od 90 % /
  červený po překročení), zbývá, dny do konce období, Ø za den, odhad na konci měsíce.
- Hlavní obrazovka: velká procenta s proužkem, dlaždice (zbývá, do konce období,
  Ø za den, odhad), přepínač „Zobrazovat na liště“, měsíční limit (ukládá se sám).
  Dokud chybí oprávnění, je nahoře karta „Dokončete nastavení“; potom se sbalí dolů.
- Po restartu telefonu se sama znovu spustí (pokud byla zapnutá).

## Build (na tomto PC)

Toolchain je přenosný v `C:\Claude\_tools\` (nic není instalováno do systému):
`jdk-17.0.20.1+1\`, `android-sdk\`, `gradle-8.10.2\`.

```bash
cd /c/Claude/AndroidDataUsage
export JAVA_HOME="/c/Claude/_tools/jdk-17.0.20.1+1"
./gradlew test assembleDebug
```

Výsledek: `app\build\outputs\apk\debug\app-debug.apk`.
`local.properties` (cesta k SDK) je lokální, necommitovat.

## Release balíček (pro jiné telefony)

```bash
./gradlew test assembleRelease
cp app/build/outputs/apk/release/app-release.apk dist/SpotrebaDat-<verze>.apk
```

- Podepisuje se klíčem `signing\release.jks`, hesla jsou v `keystore.properties`
  (obojí jen na tomto PC, mimo verzování). **Zálohujte obojí** — bez klíče nejde vydat
  aktualizaci, která se nainstaluje přes již nainstalovanou verzi (nutná by byla
  odinstalace a ztráta nastavení).
- Před novým vydáním zvyšte `versionCode`/`versionName` v `app/build.gradle.kts`
  a v `dist\INSTALACE.md` aktualizujte verzi, velikost a SHA-256 (`sha256sum dist/*.apk`).
- Návod pro uživatele: `dist\INSTALACE.md` — posílá se spolu s APK.
- Debug build (`assembleDebug`) je podepsaný ladicím klíčem tohoto PC; release
  ho nepřepíše (jiný podpis) — přechod vyžaduje odinstalaci.

## Instalace do telefonu (vývoj)

**Varianta A — přes USB (doporučeno):**
1. Telefon: Nastavení → O telefonu → Informace o softwaru → 7× klepnout na „Číslo sestavení“.
2. Nastavení → Možnosti pro vývojáře → zapnout „Ladění USB“.
3. Připojit kabelem, na telefonu potvrdit otisk počítače.
4. `C:\Claude\_tools\android-sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk`

Pokud `adb devices` hlásí `unauthorized`, je na displeji telefonu nepotvrzený dotaz
„Povolit ladění USB?“ (po delší pauze se ptá znovu).

**Varianta B — zkopírováním APK** do telefonu a otevřením v aplikaci Moje soubory
(telefon si vyžádá povolení „Instalovat neznámé aplikace“ pro Moje soubory).
Pokud pak Android u přístupu k údajům o využití hlásí „Omezené nastavení“:
Nastavení → Aplikace → Spotřeba dat → ⋮ → „Povolit omezená nastavení“.

## První spuštění

Aplikace nahoře ukáže kartu **Dokončete nastavení** s kroky:
1. **Přístup k údajům o využití** (otevře systémové nastavení, zapnout u „Spotřeba dat“).
2. **Notifikace** (po dvojím zamítnutí otevře rovnou nastavení notifikací aplikace).
3. **Baterie bez omezení** (One UI jinak službu uspává).

Pak zkontrolovat **Měsíční limit** (výchozí 12 GB) a zapnout **Zobrazovat na liště**.
