package com.example.semestralka.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.semestralka.core.GameApplication
import com.example.semestralka.data.ElementEntity
import com.example.semestralka.data.GameRepository
import com.example.semestralka.data.ScoreEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Random
import kotlin.math.abs

data class UiState(
    val allElements: List<ElementEntity> = emptyList(),
    val start: ElementEntity? = null,
    val target: ElementEntity? = null,
    val revealed: Set<ElementEntity> = emptySet(),
    val leaderboard: List<ScoreEntry> = emptyList(),
    val isWin: Boolean = false,
    val moves: Int = 0,
    val message: String = "Načítám...",
    val isDailyMode: Boolean = true // True = Daily, False = Trénink
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private val todayId = LocalDate.now().toString()

    init {
        viewModelScope.launch {
            repository.initData()
            repository.getElements().collect { list ->
                if (list.isNotEmpty()) {
                    if (_state.value.start == null) {
                        startDailyChallenge(list)
                    } else {
                        _state.update { it.copy(allElements = list) }
                    }
                }
            }
        }
    }

    fun startDailyChallenge(list: List<ElementEntity> = _state.value.allElements) {
        if (list.isEmpty()) return

        val seed = LocalDate.now().toEpochDay()
        val rng = Random(seed)

        generateGame(list, rng, isDaily = true)
        loadLeaderboard()
    }

    fun startRandomGame(list: List<ElementEntity> = _state.value.allElements) {
        if (list.isEmpty()) return

        val rng = Random()

        generateGame(list, rng, isDaily = false)
        _state.update { it.copy(leaderboard = emptyList()) }
    }

    private fun generateGame(list: List<ElementEntity>, rng: Random, isDaily: Boolean) {
        val start = list[rng.nextInt(list.size)]
        var target = list[rng.nextInt(list.size)]

        while (target == start) target = list[rng.nextInt(list.size)]

        _state.update {
            it.copy(
                allElements = list,
                start = start,
                target = target,
                revealed = setOf(start, target),
                message = if (isDaily) "Daily Challenge" else "Trénink - Nekonečná hra",
                moves = 0,
                isWin = false,
                isDailyMode = isDaily
            )
        }
    }


    fun submitGuess(guess: String) {
        if (_state.value.isWin) return

        val input = guess.trim()
        val currentState = _state.value

        val element = currentState.allElements.find {
            it.symbol.equals(input, ignoreCase = true) ||
                    it.name.equals(input, ignoreCase = true) ||
                    it.czechName.equals(input, ignoreCase = true)
        }

        if (element != null) {
            _state.update { state ->
                val newRevealed = state.revealed + element
                val newMoves = if (state.revealed.contains(element)) state.moves else state.moves + 1
                val win = checkWinInternal(state.start, state.target, newRevealed)

                state.copy(
                    revealed = newRevealed,
                    moves = newMoves,
                    isWin = win,
                    message = if (win) "VÍTĚZSTVÍ!" else "Odhaleno: ${element.czechName}"
                )
            }
        } else {
            _state.update { it.copy(message = "Neznámý prvek: $input") }
        }
    }

    private fun areNeighbors(e1: ElementEntity, e2: ElementEntity): Boolean {
        val dRow = abs(e1.period - e2.period)
        val dCol = abs(e1.group - e2.group)
        return (dRow + dCol) == 1
    }

    private fun checkWinInternal(start: ElementEntity?, target: ElementEntity?, revealed: Set<ElementEntity>): Boolean {
        if (start == null || target == null) return false
        if (!revealed.contains(start) || !revealed.contains(target)) return false

        val queue = ArrayDeque<ElementEntity>()
        queue.add(start)
        val visited = mutableSetOf<ElementEntity>()
        visited.add(start)

        while (!queue.isEmpty()) {
            val current = queue.removeFirst()
            if (current == target) return true

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

    // --- UKLÁDÁNÍ SKÓRE ---

    fun saveScore(playerName: String) {
        if (!_state.value.isDailyMode) return

        val entry = ScoreEntry(playerName, _state.value.moves, todayId)
        _state.update {
            val updatedList = (it.leaderboard + entry).sortedBy { s -> s.moves }.take(10)
            it.copy(leaderboard = updatedList)
        }
        viewModelScope.launch { try { repository.saveScore(entry); loadLeaderboard() } catch (e: Exception) {} }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch { val scores = repository.getDailyLeaderboard(todayId); _state.update { it.copy(leaderboard = scores) } }
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