package com.example.apptest1

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale
import  com.example.apptest1.model.HistoryItem

class MainActivity : AppCompatActivity() {

    private lateinit var cameraImage: ImageView
    private lateinit var captureImgBtn: Button
    private lateinit var captureImgBtnLib: Button
    private lateinit var checkPhisingBtn: Button
    private lateinit var resultText: TextView
    private lateinit var copyTextBtn: Button

    private var currenPhotoPath: String? = null
    private var selectedImageUri: Uri? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        cameraImage = findViewById(R.id.cameraImage)
        captureImgBtn = findViewById(R.id.captureImgBtn)
        captureImgBtnLib = findViewById(R.id.captureImgBtnLib)
        resultText = findViewById(R.id.resultText)
        copyTextBtn = findViewById(R.id.copyTextBtn)
        checkPhisingBtn = findViewById(R.id.checkPhishingBtn)
        val aiView = AiAssistantView(findViewById(R.id.aiAssistantContainer))
        aiView.bind()
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) captureImage()
                else Toast.makeText(this, "Camera denied", Toast.LENGTH_SHORT).show()
            }

        requestReadPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) pickImageLauncher.launch("image/*")
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }

        // ===== Camera =====
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    currenPhotoPath?.let { path ->
                        val bitmap = BitmapFactory.decodeFile(path)
                        cameraImage.setImageBitmap(bitmap)
                        recognizeText(bitmap)
                    }
                }
            }

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    selectedImageUri = it
                    val inputStream = contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    cameraImage.setImageBitmap(bitmap)
                    recognizeText(bitmap)
                }
            }

        captureImgBtn.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureImgBtnLib.setOnClickListener {
            val permission =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                requestReadPermissionLauncher.launch(permission)
            }
        }

        checkPhisingBtn.setOnClickListener {
            checkPhishing(resultText.text.toString().trim())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.tab_scan

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tab_scan -> true
                R.id.tab_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.tab_howto -> {
                    startActivity(Intent(this, HowToActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JEP_${timeStamp}_", ".jpg", storageDir).apply {
            currenPhotoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.let {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            takePictureLauncher.launch(uri)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = result.text
                resultText.text = text
                resultText.movementMethod = ScrollingMovementMethod()
                checkPhishing(text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "OCR failed", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentImagePath(): String? {
        return currenPhotoPath ?: selectedImageUri?.toString()
    }


    private fun checkPhishing(text: String) {
        if (text.isBlank()) return

        PhishingApi.checkPhishing(text) { success, response ->
            runOnUiThread {
                if (success) {

                    HistoryStorage.save(
                        this,
                        HistoryItem(
                            text = text,
                            result = response,
                            imagePath = getCurrentImagePath(),
                            time = System.currentTimeMillis()
                        )
                    )

                    val intent = Intent(this, PhishingResultActivity::class.java)
                    intent.putExtra("PHISHING_RESULT", response)
                    intent.putExtra("OCR_TEXT", text)
                    startActivity(intent)

                } else {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
