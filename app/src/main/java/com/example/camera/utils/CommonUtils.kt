package com.example.camera.utils

import android.util.Log
import java.io.File

object CommonUtils {
    const val PIC_URI = "PIC_URI"
    const val BASE_URI = "storage/emulated/0/Android/media/com.example.camera/camera/"
    const val COMPLETE_URI = "file:///storage/emulated/0/Android/media/com.example.camera/camera/"

    fun getPics(path: String): List<File>? = File(path).listFiles()?.takeWhile {
        it.isFile
    }

}



/*
    fun picsCounter(path: String): Int{
        var iterator= 0

        val f = File(path).listFiles()

        for (i in f) {
            if(i.isFile){
                iterator++
            }
        }
        return iterator
    }*/