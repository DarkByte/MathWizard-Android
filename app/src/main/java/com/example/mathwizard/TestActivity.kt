package com.example.mathwizard

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.final_score.*


class TestActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        startTesting()
    }

    override fun onClick(view: View?) {
        Sound.tap()
        if (view!!.tag == getString(R.string.valExit)) {
            showExitDialog()
        } else {
            quizManager.newKeypress(view.tag as String)
        }
    }

    private lateinit var quizManager: QuizManager
    private var spamCount: Int = 0

    private fun startTesting() {
        setupQuizManager()
        initNewQuestion()
    }

    private fun setupQuizManager() {
        quizManager = QuizManager(intent.getIntArrayExtra("tables"), intent.getStringExtra("operation"))
        quizManager.setup()

        quizManager.countdownSeconds.observe(this, Observer { countdown ->
            if (countdown < 0) {
                markAsFailure()
            } else {
                updateUIWith(countdown)
            }
        })

        quizManager.coolDown.observe(this, Observer { initNewQuestion() })

        quizManager.quizScore.observe(this, Observer { score ->
            scoreLabel.text = score.toString()
        })

        quizManager.answerText.observe(this, Observer { answer ->
            testAnswer.text = answer
            testQuestion.text = quizManager.questionText() + answer
            if (isAnswerCorrect(answer, quizManager.questionResult())) {
                markAsSuccess()
            }
        })
    }

    private fun initNewQuestion() {
        if (quizManager.canMoveToNextQuestion()) {
            starsImage.setImageResource(R.drawable.stars3)
            wizardImage.setImageResource(R.drawable.wizard_eyes_front)

            updateTestQuestion()
        } else {
            showScoreDialog()
        }
    }

    private fun updateTestQuestion() {
        testQuestion.text = quizManager.questionText()
    }

    private fun isAnswerCorrect(answer: String, correctAnswer: Int): Boolean {
        if (answer.isEmpty() && !quizManager.isTooLate()) {
            spamCount += 1
            if (spamCount > 2) {
                markAsFailure()
            }
        }

        return ((answer.isNotEmpty()) && (answer.toInt() == correctAnswer))
    }

    private fun updateUIWith(seconds: Int) {
        quizManager.starsAt(seconds).let { starsImage.setImageResource(it) }

        if (seconds > 9) {
            wizardImage.setImageResource(R.drawable.wizard_eyes_front)
        } else {
            if (seconds.rem(2) == 1) {
                wizardImage.setImageResource(R.drawable.wizard_eyes_left)
            } else {
                wizardImage.setImageResource(R.drawable.wizard_eyes_right)
            }
        }
    }

    private fun markAsSuccess() {
        resetQuestion()
        Sound.good()
        wizardImage.setImageResource(R.drawable.wizard_angry)
    }

    private fun markAsFailure() {
        resetQuestion()
        Sound.bad()
        wizardImage.setImageResource(R.drawable.wizard_happy)
    }

    private fun resetQuestion() {
        testQuestion.text = quizManager.questionAnswer()
        testAnswer.text = ""
        starsImage.setImageDrawable(null)

        quizManager.finishQuestion()
        spamCount = 0
    }

    private fun showExitDialog() {
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.simple_dialog)
        val body = dialog.findViewById(R.id.dialogText) as TextView
        body.text = getString(R.string.exitConfirmation)
        val confirmButton = dialog.findViewById(R.id.confirmButton) as Button
        val denyButton = dialog.findViewById(R.id.denyButton) as TextView
        confirmButton.setOnClickListener {
            Sound.tap()
            quizManager.finishQuiz()
            dialog.dismiss()
            finish()
        }
        denyButton.setOnClickListener {
            Sound.tap()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showScoreDialog() {
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.final_score)

        (dialog.findViewById(R.id.scoreText) as TextView).text = quizManager.score().toString()

        val starCount = quizManager.starsAchieved()
        Sound.finalScore(starCount)
        if (starCount > 0) dialog.starLeft.setImageResource(R.drawable.star_left_full) else dialog.starLeft.setImageResource(R.drawable.star_left_empty)
        if (starCount > 1) dialog.starMiddle.setImageResource(R.drawable.star_middle_full) else dialog.starMiddle.setImageResource(R.drawable.star_middle_empty)
        if (starCount > 2) dialog.starRight.setImageResource(R.drawable.star_right_full) else dialog.starRight.setImageResource(R.drawable.star_right_empty)

        val returnButton = dialog.findViewById(R.id.returnButton) as Button
        returnButton.setOnClickListener {
            Sound.tap()
            quizManager.finishQuiz()
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
