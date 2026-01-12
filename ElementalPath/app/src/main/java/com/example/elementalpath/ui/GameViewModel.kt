package com.example.elementalpath.ui

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.elementalpath.GameApplication
import com.example.elementalpath.data.ElementEntity
import com.example.elementalpath.data.GameLog
import com.example.elementalpath.data.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

data class GameUiState(
    val allElements: List<ElementEntity> = emptyList(),
    // Množina prvků, které jsou "rozsvícené" (Start, Cíl + uhádnuté)
    val revealedElements: Set<ElementEntity> = emptySet(),
    val start: ElementEntity? = null,
    val target: ElementEntity? = null,
    val isWin: Boolean = false,
    val lastMessage: String = "" // Feedback pro uživatele (např. "Neznámý prvek")
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initData()
            repository.getAllElements().collect { list ->
                if (list.isNotEmpty()) startNewGame(list)
            }
        }
    }

    private fun startNewGame(list: List<ElementEntity>) {
        if (list.size < 2) return
        val shuffled = list.shuffled()
        val start = shuffled[0]
        val target = shuffled[1]

        _uiState.update {
            it.copy(
                allElements = list,
                start = start,
                target = target,
                // Na začátku svítí jen Start a Cíl
                revealedElements = setOf(start, target),
                isWin = false,
                lastMessage = "Spoj ${start.name} a ${target.name}"
            )
        }
    }

    // Hlavní herní akce: Uživatel zadal název
    fun submitGuess(guess: String) {
        if (_uiState.value.isWin) return

        val normalizedGuess = guess.trim()
        // Najdeme prvek podle anglického názvu (case-insensitive)
        val element = _uiState.value.allElements.find {
            it.name.equals(normalizedGuess, ignoreCase = true)
        }

        if (element != null) {
            // Prvek existuje -> přidáme do odhalených
            _uiState.update { state ->
                val newRevealed = state.revealedElements + element
                state.copy(
                    revealedElements = newRevealed,
                    lastMessage = "Odhaleno: ${element.symbol}"
                )
            }
            // Po odhalení zkontrolujeme, zda se tím propojila cesta
            checkWinCondition()
        } else {
            _uiState.update { it.copy(lastMessage = "Prvek '$guess' neexistuje!") }
        }
    }

    // Algoritmus pro kontrolu spojení (Breadth-First Search)
    private fun checkWinCondition() {
        val state = _uiState.value
        val start = state.start ?: return
        val target = state.target ?: return
        val revealed = state.revealedElements

        // Pokud start nebo cíl nejsou v odhalených (teoreticky nemožné), konec
        if (start !in revealed || target !in revealed) return

        // Fronta pro prohledávání
        val queue = ArrayDeque<ElementEntity>()
        queue.add(start)

        val visited = mutableSetOf<ElementEntity>()
        visited.add(start)

        var pathFound = false

        while (!queue.isEmpty()) {
            val current = queue.removeFirst()

            // Pokud jsme došli do cíle po odhalených prvcích -> VÍTĚZSTVÍ
            if (current == target) {
                pathFound = true
                break
            }

            // Najdi sousedy, kteří jsou TAKÉ odhalení
            val neighbors = revealed.filter { other ->
                isNeighbor(current, other) && !visited.contains(other)
            }

            for (neighbor in neighbors) {
                visited.add(neighbor)
                queue.add(neighbor)
            }
        }

        if (pathFound) {
            _uiState.update { it.copy(isWin = true, lastMessage = "VÍTĚZSTVÍ! Cesta spojena.") }
            saveLog(true)
        }
    }

    // Pomocná funkce: Jsou dva prvky vedle sebe v tabulce?
    private fun isNeighbor(e1: ElementEntity, e2: ElementEntity): Boolean {
        val dRow = abs(e1.period - e2.period)
        val dCol = abs(e1.group - e2.group)
        // Manhattan distance = 1 (pouze nahoře, dole, vlevo, vpravo)
        return (dRow + dCol) == 1
    }

    fun restart() {
        startNewGame(_uiState.value.allElements)
    }

    private fun saveLog(success: Boolean) {
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveLog(GameLog(
                start = s.start?.name ?: "",
                target = s.target?.name ?: "",
                steps = s.revealedElements.size, // Počet odhalených prvků jako skóre
                success = success
            ))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GameApplication)
                GameViewModel(app.repository)
            }
        }
    }
}