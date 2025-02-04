package com.aican.tlcanalyzer.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ImageType
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.databinding.ActivitySplitCroppingBinding
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.utils.ImageCache
import com.aican.tlcanalyzer.utils.SplitImageProjectManager
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SplitCropping : AppCompatActivity() {

    private var originalImage: Mat? = null
    private val verticalLinesXCoordinates = ArrayList<Int>()
    var dir: File? = null
    var rgba: Mat? = null
    private var loadedBitmap: Bitmap? = null
    var id: String? = null
    var projectImage: String? = null
    var tableName: String? = null
    var projectName: String? = null

    var sizeOfMainImageList: Int = 0

    lateinit var binding: ActivitySplitCroppingBinding

    var imageBitmap: Bitmap? = null
    private val projectViewModel: ProjectViewModel by viewModels()

    var projectDescription: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplitCroppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        binding.back.setOnClickListener { finish() }

        OpenCVLoader.initLocal()


        //        Uri imageUri = Uri.parse(getIntent().getStringExtra("img_path"));
        id = intent.getStringExtra("id").toString()

        projectName = intent.getStringExtra("projectName").toString()
        projectDescription = intent.getStringExtra("projectDescription").toString()
        projectImage = intent.getStringExtra("projectImage").toString()
        tableName = intent.getStringExtra("tableName").toString()

        imageBitmap = ImageCache.retrieveBitmap()
        loadedBitmap = imageBitmap

        if (imageBitmap != null) {
            binding.imageView.setImageBitmap(imageBitmap)

            originalImage = Mat()
            Utils.bitmapToMat(imageBitmap, originalImage)


        } else {
            Toast.makeText(this@SplitCropping, "Image is null", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.imageView.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val touchX = event.x.toInt()
                val touchY = event.y.toInt()

                // Calculate the touch coordinates relative to the displayed image bounds
                val originalImageX = (touchX.toFloat() / v.width * originalImage!!.cols()).toInt()
                val originalImageY = (touchY.toFloat() / v.height * originalImage!!.rows()).toInt()

                verticalLinesXCoordinates.add(originalImageX)

                drawVerticalLinesOnImage()
                return@OnTouchListener true
            }
            false
        })

        binding.btnFinalSlicing.setOnClickListener {
            if (verticalLinesXCoordinates.isNotEmpty() && imageBitmap != null) {
                lifecycleScope.launch {
                    val projectId = AppUtils.generateRandomId("TLC_IDN", 8)

                    // ✅ Create metadata file
                    val metadataPath = SplitImageProjectManager.createMetadataFile(
                        this@SplitCropping,
                        projectId,
                        projectName!!
                    )

                    if (metadataPath == null) {
                        Toast.makeText(
                            this@SplitCropping,
                            "❌ Failed to create metadata file",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    // ✅ Save the main image
                    val mainImageId = "main_1"
                    val mainImagePaths = SplitImageProjectManager.addMainImageToProject(
                        this@SplitCropping,
                        projectId,
                        mainImageId,
                        imageBitmap!!,
                        imageBitmap!!,
                        imageBitmap!!,
                    )

                    if (mainImagePaths.all { it != null }) {
                        println("✅ Main Image: ${mainImagePaths[0]}")
                        println("✅ Original Image: ${mainImagePaths[1]}")
                        println("✅ Contour Image: ${mainImagePaths[2]}")

                        val imageId = AppUtils.generateRandomId("IMAGE", 8)
                        val timestamp =
                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val projectCount = projectViewModel.getProjectCount()

//                        AppUtils.MAIN_IMAGE_COUNT =

                        projectViewModel.insertProjectDetails(
                            ProjectDetails(
                                projectId = projectId,
                                projectName = projectName!!,
                                projectDescription = projectDescription!!,
                                timeStamp = timestamp,
                                mainImagePath = mainImagePaths[0]!!,
                                projectNumber = projectCount.toString(),
                                imageSplitAvailable = true,
                            )
                        )

                        val mainImageName = "Main Image ${AppUtils.MAIN_IMAGE_COUNT + 1}"

                        projectViewModel.insertImage(
                            Image(
                                imageId = imageId,
                                name = mainImageName,
                                originalImagePath = mainImagePaths[1].toString(),
                                croppedImagePath = mainImagePaths[0].toString(),
                                contourImagePath = mainImagePaths[2].toString(),
                                timeStamp = timestamp,
                                thresholdVal = 0,
                                noOfSpots = 0,
                                description = "Main Image with Cropped and Contour Images",
                                projectId = projectId,
                                imageType = ImageType.MAIN
                            )
                        )

                        // ✅ Slice the image into split images
                        sliceImage(
                            projectId = projectId, mainImageId = mainImageId,
                            projectName = projectName!!, projectDescription = projectDescription!!,
                            timestamp = timestamp, mainImagePath = mainImagePaths[0]!!,
                            mainImageName = mainImageName
                        )

                        runOnUiThread {
                            Toast.makeText(
                                this@SplitCropping,
                                "✅ Slicing Completed Successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        println("❌ Failed to save main images")
                    }
                }
            } else {
                Toast.makeText(this, "❌ No split added or Image is null", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnUndo.setOnClickListener(View.OnClickListener {
            undoLastLine()
        })

    }


    private fun undoLastLine() {
        if (verticalLinesXCoordinates.isNotEmpty()) {
            // Remove the last drawn line coordinate

            verticalLinesXCoordinates.removeAt(verticalLinesXCoordinates.size - 1)
            // Redraw the image without the last drawn line
            drawVerticalLinesOnImage()
        }
    }

    private fun drawVerticalLinesOnImage() {
        val imageWithLines = originalImage!!.clone()

        Collections.sort(verticalLinesXCoordinates)

        val lineColor = Scalar(0.0, 0.0, 255.0) // Red color
        val lineThickness = 2
        for (x in verticalLinesXCoordinates) {
            drawVerticalLine(imageWithLines, x, lineColor, lineThickness)
        }

        val bitmapWithLines = Bitmap.createBitmap(
            imageWithLines.cols(),
            imageWithLines.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(imageWithLines, bitmapWithLines)
        binding.imageView.setImageBitmap(bitmapWithLines)
    }

    private fun drawVerticalLine(image: Mat, x: Int, color: Scalar, thickness: Int) {
        val start = Point(x.toDouble(), 0.0)
        val end = Point(x.toDouble(), (image.rows() - 1).toDouble())
        Imgproc.line(image, start, end, color, thickness)
    }

    suspend fun sliceImage(
        projectName: String,
        projectDescription: String,
        projectId: String,
        mainImageId: String,
        mainImagePath: String,
        timestamp: String,
        mainImageName: String
    ) {


        // ✅ Clear and sort the coordinates before slicing
        verticalLinesXCoordinates.sort()

        val slicedImagesBitmap = mutableListOf<Bitmap>()

        // ✅ Add the starting and ending boundaries if not already added
        if (verticalLinesXCoordinates.first() != 0) verticalLinesXCoordinates.add(0, 0)
        if (verticalLinesXCoordinates.last() != originalImage!!.cols()) verticalLinesXCoordinates.add(
            originalImage!!.cols()
        )

        verticalLinesXCoordinates.sort()  // Ensure sorting after additions

        // ✅ Slice and save each split image
        for (i in 1 until verticalLinesXCoordinates.size) {
            val startX = verticalLinesXCoordinates[i - 1]
            val endX = verticalLinesXCoordinates[i]

            val roiWidth = endX - startX
            val roi = Rect(startX, 0, roiWidth, originalImage!!.rows())
            val slicedMat = Mat(originalImage, roi)

            val slicedBitmap =
                Bitmap.createBitmap(slicedMat.cols(), slicedMat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(slicedMat, slicedBitmap)
            slicedImagesBitmap.add(slicedBitmap)

            val splitImageId = "split_image_$i"

            // ✅ Save split image
            val savedPaths = SplitImageProjectManager.addSplitImageToProject(
                this@SplitCropping,
                projectId,
                mainImageId,
                splitImageId,
                slicedBitmap
            )

            if (savedPaths.all { it != null }) {
                println("✅ Split Image: ${savedPaths[0]}")
                println("✅ Temp Split Image: ${savedPaths[1]}")
                println("✅ Contour Split Image: ${savedPaths[2]}")
                val imageId = AppUtils.generateRandomId("IMAGE", 8)

                val splitImageName = "$mainImageName -> Split Image $i"

                projectViewModel.insertImage(
                    Image(
                        imageId = imageId,
                        name = splitImageName,
                        originalImagePath = savedPaths[1].toString(),
                        croppedImagePath = savedPaths[0].toString(),
                        contourImagePath = savedPaths[2].toString(),
                        timeStamp = timestamp,
                        thresholdVal = 0,
                        noOfSpots = 0,
                        description = "Split Image with Cropped and Contour Images",
                        projectId = projectId,
                        imageType = ImageType.SPLIT
                    )
                )

            } else {
                println("❌ Failed to save split images")
            }
        }
    }


    fun saveImageViewToFile(
        originalBitmapImage: Bitmap,
        fileName: String,
        context: Context
    ): String {
        //        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }


        var outStream: FileOutputStream? = null
        try {
            val sdCard = Environment.getExternalStorageDirectory()
            val dir = File(
                ContextWrapper(context).externalMediaDirs[0],
                context.resources.getString(R.string.app_name) + id
            )
            dir.mkdirs()
            val outFile = File(dir, fileName)
            outStream = FileOutputStream(outFile)
            originalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            Log.d("TAG", "onPictureTaken - wrote to " + outFile.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (outStream != null) {
                try {
                    outStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return fileName
    }

    override fun onResume() {
        super.onResume()


        //        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (verticalLinesXCoordinates != null) {
            verticalLinesXCoordinates.clear()
            originalImage = Mat()
            Utils.bitmapToMat(loadedBitmap, originalImage)
            drawVerticalLinesOnImage()
        }


    }

}