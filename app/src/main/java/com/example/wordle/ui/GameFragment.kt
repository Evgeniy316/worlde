package com.example.wordlу.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wordl.adapter.TileAdapter
import com.example.wordle.databinding.FragmentGameBinding
import com.example.wordle.viewmodel.GameViewModel

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by activityViewModels()
    private val adapter = TileAdapter()

    // ←←← ИСПРАВЛЕННЫЙ TextWatcher (главное изменение)
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // Убираем слушатель, чтобы избежать рекурсии
            binding.etInput.removeTextChangedListener(this)

            val filtered = s.toString()
                .uppercase()
                .filter { it in 'А'..'Я' || it == 'Ё' }
                .take(5)

            // Если текст изменился после фильтрации — ставим очищенный
            if (filtered != s.toString()) {
                binding.etInput.setText(filtered)
                binding.etInput.setSelection(filtered.length)
            }

            // Возвращаем слушатель обратно
            binding.etInput.addTextChangedListener(this)

            // Передаём в ViewModel (для показа в сетке)
            viewModel.setCurrentInput(filtered)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)

        // RecyclerView
        binding.rvBoard.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvBoard.adapter = adapter

        viewModel.board.observe(viewLifecycleOwner) { adapter.update(it) }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                binding.tvMessage.text = msg
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.gameOver.observe(viewLifecycleOwner) { isOver ->
            binding.btnGuess.isEnabled = !isOver
            binding.etInput.isEnabled = !isOver
            binding.btnNewGame.visibility = if (isOver) View.VISIBLE else View.GONE
        }

        // ←←← Подключаем исправленный watcher
        binding.etInput.addTextChangedListener(inputWatcher)

        // Кнопка Проверить
        binding.btnGuess.setOnClickListener {
            viewModel.submitGuess()
            // После отправки очищаем поле
            binding.etInput.text?.clear()
        }

        // Кнопка Новая игра
        binding.btnNewGame.setOnClickListener {
            viewModel.startNewGame()
            binding.etInput.text?.clear()
            binding.tvMessage.text = ""
        }

        return binding.root
    }

    override fun onDestroyView() {
        // Важно! Убираем слушатель при уничтожении фрагмента
        binding.etInput.removeTextChangedListener(inputWatcher)
        super.onDestroyView()
        _binding = null
    }
}