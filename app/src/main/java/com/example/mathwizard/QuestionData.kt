package com.example.mathwizard

data class QuestionData(private val leftTerm: Int, private val rightTerm: Int, private val operation: String) {
    val result: Int
        get() {
            when (operation) {
                "+" -> return leftTerm + rightTerm
                "-" -> return leftTerm - rightTerm
                "*" -> return leftTerm * rightTerm
                "/" -> return leftTerm / rightTerm
            }
            return -1
        }

    fun asQuestionText(): String {
        return "$leftTerm $operation $rightTerm = "
    }

    fun asCompleteAnswer(): String {
        return "$leftTerm $operation $rightTerm = $result"
    }

    private val increaseSet = setOf<String>("+", "*") // comutativitate

    fun isSame(data: QuestionData): Boolean {
        if ((leftTerm == data.leftTerm) && (rightTerm == data.rightTerm)) {
            return true
        }

        if ((operation in increaseSet) && (leftTerm == data.rightTerm) && (rightTerm == data.leftTerm)) {
            return true
        }

        return false
    }
}