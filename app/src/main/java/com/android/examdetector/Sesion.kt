package org.opencv.samples.recorder

import org.opencv.core.Rect

class Sesion {
    val examAnswers = mutableListOf<String>()
    val examCode = mutableListOf<Int>()
    val dniNie = mutableListOf<String>()
    var dniCoordinates = Rect()
    val columnCoordinates = mutableListOf<Rect>()

    companion object {
        val instance: Sesion by lazy { Sesion() }
    }

    fun addAnswer(answer: String) {
        examAnswers.add(answer)
    }

    fun addExamCode(code: Int) {
        examCode.add(code)
    }

    fun addDniNie(dni: String) {
        dniNie.add(dni)
    }

    fun clearSession() {
        examAnswers.clear()
        examCode.clear()
        dniNie.clear()
        columnCoordinates.clear()
        dniCoordinates.x = 0
        dniCoordinates.y = 0
    }


}