package com.example.wordle.viewmodel
import androidx.lifecycle.*
import com.example.wordle.R
import com.example.wordle.data.GameWords
import com.example.wordle.data.Tile
import com.example.wordle.data.TileColor

class GameViewModel : ViewModel() {

    private val _targetWord = MutableLiveData<String>()
    val targetWord: LiveData<String> = _targetWord

    private val _board = MutableLiveData<List<Tile>>(List(30) { Tile(' ', TileColor.GRAY) })
    val board: LiveData<List<Tile>> = _board

    private val _currentInput = MutableLiveData("")
    val currentInput: LiveData<String> = _currentInput

    private val _message = MutableLiveData<UiMessage?>(null)
    val message: LiveData<UiMessage?> = _message

    private val _gameOver = MutableLiveData(false)
    val gameOver: LiveData<Boolean> = _gameOver

    private val _isWin = MutableLiveData(false)
    val isWin: LiveData<Boolean> = _isWin

    private val _absentLetters = MutableLiveData<Set<Char>>(emptySet())
    val absentLetters: LiveData<Set<Char>> = _absentLetters

    private var attempt = 0
    private val guesses = mutableListOf<List<Tile>>()

    init { startNewGame() }

    fun startNewGame() {
        _targetWord.value = GameWords.words.random()
        guesses.clear()
        _currentInput.value = ""
        attempt = 0
        _gameOver.value = false
        _isWin.value = false
        _message.value = null
        _absentLetters.value = emptySet()
        rebuildBoard()
    }

    fun setCurrentInput(input: String) {
        if (!_gameOver.value!! && input.length <= 5) {
            _currentInput.value = input
            _message.value = null
            rebuildBoard()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun submitGuess() {
        val guess = _currentInput.value!!.uppercase()
        if (guess.length != 5) {
            _message.value = UiMessage(R.string.error_length)
            return
        }

        val target = _targetWord.value!!
        val colors = evaluateGuess(guess, target)
        updateAbsentLetters(guess, colors)

        val newRow = guess.mapIndexed { i, c -> Tile(c, colors[i]) }
        guesses.add(newRow)
        attempt++

        if (guess == target) {
            _gameOver.value = true
            _isWin.value = true
            _message.value = UiMessage(R.string.win_message)
        } else if (attempt >= 6) {
            _gameOver.value = true
            _isWin.value = false
            _message.value = UiMessage(R.string.lose_message_with_word, target)
        }

        _currentInput.value = ""
        rebuildBoard()
    }

    private fun evaluateGuess(guess: String, target: String): List<TileColor> {
        val result = MutableList(5) { TileColor.GRAY }
        val targetLetters = target.toMutableList()

        for (i in guess.indices) {
            if (guess[i] == target[i]) {
                result[i] = TileColor.GREEN
                targetLetters[i] = ' '
            }
        }
        for (i in guess.indices) {
            if (result[i] == TileColor.GRAY) {
                val idx = targetLetters.indexOf(guess[i])
                if (idx != -1) {
                    result[i] = TileColor.YELLOW
                    targetLetters[idx] = ' '
                }
            }
        }
        return result
    }

    private fun rebuildBoard() {
        val newBoard = MutableList(30) { Tile(' ', TileColor.GRAY) }

        guesses.forEachIndexed { row, rowTiles ->
            rowTiles.forEachIndexed { col, tile ->
                newBoard[row * 5 + col] = tile
            }
        }

        val curr = _currentInput.value!!
        val currentRow = guesses.size
        curr.forEachIndexed { col, c ->
            newBoard[currentRow * 5 + col] = Tile(c, TileColor.GRAY)
        }

        _board.value = newBoard
    }

    private fun updateAbsentLetters(guess: String, colors: List<TileColor>) {
        val absent = (_absentLetters.value ?: emptySet()).toMutableSet()

        val presentInThisGuess = mutableSetOf<Char>()
        guess.forEachIndexed { i, c ->
            if (colors[i] != TileColor.GRAY) presentInThisGuess.add(c)
        }

        guess.forEachIndexed { i, c ->
            if (colors[i] == TileColor.GRAY && c !in presentInThisGuess) {
                absent.add(c)
            }
        }

        _absentLetters.value = absent
    }
}