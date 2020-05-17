package com.example.mathwizard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.core.view.MotionEventCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    enum class OperationType(val sign: String, val resourceId: Int) {
        ADD("+", R.drawable.sign_plus),
        SUB("-", R.drawable.sign_minus),
        MUL("*", R.drawable.sign_times),
        DIV("/", R.drawable.sign_div);

        fun nextOperation(): OperationType {
            return values()[(this.ordinal + 1) % values().count()];
        }
    }

    private val startTestRunnable: Runnable = Runnable {
        if (!isFinishing) {
            val intent = Intent(applicationContext, TestActivity::class.java)
            intent.putExtra("tables", selectedTables)
            intent.putExtra("operation", currentOperation.sign)
            startActivity(intent)
        }
    }

    var buttons = emptyArray<Button>()
    var currentOperation = OperationType.ADD

    private val selectedButtonCount: Int
        get() {
            return buttons.count { it.isActivated }
        }

    private val selectedTables: IntArray
        get() {
            var tables = ArrayList<Int>()
            buttons.forEachIndexed { index, button ->
                if ((button.isActivated) && (index < 9)) {
                    tables.add(index + 1)
                }
            }

            return tables.toIntArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        buttons = arrayOf(button1, button2, button3, button4, button5, button6, button7, button8, button9)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonStart -> {
                if (selectedButtonCount > 0) {
                    Sound.tap()
                    startTestRunnable.run()
                    for (button: Button in buttons) {
                        button.isActivated = false
                    }
                    buttonStart.isActivated = false
                } else {
                    Sound.error()
                }
            }

            R.id.buttonOperation -> {
                Sound.switch()
                currentOperation = currentOperation.nextOperation()
                buttonOperation.setImageResource(currentOperation.resourceId)
            }

            else -> {
                Sound.tap()
                view!!.isActivated = !view.isActivated
                buttonStart.isActivated = selectedButtonCount > 0
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }
}
