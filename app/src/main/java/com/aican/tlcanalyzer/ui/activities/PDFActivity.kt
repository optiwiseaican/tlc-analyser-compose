package com.aican.tlcanalyzer.ui.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aican.tlcanalyzer.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PDFActivity : AppCompatActivity() {

    lateinit var pdfView: PDFView
    var filePath: String? = null
    var fileLink: String? = null
    var flag: String? = null
    var title: String? = null
    var chNo: String? = null
    lateinit var progressDialog: ProgressDialog
    var file: File? = null
    var time: String? = null
    lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfactivity)

        supportActionBar!!.hide()

        back = findViewById(R.id.back)

        back.setOnClickListener {
            finish()
        }

        pdfView = findViewById<View>(R.id.pdfView) as PDFView
        filePath = intent.getStringExtra("path")
        fileLink = intent.getStringExtra("link")
        flag = intent.getStringExtra("flag")
        title = intent.getStringExtra("title")
        chNo = intent.getStringExtra("chNo")


        val file = File(filePath!!)
        val path = Uri.fromFile(file)
//        setTitle(intent.getStringExtra("fileName"))
        pdfView.fromUri(path)
            .scrollHandle(DefaultScrollHandle(this@PDFActivity)).load()


    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPressed() {
        super.onBackPressed()

    }


}