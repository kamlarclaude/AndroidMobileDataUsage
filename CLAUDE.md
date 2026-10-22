# CLAUDE.md — AndroidDataUsage

Android aplikace (Kotlin, bez knihoven třetích stran — jen framework API, žádné
AndroidX) zobrazující procento spotřebovaných mobilních dat ve stavovém řádku.
Uživatelský popis, build a instalace: `README.md`.

## Struktura

- `UsageCalc.kt` — čistá logika (období, %, zbývá, Ø, odhad, úroveň/barva `Level`,
  dlaždice hlavní obrazovky, parsování limitu, české texty).
  **Jediná část pokrytá unit testy** (`app/src/test/.../UsageCalcTest.kt`);
  veškerou výpočetní/formátovací logiku drž tady, ne v Android třídách.
- `UsageReader.kt` — `NetworkStatsManager.querySummaryForDevice(TYPE_MOBILE, null, …)`;
  `subscriberId = null` = souhrn všech SIM (od Androidu 10 jiná cesta bez
  oprávnění operátora není).
- `IconRenderer.kt` — bitmapová ikona do stavového řádku (systém bere jen alfa kanál).
- `DataUsageService.kt` — foreground service typu `specialUse`, obnova 60 s,
  při zhasnutém displeji stojí.
- `MainActivity.kt` — UI skládané v kódu (bez XML layoutů a bez Material knihovny).
  Barvy karet se odvozují z `textColorPrimary` motivu (8% závoj), takže fungují
  ve světlém i tmavém režimu — nezadávej barvy pozadí natvrdo. Čtení spotřeby běží
  mimo hlavní vlákno, obrazovka se obnovuje po 60 s.
- `BootReceiver.kt`, `Prefs.kt`.
- Ikona aplikace je adaptivní (`res/mipmap/ic_launcher.xml` + `monochrome`).

## Rozhodnutí, která neměnit bez důvodu

- Kanál notifikace má `IMPORTANCE_DEFAULT` se vypnutým zvukem, **ne LOW** —
  tiché notifikace může One UI ve stavovém řádku skrýt.
- FGS typ `specialUse` (ne `dataSync`): `dataSync` má od Androidu 15 limit 6 h
  a nesmí startovat z `BOOT_COMPLETED`.
- Období začíná vždy 1. dne v měsíci (požadavek uživatele); dnešek se v průměru
  počítá jako celý den.
- Jednotky jsou vždy desítkové (1 GB = 1000³ B) jako na Samsungu. Volba
  „1024 MB“ byla odebrána, protože mátla (rozdíl 9,05 vs. 9,71 GB) — nevracet.
- Verze toolchainu: AGP 8.7.3, Kotlin 2.0.21, Gradle 8.10.2, JDK 17,
  compileSdk/targetSdk 35, minSdk 33.

## Build a testy

Po každé úpravě kódu spusť (toolchain je přenosný v `C:\Claude\_tools\`):

```bash
cd /c/Claude/AndroidDataUsage
export JAVA_HOME="/c/Claude/_tools/jdk-17.0.20.1+1"
./gradlew test assembleDebug lintDebug
```

Release: `./gradlew assembleRelease` → kopie do `dist\SpotrebaDat-<verze>.apk`,
návod `dist\INSTALACE.md` (verze, velikost, SHA-256 aktualizovat). Podpis
`signing\release.jks` + `keystore.properties` — nikdy negenerovat znovu (rozbije
aktualizace u lidí, kteří už aplikaci mají), hesla nevypisovat do chatu.

Instalace přes USB: Samsung s **Automatickým blokováním** hlásí `adb devices`
→ `unauthorized` i po potvrzení dialogu; uživatel ho musí vypnout sám.

Na reálném telefonu testuje uživatel sám — výsledek nahlas neprohlašuj za
ověřený na zařízení.

Projekt není git repozitář; necommituj bez výslovného požadavku.
