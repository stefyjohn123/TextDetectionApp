package com.projects.textdetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.schedule
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    val BASE_URL = "https://tga-search.clients.funnelback.com"
    private lateinit var mConsole: TextView
    private lateinit var mButton: Button

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val imageAnalyzer by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        TextReaderAnalyzer(::onTextFound)
                    )
                }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
    }

    var textIndex = 0
    private val actualList:MutableList<String> = mutableListOf()
    val idStart = "AUST"

    private fun onTextFound(foundText: String)  {
        Log.d("TAG", foundText)
        detectText(foundText)
//        TGASearchIdAPI()

        if (textIndex == 10) {
            mConsole.text = ""
            textIndex = 0
        }
        textIndex ++
    }

    var flag = true
    var toastShown = true

    @SuppressLint("SetTextI18n")
    private fun detectText(foundText:String) {
            if (foundText == idStart && actualList.isEmpty()) {
                actualList.add(foundText)
                Log.d("TAG", "We Aust : $foundText")

                flag = true
            }
        else if (foundText == "R" && actualList.size == 1) {
                actualList.add(foundText)
                Log.d("TAG", "got R : $foundText")

                flag = true
            } else if (isNumber(foundText) && foundText.length == 5 && actualList.size == 2) {
                actualList.add(foundText)
                Log.d("TAG", "got num : $foundText")

                flag = true
            } else if  (actualList.size == 3 && flag) {
                Log.d("TAG", "We got new text: $actualList")
                val detectedText = (actualList.joinToString (" "))
                val detectedID = actualList.last()
                Log.d("Tag", "detected ID will be $detectedID")
                mConsole.text = "ID Detected: $detectedText"
//                Toast.makeText(this," ID Detected: $detectedText",Toast.LENGTH_SHORT).show()
                flag = true
                toastShown = true
//                TGASearchIdAPI()
            }else{
                Log.d("TAG", "got : $foundText")
                actualList.clear()
                flag = false
                toastShown = false
                handleNoId(foundText)
            }
    }



    private fun TGASearchIdAPI() {

        Log.d("tag","enter into api")
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build().create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getTGAData("AUST+R+46250","tga-artg")

        retrofitData.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val medicineList = response.body()
                Log.d("tag","response-- ${response.errorBody().toString()}")
                Log.d("tag","response body will be ${medicineList.toString()}")
                Log.d("tag","request url ${call.request().url().toString()}")
                try {
                    val html = response.body()!!.string()
                    val document: Document = Jsoup.parse(html)
                    val elements: Elements = document.select("title")
                    for (element in elements) {
                        if (element.attr("name_of_attribute_you want to check")
                                .equals("value_of_the_attribute")
                        ) {
                            //Save As you want to
                            Log.d(TAG, " myHTMLResponseCallback : " + title + element.attr("value"))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("tag","response failure will be $t")
                t.printStackTrace()
            }
        })
    }



    private fun handleNoId(foundText: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            if(!flag && actualList.size!=3 && !toastShown ){
                Toast.makeText(this,"No ID Detected",Toast.LENGTH_SHORT).show()
                Log.d("TAG", "we : $foundText")

                mConsole.text = "No ID Detected in main"
                                                             }
        }, 30000)
    }


//    @SuppressLint("SetTextI18n")
//    private fun detectText(foundText:String) {
//        if (foundText == idStart || foundText == "R" || isNumber(foundText) && (foundText.length) == 5) {
//            if (foundText == idStart && actualList.isEmpty()) {
//                actualList.add(foundText)
//            } else if (foundText == "R" && actualList.size == 1) {
//                actualList.add(foundText)
//            } else if (isNumber(foundText) && foundText.length == 5 && actualList.size == 2) {
//                actualList.add(foundText)
//            } else {
//                actualList.clear()
//            }
//            if (actualList.size == 3) {
//                Log.d("TAG", "We got new text: $actualList")
//                val detectedText = (actualList.joinToString (" "))
//
//                Log.d("Tag", "Third Element----$foundText")
//                Log.d("Tag", "First+Second+Third actualList----$actualList")
//
//                mConsole.text = "ID Detected: $detectedText"
//                Toast.makeText(this," ID Detected: $detectedText",Toast.LENGTH_SHORT).show()
//            }
//        }
//        else {
//            actualList.clear()
//            Handler(Looper.getMainLooper()).postDelayed({
//                mConsole.text = "No ID Detected in main"
//            }, 30000)
//        }
//    }

    var numeric = true
    private fun isNumber(foundText: String):Boolean {
        numeric = foundText.matches("-?\\d+(\\.\\d+)?".toRegex())
        if (numeric) {
            println("$foundText is a number")
            return numeric
        }
        else {
            println("$foundText is not a number")
            numeric = false
            return numeric
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mConsole = findViewById(R.id.console)
        mButton = findViewById(R.id.button)

        mButton.setOnClickListener {
            val intent = Intent(this, TGADetailsActivity::class.java).apply {
//                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
        }


        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        if (allPermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startCamera()
            }
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startCamera()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            Runnable {
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(findViewById<PreviewView>(R.id.cameraPreviewView).surfaceProvider) }
                cameraProviderFuture.get().bind(preview, imageAnalyzer)
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ProcessCameraProvider.bind(
        preview: Preview,
        imageAnalyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@MainActivity,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalyzer
        )
    } catch (ise: IllegalStateException) {
        // Thrown if binding is not done from the main thread
        Log.e(TAG, "Binding failed", ise)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }
    private companion object {
        val TAG = MainActivity::class.java.simpleName
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

private fun <E> List<E>.clear() {
}

