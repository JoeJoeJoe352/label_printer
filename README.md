# Címkenyomtató – Android próbafeladat

Egyképernyős Android-alkalmazás telefonra és tabletre. Három beviteli mezőben
megadható a **név**, a **város** és az **utca, házszám** (mezőnként legfeljebb
64 karakter). Alattuk élő előnézet mutatja a címkét, a **Nyomtat** gombbal pedig
a három sor kinyomtatható, például:

```
John Doe
Hódmezővásárhely
Andrássy út 12.
```

Induláskor a mezőkben ez a mintaadat áll (`LabelData.Sample`), így azonnal
kipróbálható.

### Bevitel

- Mezőnként legfeljebb 64 karakter, a mező alatt számláló mutatja (pl. `12 / 64`).
- Minden mező egy sor a címkén: a beillesztett sortörések szóközzé alakulnak,
  a szélső szóközök nyomtatás előtt levágódnak.
- Mindhárom mező kötelező. Ha egy mező üres (vagy csak szóközt tartalmaz),
  alatta „Kötelező mező” hibaüzenet jelenik meg, és a nyomtatás nem indul el.
  A hiba akkor látszik, ha a mezőt már szerkesztették, vagy a **Nyomtat**
  gombot megnyomták, így induláskor nincs piros mező.
- Képernyőforgatáskor a beírt adatok megmaradnak.
- Tableten (és fekvő telefonon, 600 dp szélesség felett) két oszlop van: bal
  oldalt a mezők és alattuk a Nyomtat gomb, jobb oldalt az előnézet. Keskeny
  kijelzőn minden egymás alatt van.
- Hosszú szövegnél a betűméret automatikusan csökken, hogy a sor kiférjen a címkére.

## Technológia

- Kotlin, Jetpack Compose (Material 3)
- minSdk 26 (Android 8.0), targetSdk 37
- Külső függőség nincs, csak az Android SDK és az AndroidX alapkönyvtárak

## Nyomtatási megoldás és feltételezések

A feladat nem adja meg a címkenyomtató típusát és a kapcsolat módját, ezért az
alkalmazás az **Android beépített nyomtatási rendszerét** (`PrintManager`) használja:

- **Gyártófüggetlen.** Bármilyen nyomtatóval működik, amelyhez telepítve van
  Android nyomtatási szolgáltatás (pl. *Brother Print Service Plugin*,
  *Zebra Printer Setup Utility / PrintConnect*, *Mopria*). Wi-Fi-n,
  Bluetooth-on és USB-n is, attól függően, mit támogat a szolgáltatás.
- **Ékezethelyes.** A szöveget a telefon rajzolja ki vektoros PDF-be, nem a
  nyomtató saját betűkészlete, így az *ő*, *ű* és a többi ékezetes betű
  nyomtatótól függetlenül helyesen jelenik meg. (Nyers ZPL/ESC-POS parancsoknál
  ez külön kódlap-beállítást igényelne.)
- **Az előnézet és a nyomat egyezik.** A képernyős előnézet és a PDF ugyanazt a
  `LabelRenderer`-t használja.
- **Címkeméret.** Alapból 62 × 29 mm (Brother DK-11209), a `LabelSpec`-ben
  átállítható. A rendszer párbeszédablakában a nyomtató saját méretei közül is
  lehet választani. A tartalom az arányokat megtartva igazodik a kiválasztott
  papírhoz, a betűméret pedig úgy skálázódik, hogy a leghosszabb sor is kiférjen.

### Bővíthetőség

A felület csak a `LabelPrinter` interfészt ismeri. Ha a cél egy konkrét
nyomtató (pl. Brother SDK, Zebra Link-OS / ZPL hálózaton a 9100-as porton vagy
Bluetooth ESC/POS), elég egy új megvalósítást írni, és azt átadni a
`MainActivity`-ben. A felülethez nem kell hozzányúlni.

## Szerkezet

```
app/src/main/java/com/example/labelprinter/
├── MainActivity.kt              – belépési pont, összeköti a képernyőt a nyomtatóval
├── model/LabelData.kt           – a címke adatai, hosszkorlát, bemenet-tisztítás
├── print/
│   ├── LabelPrinter.kt          – nyomtatási interfész + eredménytípus
│   ├── SystemLabelPrinter.kt    – PrintManager-alapú megvalósítás
│   ├── LabelPrintAdapter.kt     – egyoldalas PDF előállítása a nyomtatáshoz
│   ├── LabelRenderer.kt         – közös rajzoló az előnézethez és a nyomtatáshoz
│   └── LabelSpec.kt             – címkeméret
└── ui/LabelScreen.kt            – az egyetlen képernyő (fejléc, beviteli mezők, előnézet, Nyomtat gomb)
```

## Fordítás és telepítés

1. Nyisd meg a projektet Android Studióban, és várd meg a Gradle-szinkront.
2. Futtatás: válassz eszközt vagy emulátort, majd **Run ▶**.
3. APK: **Build → Generate App Bundles or APKs → Generate APKs**. A fájl az
   `app/build/outputs/apk/debug/app-debug.apk` helyre kerül.

Parancssorból:

```bash
./gradlew assembleDebug        # APK
./gradlew testDebugUnitTest    # unit tesztek
```

## Tesztelés nyomtató nélkül

A **Nyomtat** gomb után a rendszer párbeszédablakában válaszd a
**Mentés PDF-ként** lehetőséget. A kapott PDF pontosan az, amit a nyomtató kapna.

## Hibakezelés

- Ha az eszközön nem érhető el a nyomtatási szolgáltatás, vagy a nyomtatás nem
  indítható, az alkalmazás nem omlik össze, hanem érthető üzenetet mutat
  (snackbar).
- A már elindított nyomtatási feladat állapotáról (sorban áll, kész, hiba)
  az Android rendszerértesítést küld.
