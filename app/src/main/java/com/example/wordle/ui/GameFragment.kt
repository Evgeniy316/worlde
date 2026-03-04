package com.example.wordle.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wordle.R
import com.example.wordle.adapter.TileAdapter
import com.example.wordle.databinding.FragmentGameBinding
import com.example.wordle.viewmodel.GameViewModel
import com.google.android.material.button.MaterialButton

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by activityViewModels()
    private val adapter = TileAdapter()

    private val letterButtons = mutableMapOf<Char, MaterialButton>()
    private val actionButtons = mutableListOf<MaterialButton>()

    // Ряды русской клавиатуры
    private val row1 = "ЙЦУКЕНГШЩЗ"
    private val row2 = "ФЫВАПРОЛДЖЭ"
    private val row3 = "ЯЧСМИТЬБЮ"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)

        // Скрываем обычное поле ввода — теперь используем виртуальную клавиатуру
        binding.etInput.visibility = View.GONE

        // RecyclerView
        binding.rvBoard.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvBoard.adapter = adapter

        viewModel.board.observe(viewLifecycleOwner) { adapter.update(it) }

        viewModel.absentLetters.observe(viewLifecycleOwner) { absent ->
            updateKeyboardColors(absent)
        }

        viewModel.currentInput.observe(viewLifecycleOwner) { input ->
            updateKeyboardEnabledState(input)
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                binding.tvMessage.text = msg
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.gameOver.observe(viewLifecycleOwner) { isOver ->
            binding.btnGuess.isEnabled = !isOver
            binding.btnNewGame.visibility = if (isOver) View.VISIBLE else View.GONE
            binding.keyboardLayout.visibility = if (isOver) View.GONE else View.VISIBLE
            updateKeyboardEnabledState(viewModel.currentInput.value ?: "")
        }

        setupKeyboard()

        // Кнопка Проверить
        binding.btnGuess.setOnClickListener {
            viewModel.submitGuess()
        }

        // Кнопка Новая игра
        binding.btnNewGame.setOnClickListener {
            viewModel.startNewGame()
            binding.tvMessage.text = ""
        }

        return binding.root
    }

    private fun setupKeyboard() {
        val rows = listOf(
            row1 to binding.keyboardRow1,
            row2 to binding.keyboardRow2,
            row3 to binding.keyboardRow3
        )

        rows.forEach { (letters, layout) ->
            addLetterButtons(letters, layout)
        }

        // Кнопки действий: удалить последнюю букву и очистить строку
        val actionsLayout = binding.keyboardRowActions
        addActionButtons(actionsLayout)
    }

    private fun addLetterButtons(letters: String, layout: LinearLayout) {
        letters.forEach { ch ->
            val button = MaterialButton(requireContext()).apply {
                text = ch.toString()
                textSize = 16f
                setTextColor(Color.WHITE)
                setOnClickListener { onLetterClicked(ch) }
            }
            tintButton(button, R.color.dark)
            letterButtons[ch] = button

            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            params.marginStart = 4
            params.marginEnd = 4
            layout.addView(button, params)
        }
    }

    private fun addActionButtons(layout: LinearLayout) {
        val backspaceButton = MaterialButton(requireContext()).apply {
            text = "⌫"
            textSize = 16f
            setTextColor(Color.WHITE)
            setOnClickListener { onBackspaceClicked() }
        }

        val clearButton = MaterialButton(requireContext()).apply {
            text = "Очистить"
            textSize = 16f
            setTextColor(Color.WHITE)
            setOnClickListener { onClearClicked() }
        }

        tintButton(backspaceButton, R.color.dark)
        tintButton(clearButton, R.color.dark)
        actionButtons.clear()
        actionButtons.add(backspaceButton)
        actionButtons.add(clearButton)

        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        params.marginStart = 4
        params.marginEnd = 4

        layout.addView(backspaceButton, params)
        layout.addView(clearButton, params)
    }

    private fun onLetterClicked(ch: Char) {
        if (viewModel.gameOver.value == true) return

        val current = viewModel.currentInput.value ?: ""
        if (current.length < 5) {
            viewModel.setCurrentInput(current + ch)
        }
    }

    private fun onBackspaceClicked() {
        if (viewModel.gameOver.value == true) return

        val current = viewModel.currentInput.value ?: ""
        if (current.isNotEmpty()) {
            viewModel.setCurrentInput(current.dropLast(1))
        }
    }

    private fun onClearClicked() {
        if (viewModel.gameOver.value == true) return
        viewModel.setCurrentInput("")
    }

    private fun updateKeyboardEnabledState(input: String) {
        val isOver = viewModel.gameOver.value == true
        val lettersEnabled = !isOver && input.length < 5
        val actionsEnabled = !isOver

        letterButtons.values.forEach { btn ->
            btn.isEnabled = lettersEnabled
            btn.alpha = if (lettersEnabled) 1f else 0.5f
        }

        actionButtons.forEach { btn ->
            btn.isEnabled = actionsEnabled
            btn.alpha = if (actionsEnabled) 1f else 0.5f
        }
    }

    private fun updateKeyboardColors(absent: Set<Char>) {
        letterButtons.forEach { (ch, button) ->
            val colorRes = if (ch in absent) R.color.absent_gray else R.color.dark
            tintButton(button, colorRes)
        }
    }

    private fun tintButton(button: MaterialButton, colorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), colorRes)
        button.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}