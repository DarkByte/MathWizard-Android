package com.example.mathwizard
import android.os.CountDownTimer
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

data class QuizManager(val tables: IntArray, private val operation: String) {

    val countdownSeconds = MutableLiveData<Int>()
    var coolDown = MutableLiveData<Int>()
    val quizScore = MutableLiveData<Int>()
    val answerText = MutableLiveData<String>()

    private val maxQuestionTime = 12
    private val coolDownTime = 2
    private val questionSecond = 1000

    private lateinit var questionTimer: CountDownTimer
    private lateinit var questionCoolDown: CountDownTimer

    private var questions = ArrayList<QuestionData>()
    private var questionIndex: Int = -1
    private var questionsPerTable: Int = 4
    private var needsScoreUpdate: Boolean = true
    private var isInCoolDown: Boolean = false

    // LOGIC
    fun questionText(): String {
        if (isOutOfRange()) {
            return ""
        }
        return questions[questionIndex].asQuestionText()
    }

    fun questionAnswer(): String {
        if (isOutOfRange()) {
            return ""
        }
        return questions[questionIndex].asCompleteAnswer()
    }

    fun questionResult(): Int {
        if (isOutOfRange()) {
            return 0
        }
        return questions[questionIndex].result
    }

    private fun isOutOfRange(): Boolean {
        return (questionIndex >= questions.count()) || (questionIndex < 0)
    }

    fun isTooLate(): Boolean {
        return isInCoolDown
    }

    fun newKeypress(key: String) {
        if (isInCoolDown) {
            return
        }

        if (key.isDigitsOnly()) {
            if (answerText.value!!.length < 3) {
                answerText.value += key
            }
        } else {
            answerText.value = answerText.value?.dropLast(1)
        }
    }

    fun finishQuestion() {
        isInCoolDown = true
        questionTimer.cancel()
        questionCoolDown.start()

        var questionScore = countdownSeconds.value!!
        if (questionScore < 0) {
            questionScore = 0
        }

        if (needsScoreUpdate) {
            quizScore.value = quizScore.value?.plus(questionScore)
            needsScoreUpdate = false
        }
    }

    fun finishQuiz() {
        questionIndex = 0
        answerText.value = ""
        questionTimer.cancel()
        questionCoolDown.cancel()
    }

    fun score(): Int {
        return quizScore.value!!
    }

    fun starsAchieved(): Int {
        return score().div(tables.count()).div(15)
    }

    fun canMoveToNextQuestion(): Boolean {
        questionCoolDown.cancel()
        questionIndex++
        answerText.value = ""
        if (questionIndex < questions.count()) {
            restartQuestionTimer()
            return true
        }

        questionIndex--
        return false
    }

    // UI
    fun starsAt(second: Int): Int {
        return when ((second - 1).div(3)) {
            3 -> R.drawable.stars3
            2 -> R.drawable.stars2
            1 -> R.drawable.stars1
            else -> 0
        }
    }

    fun setup() {
        setupTimers()
        answerText.value = ""
        countdownSeconds.value = -1
        quizScore.value = 0

        var randomItem: Int
        var data: QuestionData
        var itemCount: Int

        for (table: Int in tables) {
            itemCount = 0
            while (itemCount <= questionsPerTable) {
                randomItem = 1 + (abs(ThreadLocalRandom.current().nextInt()).rem(10))
                when (operation) {
                    "-" -> randomItem += table - 1
                    "/" -> randomItem *= table
                }

                data = QuestionData(randomItem, table, operation)
                itemCount += addNewQuestion(data)
            }
        }
        questions.shuffle()
    }

    // PRIVATE
    private fun addNewQuestion(question: QuestionData): Int {
        for (data: QuestionData in questions) {
            if (data.isSame(question)) {
                return 0
            }
        }

        questions.add(question)
        return 1
    }

    private fun setupTimers() {
        questionTimer = object: CountDownTimer((maxQuestionTime * questionSecond).toLong(), questionSecond.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                countdownSeconds.value = (millisUntilFinished / questionSecond).toInt()
            }

            override fun onFinish() {
                countdownSeconds.value = -1
            }
        }

        val coolDownDuration = (coolDownTime * questionSecond).toLong()
        questionCoolDown = object: CountDownTimer(coolDownDuration, coolDownDuration) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                coolDown.value = questionIndex + 1
            }
        }
    }

    private fun restartQuestionTimer() {
        countdownSeconds.value = maxQuestionTime
        questionTimer.start()
        needsScoreUpdate = true
        isInCoolDown = false
    }
}