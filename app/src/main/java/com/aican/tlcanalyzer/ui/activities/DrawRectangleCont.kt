package com.aican.tlcanalyzer.ui.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.databinding.ActivityDrawRectangleContBinding
import com.aican.tlcanalyzer.domain.model.spots.ManualContourResult
import com.aican.tlcanalyzer.domain.model.spots.manul_spots.ManualContour
import com.aican.tlcanalyzer.utils.RegionOfInterest
import com.aican.tlcanalyzer.utils.SharedStates
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt


class DrawRectangleCont : ComponentActivity() {
    lateinit var binding: ActivityDrawRectangleContBinding
    private var rgba: Lazy<Mat> = lazy { Mat() }
    private lateinit var originalImage: Mat

    private var horizontalLinesYCoordinates = ArrayList<Int>()
    private var userTaps = ArrayList<Int>()
    private var rectangles = mutableListOf<Pair<Int, Int>>()
    private var rectangleList = ArrayList<Rect>()

    private val manualContourArrayList = ArrayList<ManualContour>()
    private val onlyManualContourArrayList = ArrayList<ManualContour>()
    private var lastSeekBarProgress = 0
    private var imageHeight = 0

    var contourImagePath: String? = null
    var contourBitmap: Bitmap? = null
    var imageBitmap: Bitmap? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawRectangleContBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
//        supportActionBar?.hide()

        binding.back.setOnClickListener {
            finish()
        }

        contourImagePath = intent.getStringExtra("contourImagePath")

