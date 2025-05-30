package com.aican.tlcanalyzer.ui.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.ManualContourDetails
import com.aican.tlcanalyzer.databinding.ActivityEditRectangleContourBinding
import com.aican.tlcanalyzer.utils.SharedData
import com.aican.tlcanalyzer.utils.SharedStates
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

@AndroidEntryPoint
class EditRectangleContour : ComponentActivity() {

    lateinit var binding: ActivityEditRectangleContourBinding
    lateinit var rgba: Mat
    var imageBitmap: Bitmap? = null
    lateinit var rectOfRectangle: Rect

    lateinit var spotName: String
    lateinit var contourId: String
    lateinit var manualContourId: String
    private var leftSeekBarProgress = 0
    private var rightSeekBarProgress = 0

    private var initialLeftSeekBarProgress = 50
    private var initialRightSeekBarProgress = 50

    private var savedRect: Rect? = null
    lateinit var dir: File
    var contourJsonFileName = "null"

    lateinit var plotTableName: String
    private var imageHeight = 0

    private var originalImagePath: String? = null

    var originalBitmap: Bitmap? = null
    private val imageAnalysisViewModel: ImageAnalysisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRectangleContourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        OpenCVLoader.initLocal()

        if (SharedData.editRectangleContourRect != null) {
            rectOfRectangle = SharedData.editRectangleContourRect!!
        } else {
            Toast.makeText(this@EditRectangleContour, "RECT is null", Toast.LENGTH_SHORT).show()
        }

        rgba = Mat()

        originalImagePath = intent.getStringExtra("mainImagePath")
        spotName = intent.getStringExtra("spotName").toString()
        contourId = intent.getStringExtra("contourId").toString()
        manualContourId = intent.getStringExtra("manualContourId").toString()

        if (originalImagePath == null) {
            Toast.makeText(this@EditRectangleContour, "No image path available", Toast.LENGTH_SHORT)
                .show()
            finish()
        }


        val contourImageFile: File = File(originalImagePath!!)

        if (contourImageFile.exists()) {
            originalBitmap = BitmapFactory.decodeFile(contourImageFile.path)
            imageBitmap = originalBitmap
        } else {
            Toast.makeText(this@EditRectangleContour, "Contour image not found", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        binding.imageView.setImageBitmap(originalBitmap)

        var y1 = rectOfRectangle.top
        var y2 = rectOfRectangle.bottom
        var p1 = rectOfRectangle.left
        var p2 = rectOfRectangle.right

        var imageWithLines = Mat()
        Utils.bitmapToMat(originalBitmap, imageWithLines)

        imageHeight = originalBitmap!!.height
        val rectHeight = rectOfRectangle.height()
        val topPercentage = (rectOfRectangle.top.toDouble() / imageHeight) * 100
        val bottomPercentage = (rectOfRectangle.bottom.toDouble() / imageHeight) * 100
        initialLeftSeekBarProgress = (100 - topPercentage).toInt()
        initialRightSeekBarProgress = (100 - (bottomPercentage)).toInt()

        // Set initial progress of SeekBars
        binding.leftSeekbar.progress = initialLeftSeekBarProgress
        binding.rightSeekbar.progress = initialRightSeekBarProgress

        drawRectangle(y1, y2, p1, p2, imageWithLines)


        binding.leftSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                leftSeekBarProgress = progress

                y1 = rectOfRectangle.top + (100 - progress) // Reverse the calculation for y1
                y2 = rectOfRectangle.bottom
                p1 = rectOfRectangle.left
                p2 = rectOfRectangle.right

                imageWithLines = Mat()

                Utils.bitmapToMat(originalBitmap, imageWithLines)

                val newY1 = (100 - progress) * imageHeight / 100
                val newY2 = rectOfRectangle.bottom
                updateLines(
                    newY1, newY2, imageWithLines
                )

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.rightSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rightSeekBarProgress = progress

                y1 = rectOfRectangle.top
                y2 = rectOfRectangle.bottom - progress // Adjust the calculation for y2
                p1 = rectOfRectangle.left
                p2 = rectOfRectangle.right

                imageWithLines = Mat()
                Utils.bitmapToMat(originalBitmap, imageWithLines)

                val newY1 = rectOfRectangle.top
                val newY2 = (100 - progress) * imageHeight / 100
//                val newY2 = rectOfRectangle.bottom + progress * imageHeight / 100
                updateLines(newY1, newY2, imageWithLines)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.saveRect.setOnClickListener {
            saveRectangle()
        }
    }


    private fun updateLines(newY1: Int, newY2: Int, imageWithLines: Mat) {
        // Update the lines based on new positions
        val p1 = rectOfRectangle.left
        val p2 = rectOfRectangle.right

        drawRectangle(newY1, newY2, p1, p2, imageWithLines)

        // Update saved rectangle coordinates
        savedRect = Rect(p1, newY1, p2, newY2)
    }

    private fun saveRectangle() {
        savedRect?.let { rect ->
            imageAnalysisViewModel.viewModelScope.launch {
                imageAnalysisViewModel.updateManualContourDetails(
                    ManualContourDetails(
                        manualContourId = manualContourId,
                        contourId = contourId, //6.75
                        roiTop = rect.top.toFloat(),
                        roiBottom = rect.bottom.toFloat(),
                        roiLeft = rect.left.toFloat(),
                        roiRight = rect.right.toFloat()
                    )
                )

                SharedStates.updateManualContourEditState(true)
                finish()

            }
        }
    }

    private fun drawRectangle(y1: Int, y2: Int, p1: Int, p2: Int, imageWithLines: Mat) {

        val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
        val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
        val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

        // Draw rectangle
        Imgproc.rectangle(
            mask,
            Point(0.0, y1.toDouble()),
            Point(imageWithLines.cols().toDouble(), y2.toDouble()),
            Scalar(255.0, 0.0, 0.0, 50.0),
            -1
        )

        // Draw horizontal lines above and below the rectangle
        val lineColor = Scalar(0.0, 0.0, 255.0)  // Green color for the line
        val lineThickness = 2

        // Draw line above the rectangle
        drawHorizontalLine(imageWithLines, y1, lineColor, lineThickness)
        // Draw line below the rectangle
        drawHorizontalLine(imageWithLines, y2, lineColor, lineThickness)

        // Combine the rectangle and lines
        Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

        // Draw text
        val fontScale = 1.0
        val fontColor = Scalar(255.0, 255.0, 255.0)
        val fontThickness = 2
        val point = Point(p1.toDouble(), p2.toDouble())

        val text = spotName

        Imgproc.putText(
            imageWithLines,
            spotName,
            point,
            Imgproc.FONT_HERSHEY_SIMPLEX,
            fontScale,
            fontColor,
            fontThickness
        )

        // Convert image to bitmap and set to imageView
        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(imageWithLines, bitmapWithLines)
        binding.imageView.setImageBitmap(bitmapWithLines)
    }

    private fun drawHorizontalLine(image: Mat, y: Int, color: Scalar, thickness: Int) {
        val start = Point(0.0, y.toDouble())
        val end = Point((image.cols() - 1).toDouble(), y.toDouble())
        Imgproc.line(image, start, end, color, thickness)
    }

}