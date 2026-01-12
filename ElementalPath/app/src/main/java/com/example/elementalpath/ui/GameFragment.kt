package com.example.elementalpath.ui

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.elementalpath.R
import com.example.elementalpath.data.ElementEntity
import com.example.elementalpath.databinding.FragmentGameBinding
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by viewModels { GameViewModel.Factory }

    // Cache pro views
    private val elementViews = mutableMapOf<Int, View>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Listenery pro Input
        binding.btnSubmit.setOnClickListener { submitInput() }
        binding.btnRestart.setOnClickListener { viewModel.restart() }

        // Odeslání enterem na klávesnici
        binding.etGuess.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitInput()
                true
            } else false
        }

        // 2. Sledování stavu
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> render(state) }
        }
    }

    private fun submitInput() {
        val text = binding.etGuess.text.toString()
        if (text.isNotBlank()) {
            viewModel.submitGuess(text)
            binding.etGuess.text.clear() // Vymazat pole po zadání
        }
    }

    private fun render(state: GameUiState) {
        // Update textů
        binding.tvInstruction.text = state.lastMessage.ifEmpty { "Spoj ${state.start?.name} a ${state.target?.name}" }
        binding.tvStats.text = "Odhaleno: ${state.revealedElements.size} / ${state.allElements.size}"

        binding.btnRestart.isVisible = state.isWin
        binding.etGuess.isEnabled = !state.isWin
        binding.btnSubmit.isEnabled = !state.isWin

        // Inicializace gridu při prvním načtení dat
        if (elementViews.isEmpty() && state.allElements.isNotEmpty()) {
            buildGrid(state.allElements)
        }

        // Update barev buněk
        state.allElements.forEach { element ->
            val view = elementViews[element.atomicNumber] ?: return@forEach
            val symbolView = view.findViewById<TextView>(R.id.cell_symbol)

            val isRevealed = state.revealedElements.contains(element)
            val isStart = element == state.start
            val isTarget = element == state.target

            // Logika vzhledu:
            if (isRevealed) {
                // Odhalený prvek: Světlý, Symbol viditelný
                symbolView.text = element.symbol

                when {
                    isStart -> symbolView.setBackgroundColor(Color.parseColor("#4285F4")) // Modrá
                    isTarget -> symbolView.setBackgroundColor(Color.parseColor("#EA4335")) // Červená
                    else -> symbolView.setBackgroundColor(Color.parseColor("#81C995")) // Zelená (uhádnuto)
                }
                symbolView.setTextColor(Color.WHITE)
            } else {
                // Neodhalený prvek: Šedý, Symbol skrytý
                symbolView.text = "" // Schováme symbol
                symbolView.setBackgroundColor(Color.parseColor("#EEEEEE")) // Světle šedá
            }
        }
    }

    private fun buildGrid(elements: List<ElementEntity>) {
        binding.gridContainer.removeAllViews()
        elementViews.clear()
        val cellSize = 120

        elements.forEach { element ->
            val cellView = layoutInflater.inflate(R.layout.item_element_cell, binding.gridContainer, false)
            val params = cellView.layoutParams as FrameLayout.LayoutParams
            params.width = cellSize
            params.height = cellSize
            params.leftMargin = (element.group - 1) * cellSize
            params.topMargin = (element.period - 1) * cellSize
            cellView.layoutParams = params

            // Kliknutí na buňku (volitelné - třeba pro zobrazení info "Tohle je neodhalené")
            // cellView.setOnClickListener { ... }

            binding.gridContainer.addView(cellView)
            elementViews[element.atomicNumber] = cellView
        }

        val maxGroup = elements.maxOfOrNull { it.group } ?: 18
        val maxPeriod = elements.maxOfOrNull { it.period } ?: 10
        binding.gridContainer.minimumWidth = maxGroup * cellSize
        binding.gridContainer.minimumHeight = maxPeriod * cellSize
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}