        if (contourImagePath == null) {
            Toast.makeText(this@DrawRectangleCont, "No contour image path", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        val contourImageFile: File = File(contourImagePath!!)

        if (contourImageFile.exists()) {
            contourBitmap = BitmapFactory.decodeFile(contourImageFile.path)
            imageBitmap = contourBitmap
        } else {
            Toast.makeText(this@DrawRectangleCont, "Contour image not found", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
        if (contourBitmap == null) {
            Log.e("ItsNullvalue", "Null Uri" + "")
            Toast.makeText(
                this@DrawRectangleCont,
                "Bitmap Null, Spot the contour once",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        OpenCVLoader.initLocal()



        for (manualCont in manualContourArrayList) {
            manualContourArrayList.add(
                ManualContour(
                    manualCont.shape,
                    manualCont.roi,
                    manualCont.indexName,
                    manualCont.mainContIndex,
                    manualCont.rfIndex
                )
            )
        }



        binding.imageView.setImageBitmap(contourBitmap)

        binding.imageView.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }

        binding.clearAll.setOnClickListener {

            rectangleList = ArrayList()
            userTaps = ArrayList()
            horizontalLinesYCoordinates = ArrayList()
            rectangles = ArrayList()
            binding.imageView.setImageBitmap(imageBitmap)

            for (i in onlyManualContourArrayList.indices) {
                manualContourArrayList.removeAt(manualContourArrayList.size - 1)
            }

            onlyManualContourArrayList.clear()

        }

        binding.saveRect.setOnClickListener {

            if (rectangleList != null && rectangleList.size > 0) {
                println("Rectangles - Save button clicked: ${rectangleList}")
                println("manualContourArrayList - Save button clicked: ${manualContourArrayList}")

                val manualContourResultList = ArrayList<ManualContourResult>()
                val df = DecimalFormat("0.00E0")

                rectangleList.forEachIndexed { index, rect ->

                    val area = RegionOfInterest.calculateRectangleArea(rect.width(), rect.height())

                    val imageHeight: Int = contourBitmap!!.getHeight()
                    val distanceFromTop: Double = (rect.top + rect.bottom) * 0.5

                    val maxDistance = imageHeight.toDouble()
                    val rfValue4: Double = 1.0 - (distanceFromTop / maxDistance)

                    val cv: Double = 1 / rfValue4

                    val rfValueTop: Double = rfValue4 + (rect.height() / 2) / imageHeight.toDouble()
                    val rfValueBottom: Double =
                        rfValue4 - (rect.height() / 2) / imageHeight.toDouble()
                    //                double area = pixelArea;

                    val x: Int = rect.left
                    val y: Int = rect.top
                    val w: Int = rect.width()
                    val h: Int = rect.height()

                    val solventFrontDistance: Double = w * 0.5 * (1.0 - 100.0 / 255.0)

                    val contourDistance = sqrt((x * x + y * y).toDouble())

                    val number: Double = area * abs(solventFrontDistance - contourDistance)
                    println(df.format(number))

                    val volume: Double = df.format(number).toDouble()


                    manualContourResultList.add(
                        ManualContourResult(
                            ContourType.RECTANGULAR,
                            ContourData(
                                contourId = "",
                                imageId = "",
                                name = "",
                                area = area.toString(),
                                volume = volume.toString(),
                                rf = rfValue4.toString(),
                                rfTop = rfValueTop.toString(),
                                rfBottom = rfValueBottom.toString(),
                                cv = cv.toString(),
                                chemicalName = "",
                                type = ContourType.RECTANGULAR

                            ),
                            rect,
                        )
                    )

                }

                updateManualRectContourListState(manualContourResultList.toList())

                finish()
            } else {
                Toast.makeText(this@DrawRectangleCont, "No spots", Toast.LENGTH_SHORT).show()
            }

        }
        binding.imageView.scaleX *= 2.1f
        binding.imageView.scaleY *= 2.1f
        binding.zoomIn.setOnClickListener {
            binding.imageView.scaleX *= 1.1f
            binding.imageView.scaleY *= 1.1f

        }

        binding.zoomOut.setOnClickListener {

            binding.imageView.scaleX *= 0.9f
            binding.imageView.scaleY *= 0.9f

        }

        binding.moveImageView.max =
            (imageBitmap?.width!! - binding.imageView.width).coerceAtLeast(0)

        binding.moveImageView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate the translation factor based on progress and current zoom level
                val scaleFactor = binding.imageView.scaleX
                val multiplier = (5 * scaleFactor).toInt() // Adjust this value as needed
                val translationFactor = (progress - seekBar!!.max / 2) * multiplier.toFloat()

                // Apply translation to the ImageView
                binding.imageView.translationX = translationFactor
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.undoButton.setOnClickListener {
            undoLastRectangle()
        }

        binding.lineSeekBar.visibility = View.GONE

        binding.lineSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateLastDrawnRectangleLines(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed in this case
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed in this case
            }
        })


        binding.slideThisLine.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateLastDrawnRectangleLines2(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


    }

    private fun updateManualRectContourListState(rectArrayList: List<ManualContourResult>) {
        SharedStates.updateManualRectContourList(rectArrayList)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun handleTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x.toInt()
            val touchY = event.y.toInt()


            imageHeight = binding.imageView.height

            // Calculate the percentage of the touched y-coordinate relative to the image height
            val topPer = (touchY.toDouble() / imageHeight) * 100

            binding.slideThisLine.progress = topPer.roundToInt()

            val originalImageX =
                (touchX.toFloat() / binding.imageView.getWidth() * originalImage.cols()).toInt()
            val originalImageY =
                (touchY.toFloat() / binding.imageView.getHeight() * originalImage.rows()).toInt()

            Log.e(
                "TopPer",
                "$topPer, touchY: $touchY, touchX: $touchX, ImageHeight: $imageHeight" +
                        " OriginalImageY: $originalImageY"
            )

            horizontalLinesYCoordinates.add(originalImageY)
            userTaps.add(originalImageY)
            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2]
                val y2 = userTaps[userTaps.size - 1]
                if (userTaps.size % 2 == 0) {

                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
                        rectangleList.add(rect)

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

                        rectangleList.add(rect)



                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }


                }
            }

        }
    }

    private fun updateLastDrawnRectangleLines2(progress: Int) {
        if (userTaps.isNotEmpty() && horizontalLinesYCoordinates.isNotEmpty()) {
            val lastUserTapIndex = userTaps.size - 1
            val lastUserTapIndexH = horizontalLinesYCoordinates.size - 1

            val lastVal = userTaps[lastUserTapIndex]
            val lastValH = horizontalLinesYCoordinates[lastUserTapIndexH]

            userTaps.removeLast()
            horizontalLinesYCoordinates.removeLast()

            imageHeight = binding.imageView.height

//            val topPer = (touchY.toDouble() / imageHeight) * 100


            val touchY = (progress.toDouble() / 100.toDouble()) * imageHeight

            val originalImageY =
                (touchY.toFloat() / binding.imageView.height * originalImage.rows()).toInt()

            Log.e(
                "Oriinal", "touchY: $touchY, originalY: $originalImageY, progress: $progress" +
                        " imageHeight: $imageHeight" +
                        ""
            )

            horizontalLinesYCoordinates.add(originalImageY)
            userTaps.add(originalImageY)


            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2]
                val y2 = userTaps[userTaps.size - 1]
                if (userTaps.size % 2 == 0) {

                    rectangles.removeLast()
                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
                        rectangleList.removeLast()
                        rectangleList.add(rect)

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                        if (manualContourArrayList.isNotEmpty()) {
                            manualContourArrayList.removeLast()
                        }
                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

                        rectangleList.removeLast()
                        rectangleList.add(rect)



                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }


                }
            }


        }
    }

    private fun updateLastDrawnRectangleLines(progress: Int) {
        if (rectangleList.isNotEmpty()) {
            val lastRectangle = rectangleList.last()
            val deltaY = progress - lastSeekBarProgress

            // Update the Y coordinates of the last drawn rectangle's lines
            val updatedY1 = lastRectangle.top + deltaY
            val updatedY2 = lastRectangle.bottom + deltaY

            // Update the Y coordinates in rectangleList
            lastRectangle.top = updatedY1
            lastRectangle.bottom = updatedY2

            // Clear the image
            val imageWithLines = originalImage.clone()

            // Redraw all rectangles with the updated positions
            drawAllRectangles(imageWithLines)

            // Update the imageView with the modified image
            val bitmapWithLines = Bitmap.createBitmap(
                imageWithLines.cols(),
                imageWithLines.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(imageWithLines, bitmapWithLines)
            binding.imageView.setImageBitmap(bitmapWithLines)

            // Update lastSeekBarProgress
            lastSeekBarProgress = progress
        }
    }

    private fun undoLastRectangle() {
        if (rectangleList.isNotEmpty()) {

            if (rectangleList.isNotEmpty())
                rectangleList.removeLast()

            if (rectangleList.isNotEmpty())
                rectangles.removeLast()

            if (rectangleList.isNotEmpty())
                horizontalLinesYCoordinates.removeLast()

            if (rectangleList.isNotEmpty())
                horizontalLinesYCoordinates.removeLast()

            if (rectangleList.isNotEmpty())
                userTaps.removeLast()

            if (rectangleList.isNotEmpty())
                userTaps.removeLast()


            drawHorizontalLinesOnImage()

            if (userTaps.size >= 2) {
                val y1 = userTaps[userTaps.size - 2]
                val y2 = userTaps[userTaps.size - 1]
                if (userTaps.size % 2 == 0) {

//                    rectangles.add(y1 to y2)

                    if (y1 < y2) {
                        val rect = Rect(0, y1, imageWithLines.cols(), y2)
//                        rectangleList.add(rect)

                        manualContourArrayList.removeLast()

                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
//                        manualContourArrayList.add(ManualContour(0, rect, "i", "i", 0))


                    } else {
                        val rect = Rect(0, y2, imageWithLines.cols(), y1)

//                        rectangleList.add(rect)


                        drawRectangle(y1, y2, rect.left, rect.top, imageWithLines.clone())
                    }
                }
            }

        } else {
            rectangleList = ArrayList()
            userTaps = ArrayList()
            horizontalLinesYCoordinates = ArrayList()
            rectangles = ArrayList()
            binding.imageView.setImageBitmap(imageBitmap)

            for (i in onlyManualContourArrayList.indices) {
                manualContourArrayList.removeLast()
            }

            onlyManualContourArrayList.clear()
            Toast.makeText(this@DrawRectangleCont, "No rectangles to undo", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Function to redraw the image with rectangles after modification


    fun generateUniqueIndexName(manualContourArrayList: ArrayList<ManualContour>): String {
        var index = 1
        var indexName = "m$index"
        while (this.isIndexNameInUse(indexName, manualContourArrayList)) {
            index++
            indexName = "m$index"
        }
        return indexName
    }

    fun isIndexNameInUse(
        indexName: String,
        manualContourArrayList: ArrayList<ManualContour>
    ): Boolean {
        return manualContourArrayList.any { it.indexName == indexName }
    }


    private fun drawRectangle(y1: Int, y2: Int, p1: Int, p2: Int, imageWithLines: Mat) {

        val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
        val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
        val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

        Imgproc.rectangle(
            mask,
            Point(0.0, y1.toDouble()),
            Point(imageWithLines!!.cols().toDouble(), y2.toDouble()),
            Scalar(255.0, 0.0, 0.0, 50.0),
            -1
        )

        Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

        val fontScale = 1.0
        val fontColor = Scalar(255.0, 255.0, 255.0)
        val fontThickness = 2
        val point = Point(p1.toDouble(), p2.toDouble()) // Adjust these coordinates as needed

//        val text = "Rectangle: ($p1, $p2)"
//        val text = generateUniqueIndexName(manualContourArrayList)
        val text = "m" + rectangles.size.toString()
//        Toast.makeText(this@DrawRectangleCont, "" + text, Toast.LENGTH_SHORT).show()
//        Toast.makeText(this@DrawRectangleCont, "" + text, Toast.LENGTH_SHORT).show()

        Imgproc.putText(
            imageWithLines,
            text,
            point,
            Imgproc.FONT_HERSHEY_SIMPLEX,
            fontScale,
            fontColor,
            fontThickness
        )

        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )

        Utils.matToBitmap(imageWithLines, bitmapWithLines)

        binding.imageView.setImageBitmap(bitmapWithLines)

        // Add text to the top left corner

    }


    private fun drawAllRectangles(imageWithLines: Mat) {

//        val imageWithLines = originalImage.clone()

        var bitmapWithLines: Bitmap? = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )

        var i = 1


        for (rectangle in rectangles) {

            val color = Scalar(0.0, 0.0, 255.0)  // BGR color for the line (red in this case)
            val transparentRed = Scalar(255.0, 0.0, 0.0, 50.0)  // More transparent red color
            val mask = Mat.zeros(imageWithLines.size(), imageWithLines.type())

            Imgproc.rectangle(
                mask,
                Point(0.0, rectangle.first.toDouble()),
                Point(imageWithLines!!.cols().toDouble(), rectangle.second.toDouble()),
                Scalar(255.0, 0.0, 0.0, 50.0),
                -1
            )

            Core.addWeighted(imageWithLines, 1.0, mask, 0.5, 0.0, imageWithLines)

            val y1 = rectangle.first
            val y2 = rectangle.second

            if (y1 < y2) {
                val fontScale = 1.0
                val fontColor = Scalar(255.0, 255.0, 255.0)
                val fontThickness = 2
                val point = Point(
                    0.toDouble(),
                    y1.toDouble()
                ) // Adjust these coordinates as needed
//                val text = generateUniqueIndexName(manualContourArrayList)

                val text = "m$i"
                Imgproc.putText(
                    imageWithLines,
                    text,
                    point,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    fontScale,
                    fontColor,
                    fontThickness
                )

            } else {
                val fontScale = 1.0
                val fontColor = Scalar(255.0, 255.0, 255.0)
                val fontThickness = 2
                val point = Point(
                    0.toDouble(),
                    y2.toDouble()
                ) // Adjust these coordinates as needed

                val text = "m$i"

//                val text = "Rectangle: (0, ${y2})"
                Imgproc.putText(
                    imageWithLines,
                    text,
                    point,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    fontScale,
                    fontColor,
                    fontThickness
                )
            }



            bitmapWithLines = Bitmap.createBitmap(
                imageWithLines.cols(),
                imageWithLines.rows(),
                Bitmap.Config.ARGB_8888
            )
            i++

//            drawRectangle(rectangle.first, rectangle.second)
        }
        Utils.matToBitmap(imageWithLines, bitmapWithLines)

        binding.imageView.setImageBitmap(bitmapWithLines)

    }

    lateinit var imageWithLines: Mat

    private fun drawHorizontalLinesOnImage() {
        imageWithLines = originalImage.clone()

//        horizontalLinesYCoordinates.sort()

        val lineColor = Scalar(0.0, 0.0, 255.0) // Red color
        val lineThickness = 2

        for (y in horizontalLinesYCoordinates) {
            drawHorizontalLine(imageWithLines, y, lineColor, lineThickness)
        }

        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(imageWithLines, bitmapWithLines)
        binding.imageView.setImageBitmap(bitmapWithLines)

        drawAllRectangles(imageWithLines)
    }

    private fun drawHorizontalLine(image: Mat, y: Int, color: Scalar, thickness: Int) {
        val start = Point(0.0, y.toDouble())
        val end = Point((image.cols() - 1).toDouble(), y.toDouble())
        Imgproc.line(image, start, end, color, thickness)
    }

    override fun onResume() {
        super.onResume()
        originalImage = Mat()
        Utils.bitmapToMat(imageBitmap, originalImage)
        drawAllRectangles(originalImage) // Redraw rectangles

    }


}