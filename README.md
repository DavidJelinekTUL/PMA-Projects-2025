# Technická dokumentace a Implementační manuál: Periodic Table Game

Tento dokument slouží jako kompletní průvodce implementací Android aplikace v Kotlinu. Architektura sleduje vzor **MVVM** (Model-View-ViewModel) a využívá **Room** pro lokální persistenci a **Firebase Firestore** pro sdílený online leaderboard.

## Obsah
1.  [Nastavení prostředí a Gradle](#1-nastavení-prostředí-a-gradle)
2.  [Datová vrstva - Lokální (Room)](#2-datová-vrstva---lokální-room)
3.  [Datová vrstva - Vzdálená (Firestore)](#3-datová-vrstva---vzdálená-firestore)
4.  [Repository a Inicializace dat](#4-repository-a-inicializace-dat)
5.  [Core a Dependency Injection](#5-core-a-dependency-injection)
6.  [Logická vrstva (ViewModel)](#6-logická-vrstva-viewmodel)
7.  [Prezentační vrstva (UI)](#7-prezentační-vrstva-ui)
8.  [Spuštění a Navigace](#8-spuštění-a-navigace)

---

## 1. Nastavení prostředí a Gradle

Než začneme psát kód, musíme připravit build systém. Aplikace vyžaduje zpracování anotací (KSP) pro databázi a pluginy pro Firebase.

### 1.1 Project-level `build.gradle.kts`
Definujeme verze pluginů pro celý projekt.
* **KSP (Kotlin Symbol Processing):**. Je nutný pro generování kódu Room databáze. Verze KSP musí striktně odpovídat verzi Kotlinu.
* **Google Services:** Nutné pro komunikaci s Firebase.

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
}
```

### 1.2 Module-level `app/build.gradle.kts`
Zde aplikujeme pluginy a přidáváme konkrétní knihovny.

**Klíčové nastavení:**
* `viewBinding = true`: Generuje vazební třídy pro XML layouty (např. `FragmentGameBinding`), což eliminuje `findViewById` a zvyšuje bezpečnost typů.
* `ksp(...)`: Říká kompilátoru, aby zpracoval anotace Room.

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.semestralka"
    compileSdk = 36 

    defaultConfig {
        applicationId = "com.example.semestralka"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // UI a Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigace a Lifecycle (ViewModel, LiveData)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Firebase (BOM hlídá kompatibilitu verzí)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-firestore")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Podpora Coroutines
    ksp("androidx.room:room-compiler:$roomVersion")      // Generátor kódu
}
```

### 1.3 AndroidManifest.xml
Nutné přidat oprávnění k internetu pro Firebase a zaregistrovat vlastní třídu `GameApplication`.

```xml
<manifest xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    xmlns:tools="[http://schemas.android.com/tools](http://schemas.android.com/tools)">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".core.GameApplication" 
        ... >
        </application>
</manifest>
```

---

## 2. Datová vrstva - Lokální (Room)

Tato vrstva definuje schéma SQL databáze pomocí Kotlin objektů.

### 2.1 Entita (Tabulka)
**Soubor:** `data/ElementEntity.kt`
Třída reprezentuje jeden řádek v tabulce `elements`.
* `@Entity`: Označuje třídu jako tabulku.
* `period` a `group`: Jsou kritické pro pozdější vykreslení mřížky (řádek a sloupec v periodické tabulce).

```kotlin
package com.example.semestralka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "elements")
data class ElementEntity(
    @PrimaryKey val atomicNumber: Int, // Unikátní ID (protonové číslo)
    val symbol: String,
    val name: String,
    val czechName: String,
    val period: Int,
    val group: Int
)
```

### 2.2 DAO (Data Access Object)
**Soubor:** `data/GameDao.kt`
Rozhraní definující SQL dotazy. Room automaticky vygeneruje implementaci tohoto rozhraní.
* `Flow<List<...>>`: Reaktivní stream. Jakmile se data v DB změní, UI se automaticky aktualizuje.
* `suspend`: Funkce se vykonávají v na pozadí.

```kotlin
package com.example.semestralka.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM elements")
    fun getAllElements(): Flow<List<ElementEntity>>

    // Pokud prvek již existuje, přepíše se (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElements(elements: List<ElementEntity>)

    @Query("SELECT COUNT(*) FROM elements")
    suspend fun getCount(): Int
}
```

### 2.3 Databáze
**Soubor:** `data/GameDatabase.kt`
Vytváří instanci databáze. Používá Singleton pattern, aby v celé aplikaci existovalo pouze jedno připojení k databázi (drahá operace).

```kotlin
package com.example.semestralka.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ElementEntity::class], version = 5, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var Instance: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GameDatabase::class.java, "semestralka_db")
                    .fallbackToDestructiveMigration() // Při změně verze smaže stará data
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

---

## 3. Datová vrstva - Vzdálená (Firestore)

Slouží pouze pro ukládání a načítání výsledků.

**Soubor:** `data/ScoreEntry.kt`
Jednoduchá datová třída, kterou Firestore umí automaticky serializovat do JSON dokumentu.
* `dateId`: Slouží k filtrování žebříčků podle dne (např. "2025-01-12").

```kotlin
package com.example.semestralka.data

import com.google.firebase.Timestamp

data class ScoreEntry(
    val playerName: String = "",
    val moves: Int = 0,
    val dateId: String = "", 
    val timestamp: Timestamp = Timestamp.now()
)
```

---

## 4. Repository a Inicializace dat

Repository funguje jako prostředník. UI neví, jestli data tečou z Room nebo Firestore, ptá se pouze Repository.

**Soubor:** `data/GameRepository.kt`
Klíčovou funkcí je `initData()`. Protože Room je po instalaci prázdný, tato metoda zkontroluje počet záznamů a pokud je 0, vloží natvrdo definovaný seznam chemických prvků.

```kotlin
package com.example.semestralka.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class GameRepository(
    private val dao: GameDao,
    private val firestore: FirebaseFirestore
) {
    fun getElements(): Flow<List<ElementEntity>> = dao.getAllElements()

    suspend fun initData() {
        if (dao.getCount() == 0) {
            val elements = listOf(
                // Zde vložte celý seznam prvků (zkráceno pro přehlednost)...
                ElementEntity(1, "H", "Hydrogen", "Vodík", 1, 1),
                ElementEntity(2, "He", "Helium", "Helium", 1, 18),
                // ... doplňte zbytek tabulky ...
                ElementEntity(118, "Og", "Oganesson", "Oganesson", 7, 18)
            )
            dao.insertElements(elements)
        }
    }

    // Uložení skóre do cloudu
    suspend fun saveScore(score: ScoreEntry) {
        try {
            firestore.collection("leaderboard").add(score).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Načtení TOP 10 pro konkrétní den
    suspend fun getDailyLeaderboard(dateId: String): List<ScoreEntry> {
        return try {
            val snapshot = firestore.collection("leaderboard")
                .whereEqualTo("dateId", dateId)
                .orderBy("moves", Query.Direction.ASCENDING)
                .limit(10)
                .get().await()
            snapshot.toObjects(ScoreEntry::class.java)
        } catch (e: Exception) { emptyList() }
    }
}
```

---

## 5. Core a Dependency Injection

Jednoduchá implementace Service Locator patternu. Místo složitých knihoven si vytvoříme závislosti ručně v třídě `Application`.

**Soubor:** `core/GameApplication.kt`
Tato třída žije po celou dobu běhu aplikace.

```kotlin
package com.example.semestralka.core

import android.app.Application
import com.example.semestralka.data.GameDatabase
import com.example.semestralka.data.GameRepository
import com.google.firebase.firestore.FirebaseFirestore

class GameApplication : Application() {
    // Lazy inicializace DB až když je potřeba
    val database by lazy { GameDatabase.getDatabase(this) }

    // Repository je připraveno pro použití ve ViewModelu
    val repository by lazy {
        GameRepository(
            dao = database.gameDao(),
            firestore = FirebaseFirestore.getInstance()
        )
    }
}
```

---

## 6. Logická vrstva (ViewModel)

Srdce aplikace. Řídí stav hry, pravidla a kontrolu vítězství.

**Soubor:** `ui/GameViewModel.kt`

### 6.1 State Management (UiState)
Veškerá data pro UI jsou v jednom immutable objektu. Když se změní cokoliv (počet tahů, zpráva), vygeneruje se nový `UiState` a UI se překreslí.

```kotlin
data class UiState(
    val allElements: List<ElementEntity> = emptyList(),
    val start: ElementEntity? = null,
    val target: ElementEntity? = null,
    val revealed: Set<ElementEntity> = emptySet(), // Množina odhalených prvků
    val leaderboard: List<ScoreEntry> = emptyList(),
    val isWin: Boolean = false,
    val moves: Int = 0,
    val message: String = "Načítám...",
    val isDailyMode: Boolean = true
)
```

### 6.2 Game Logic & Seeding
Pro **Daily Challenge** chceme, aby všichni hráči měli stejný Start a Cíl.
* `LocalDate.now().toEpochDay()` vrátí číslo (počet dní od roku 1970).
* Toto číslo použijeme jako `seed` pro `Random`. Díky tomu `rng.nextInt()` vrátí na každém zařízení stejnou sekvenci čísel pro daný den.

```kotlin
// ... uvnitř ViewModelu ...
fun startDailyChallenge(list: List<ElementEntity> = _state.value.allElements) {
    val seed = LocalDate.now().toEpochDay() 
    val rng = Random(seed) // Deterministický generátor pro daný den
    generateGame(list, rng, isDaily = true)
    loadLeaderboard()
}
```

### 6.3 Algoritmus kontroly výhry (BFS)
Musíme zjistit, zda existuje cesta od `Start` k `Target` pouze přes `revealed` (odhalené) prvky. Používáme **Breadth-First Search**.

```kotlin
private fun checkWinInternal(start: ElementEntity?, target: ElementEntity?, revealed: Set<ElementEntity>): Boolean {
    if (start == null || target == null) return false
    // Pokud start nebo cíl nejsou odhaleny, není co řešit
    if (!revealed.contains(start) || !revealed.contains(target)) return false

    val queue = ArrayDeque<ElementEntity>()
    queue.add(start)
    val visited = mutableSetOf<ElementEntity>()
    visited.add(start)

    while (!queue.isEmpty()) {
        val current = queue.removeFirst()
        if (current == target) return true // Cesta nalezena!

        // Najdi sousedy v odhalených prvcích
        val neighbors = revealed.filter { other ->
            !visited.contains(other) && areNeighbors(current, other)
        }
        for (neighbor in neighbors) {
            visited.add(neighbor)
            queue.add(neighbor)
        }
    }
    return false
}

// Manhattan distance = 1 znamená, že prvky jsou vedle sebe (ne diagonálně)
private fun areNeighbors(e1: ElementEntity, e2: ElementEntity): Boolean {
    val dRow = abs(e1.period - e2.period)
    val dCol = abs(e1.group - e2.group)
    return (dRow + dCol) == 1
}
```

---

## 7. Prezentační vrstva (UI)

### 7.1 XML Layouty
* `fragment_game.xml`: Obsahuje `ScrollView` a uvnitř `FrameLayout` (id: `gridContainer`), do kterého budeme vkládat buňky.
* `item_element_cell.xml`: Malý čtverec reprezentující prvek.
* `item_score.xml`, `activity_main.xml`, `nav_graph.xml`: Standardní pomocné soubory.

### 7.2 GameFragment
**Soubor:** `ui/GameFragment.kt`
Zde probíhá dynamické generování mřížky. Periodická tabulka má specifický tvar (mezery), proto ji nelze snadno udělat pomocí `GridLayout`.

**Metoda `buildGrid`:**
Vypočítá přesnou pozici každého prvku na základě jeho `group` (x) a `period` (y).

```kotlin
private fun buildGrid(elements: List<ElementEntity>) {
    val cellSize = 110 // Velikost buňky v pixelech
    binding.gridContainer.removeAllViews()

    elements.forEach { element ->
        // Nafoukneme View pro jednu buňku
        val cell = layoutInflater.inflate(R.layout.item_element_cell, binding.gridContainer, false)
        
        // Nastavíme absolutní pozici pomocí Marginů
        val params = FrameLayout.LayoutParams(cellSize, cellSize)
        params.leftMargin = (element.group - 1) * cellSize
        params.topMargin = (element.period - 1) * cellSize
        cell.layoutParams = params
        
        binding.gridContainer.addView(cell)
        cellViews[element.atomicNumber] = cell // Uložíme referenci pro pozdější barvení
    }
    // Nastavíme velikost plátna, aby fungoval scroll
    binding.gridContainer.minimumWidth = 18 * cellSize
    binding.gridContainer.minimumHeight = 7 * cellSize
}
```

**Metoda `updateGridColors`:**
Projiteruje všechny vytvořené Views a obarví je podle stavu ve `viewModel.state` (Modrá=Start, Zelená=Výhra, atd.).

---

## 8. Spuštění a Navigace

### 8.1 MainActivity
**Soubor:** `MainActivity.kt`
Slouží jako obálka. Řeší **Edge-to-Edge** zobrazení (aby se aplikace vykreslovala i pod status barem a navigační lištou, ale ovládací prvky se nepřekrývaly).

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Aktivace full-screen módu
        setContentView(R.layout.activity_main)

        // Nastavení paddingů podle systémových lišt (Insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
```

### 8.2 Firebase Konfigurace
1.  Stáhněte `google-services.json` z Firebase Console.
2.  Vložte jej do složky `app/` v projektu.
3.  Spusťte aplikaci.
4.  Při prvním pokusu o zobrazení leaderboardu zkontrolujte **Logcat**. Firebase vypíše chybu s odkazem na vytvoření **Indexu** (protože filtrujeme podle data A ZÁROVEŇ řadíme podle skóre). Klikněte na odkaz v logu pro automatické vytvoření indexu.
