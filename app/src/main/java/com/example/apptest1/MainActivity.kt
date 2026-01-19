package com.example.apptest1

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
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
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import com.example.apptest1.PhishingApi
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var cameraImage: ImageView
    private lateinit var captureImgBtn: Button
    private lateinit var captureImgBtnLib: Button

    private lateinit var checkPhisingBtn: Button
    private lateinit var resultText: TextView
    private lateinit var copyTextBtn: Button

    private var currenPhotoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private lateinit var phishingStatus: TextView


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

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) captureImage()
                else Toast.makeText(this, "Camera denied", Toast.LENGTH_SHORT).show()
            }

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
                    try {
                        val inputStream = contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        cameraImage.setImageBitmap(bitmap)
                        recognizeText(bitmap)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        requestReadPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) pickImageLauncher.launch("image/*")
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }

        captureImgBtn.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureImgBtnLib.setOnClickListener {
            val sdkInt = android.os.Build.VERSION.SDK_INT
            val permission = if (sdkInt >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

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
            val text = resultText.text.toString().trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Không có text để check", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PhishingApi.checkPhishing(text) { success, response ->
                runOnUiThread {
                    // In log để debug kết quả response từ API
                    Log.d("MainActivity", "Phishing check result: $response")

                    if (success) {
                        // Chuyển sang PhishingResultActivity và truyền kết quả
                        val intent = Intent(this, PhishingResultActivity::class.java)
                        intent.putExtra("PHISHING_RESULT", response)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Lỗi: $response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.tab_scan

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tab_scan -> true
                R.id.tab_history -> { startActivity(Intent(this, HistoryActivity::class.java)); true }
                R.id.tab_howto -> { startActivity(Intent(this, HowToActivity::class.java)); true }
                else -> false
            }
        }

    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JEP_${timeStamp}_", ".jpg", storageDir).apply {
            currenPhotoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { ocrText ->
                resultText.text = ocrText.text
                resultText.movementMethod = ScrollingMovementMethod()
                copyTextBtn.visibility = Button.VISIBLE
                copyTextBtn.setOnClickListener {
                    val clipboard = ContextCompat.getSystemService(
                        this,
                        android.content.ClipboardManager::class.java
                    )
                    val clip = android.content.ClipData.newPlainText("recognized text", ocrText.text)
                    clipboard?.setPrimaryClip(clip)
                    Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show()
                }
                checkPhisingBtn.performClick()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
