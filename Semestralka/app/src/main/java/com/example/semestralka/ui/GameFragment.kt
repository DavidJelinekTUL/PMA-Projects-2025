package com.example.semestralka.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semestralka.R
import com.example.semestralka.data.ElementEntity
import com.example.semestralka.data.ScoreEntry
import com.example.semestralka.databinding.FragmentGameBinding
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by viewModels { GameViewModel.Factory }
    private val cellViews = mutableMapOf<Int, View>()
    private val scoreAdapter = ScoreAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(context)
        binding.rvLeaderboard.adapter = scoreAdapter

        binding.btnSubmit.setOnClickListener {
            val text = binding.etInput.text.toString()
            if (text.isNotBlank()) {
                viewModel.submitGuess(text)
                binding.etInput.text.clear()
                hideKeyboard()
            }
        }

        binding.btnModeDaily.setOnClickListener { viewModel.startDailyChallenge() }

        binding.btnModeRandom.setOnClickListener { viewModel.startRandomGame() }

        binding.btnNextGame.setOnClickListener { viewModel.startRandomGame() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state -> render(state) }
        }
    }

    private fun render(state: UiState) {
        binding.btnModeDaily.setTypeface(null, if (state.isDailyMode) Typeface.BOLD else Typeface.NORMAL)
        binding.btnModeRandom.setTypeface(null, if (!state.isDailyMode) Typeface.BOLD else Typeface.NORMAL)

        binding.rvLeaderboard.isVisible = state.isDailyMode
        binding.tvLeaderboardTitle.isVisible = state.isDailyMode

        if (state.start != null && state.target != null) {
            binding.tvStatus.text = "Cíl: ${state.start.czechName} ➔ ${state.target.czechName}"
        } else {
            binding.tvStatus.text = state.message
        }
        binding.tvMovesCount.text = "Tahů: ${state.moves}"

        if (cellViews.isEmpty() && state.allElements.isNotEmpty()) {
            buildGrid(state.allElements)
        }
        updateGridColors(state)

        scoreAdapter.submitList(state.leaderboard)

        if (state.isWin) {
            binding.etInput.isEnabled = false
            binding.btnSubmit.isEnabled = false

            if (state.isDailyMode) {
                binding.btnNextGame.isVisible = false
                showWinDialog()
            } else {
                binding.btnNextGame.isVisible = true
            }
        } else {
            binding.etInput.isEnabled = true
            binding.btnSubmit.isEnabled = true
            binding.btnNextGame.isVisible = false
        }
    }

    private fun updateGridColors(state: UiState) {
        state.allElements.forEach { element ->
            val view = cellViews[element.atomicNumber] ?: return@forEach
            val symbolTv = view.findViewById<TextView>(R.id.cell_symbol)
            val container = view.findViewById<FrameLayout>(R.id.cell_root)

            val isStart = element == state.start
            val isTarget = element == state.target
            val isRevealed = state.revealed.contains(element)

            if (isRevealed) {
                symbolTv.text = element.symbol
                val bgColor = when {
                    isStart -> Color.parseColor("#4285F4")
                    isTarget && state.isWin -> Color.parseColor("#34A853")
                    isTarget -> Color.parseColor("#EA4335")
                    else -> Color.parseColor("#FBBC05")
                }
                symbolTv.setBackgroundColor(bgColor)
                symbolTv.setTextColor(Color.WHITE)

                if (isStart || isTarget) {
                    container.setPadding(8, 8, 8, 8)
                    container.setBackgroundColor(Color.BLACK)
                } else {
                    container.setPadding(2, 2, 2, 2)
                    container.setBackgroundColor(Color.WHITE)
                }
            } else {
                symbolTv.text = ""
                symbolTv.setBackgroundColor(Color.parseColor("#EEEEEE"))
                container.setPadding(2, 2, 2, 2)
                container.setBackgroundColor(Color.LTGRAY)
            }
        }
    }

    private fun buildGrid(elements: List<ElementEntity>) {
        val cellSize = 110
        val maxGroup = elements.maxOfOrNull { it.group } ?: 18
        val maxPeriod = elements.maxOfOrNull { it.period } ?: 7

        binding.gridContainer.removeAllViews()

        elements.forEach { element ->
            val cell = layoutInflater.inflate(R.layout.item_element_cell, binding.gridContainer, false)
            val params = FrameLayout.LayoutParams(cellSize, cellSize)
            params.leftMargin = (element.group - 1) * cellSize
            params.topMargin = (element.period - 1) * cellSize
            cell.layoutParams = params
            binding.gridContainer.addView(cell)
            cellViews[element.atomicNumber] = cell
        }
        binding.gridContainer.minimumWidth = maxGroup * cellSize
        binding.gridContainer.minimumHeight = maxPeriod * cellSize
    }

    private fun showWinDialog() {
        if (binding.etInput.tag == "dialogShown") return

        val input = EditText(context)
        input.hint = "Tvoje přezdívka"
        AlertDialog.Builder(context)
            .setTitle("Vítězství!")
            .setMessage("Zvládl jsi to na ${viewModel.state.value.moves} tahů. Ulož si skóre:")
            .setView(input)
            .setPositiveButton("Uložit") { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) viewModel.saveScore(name)
                binding.etInput.tag = "dialogShown"
            }
            .setCancelable(false)
            .show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ScoreAdapter : RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {
        private var list: List<ScoreEntry> = emptyList()
        fun submitList(newList: List<ScoreEntry>) { list = newList; notifyDataSetChanged() }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_score, p, false))
        override fun onBindViewHolder(h: ViewHolder, p: Int) {
            h.tvPos.text = "${p + 1}."; h.tvName.text = list[p].playerName; h.tvMoves.text = "${list[p].moves} tahů"
        }
        override fun getItemCount() = list.size
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvPos: TextView = v.findViewById(R.id.tv_position)
            val tvName: TextView = v.findViewById(R.id.tv_player_name)
            val tvMoves: TextView = v.findViewById(R.id.tv_moves)
        }
    }
}