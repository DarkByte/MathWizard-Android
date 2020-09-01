package com.example.mathwizard

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import androidx.appcompat.app.AppCompatActivity

class Sound {
    companion object {
        private lateinit var context: Context

        fun from(context: Context) {
            this.context = context
        }

        fun intro() {
            playSound("intro")
        }

        fun tap() {
            playSound("tap")
        }

        fun error() {
            playSound("error")
        }

        fun switch() {
            playSound("operation_switch")
        }

        fun good() {
            playSound("answer_good")
        }

        fun bad() {
            playSound("answer_bad")
        }

        fun finalScore(stars: Int) {
            when (stars) {
                1 -> playSound("stars1_final")
                2 -> playSound("stars2_final")
                3 -> playSound("stars3_final")
                else -> playSound("stars0_final")
            }
        }

        private fun playSound(soundName: String) {
            val resourceID = getResourceID(soundName)
            if (resourceID != 0) {
                playSoundFile(resourceID)
                return
            }
        }

        private fun getResourceID(resourceName: String): Int {
            return this.context.resources.getIdentifier(resourceName, "raw", this.context.packageName)
        }

        private fun playSoundFile(fileName: Int) {
            val mediaPlayer = MediaPlayer.create(this.context, fileName)
            mediaPlayer.setOnCompletionListener(OnCompletionListener { mp ->
                mp.stop()
                mp.release()
            })
            mediaPlayer.start()
        }
    }
}