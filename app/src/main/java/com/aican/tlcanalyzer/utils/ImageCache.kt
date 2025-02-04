package com.aican.tlcanalyzer.utils

import android.graphics.Bitmap

object ImageCache {
    var bitmap: Bitmap? = null
        private set

    fun setBitmap(newBitmap: Bitmap) {
        bitmap?.recycle()
        bitmap = newBitmap
    }

    fun retrieveBitmap(): Bitmap? {
        val tempBitmap = bitmap
//        bitmap = null
        return tempBitmap
    }

    fun clearBitmap(){
        bitmap?.recycle()
        bitmap = null
    }
}
