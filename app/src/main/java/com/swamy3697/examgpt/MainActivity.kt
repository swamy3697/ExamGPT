package com.swamy3697.examgpt

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swamy3697.examgpt.response.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

lateinit var adapter: Adapter
lateinit var recyclerView: RecyclerView
var isMoreToolsclosed: Boolean = false
var isExpanded: Boolean = false
lateinit var sendPromtBtn: ImageView
private val REQUEST_IMAGE_PICK = 2

lateinit var promt: EditText
lateinit var promts_and_responses_list: MutableList<MessageResponses>
// image camera nundi thisina image in nenu save cheesthunna kani ala cheyyakudna just aa vachina image ni edittext lo pettesthey saripothundhi

class MainActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "kk", Toast.LENGTH_LONG).show()
        isMoreToolsclosed = true

        val dropDownMenu = findViewById<AutoCompleteTextView>(R.id.dropdown_menu)
        val exams = arrayOf("IAS", "BPSC", "NDA", "SSC", "UPPCS")
        val exam_adapter = ArrayAdapter(this, R.layout.custome_layout_for_textviews_dropdown_menu, exams)
        dropDownMenu.setAdapter(exam_adapter)

        dropDownMenu.setOnItemClickListener { parent, view, position, id ->
            Log.d("Dropdown", "Item clicked: $position")  // Log message for debugging
            val selectedExam = exams[position]
            Toast.makeText(this, "You selected: $selectedExam", Toast.LENGTH_SHORT).show()
        }

        promts_and_responses_list = mutableListOf()

        recyclerView = findViewById(R.id.container)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = Adapter(promts_and_responses_list)
        recyclerView.adapter = adapter

        promt = findViewById(R.id.promt)



        val btn_image: ImageView = findViewById(R.id.btn_image)

        // expanding the EditText
        promt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isExpanded && !s.isNullOrEmpty()) {
                    expandEditTextWidth()
                } else if (s.isNullOrEmpty()) {
                    resetEditTextWidth()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btn_image.setOnClickListener {
            // when user clicked on image icon
            if (checkAndRequestPermissions()) {
                dispatchTakePictureIntent()
            }
        }

        val camera: ImageView = findViewById(R.id.btn_camera)
        //val mic: ImageView = findViewById(R.id.mic)
        val image: ImageView = findViewById(R.id.btn_image)

        camera.setOnClickListener {
            if (checkAndRequestPermissions()) {
                dispatchTakePictureIntent()
            }
        }

        image.setOnClickListener {
            if (checkAndRequestPermissions()) {
                dispatchPickImageFromGalleryIntent()
            }
        }

        sendPromtBtn = findViewById(R.id.send)
        sendPromtBtn.setOnClickListener {
            if (promt.text.isEmpty()) {
                return@setOnClickListener
            }

            var chatRepo = ChatRepository()
            chatRepo.createChatCompletion(promt.text.toString(), this)

            makePromt(promt.text.toString(), "user")
            promt.setText("")

            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(promt.windowToken, 0)

            Toast.makeText(this@MainActivity, "ok", Toast.LENGTH_LONG).show()
        }

        val user_image_delete_btn:ImageView=findViewById(R.id.btn_delete_image)
        user_image_delete_btn.setOnClickListener{
            findViewById<ConstraintLayout>(R.id.image_container).visibility = View.GONE

        }

    }


    //end of the oncreate



    private fun expandEditTextWidth() {
        val constraintLayout = findViewById<ConstraintLayout>(R.id.inputBar)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Clear constraints to allow EditText to expand
        constraintSet.clear(R.id.promt, ConstraintSet.START)
        constraintSet.connect(R.id.promt, ConstraintSet.START, R.id.btn_camera, ConstraintSet.END, 8)
        constraintSet.connect(R.id.promt, ConstraintSet.END, R.id.send, ConstraintSet.START, 8)
        constraintSet.setHorizontalWeight(R.id.promt, 1.0f)
        constraintSet.applyTo(constraintLayout)

        isExpanded = true
    }

    private fun resetEditTextWidth() {
        val constraintLayout = findViewById<ConstraintLayout>(R.id.inputBar)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Reset constraints to original positions
        constraintSet.clear(R.id.promt, ConstraintSet.START)
        constraintSet.connect(R.id.promt, ConstraintSet.START, R.id.btn_image, ConstraintSet.END, 8)
        constraintSet.connect(R.id.promt, ConstraintSet.END, R.id.send, ConstraintSet.START, 8)
        constraintSet.setHorizontalWeight(R.id.promt, 0f)
        constraintSet.applyTo(constraintLayout)

        isExpanded = false
    }

    private fun makePromt(text: String, personType: String) {
        sendPromtBtn

        // setting send icon gray so that users think the response is loading
        val color = ContextCompat.getColor(this@MainActivity, R.color.gray)
        val tintColor = ColorStateList.valueOf(color)
        sendPromtBtn.setImageTintList(tintColor)

        findViewById<ConstraintLayout>(R.id.iconHolder).visibility = View.GONE
        val prompt = MessageResponses(text, personType)
        adapter.insertUserPromt(prompt)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        sendPromtBtn.isClickable = true
    }

    fun ok(message: String?) {
        val prompt = MessageResponses(message.toString(), "ai")
        adapter.insertUserPromt(prompt)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        // setting send icon to original so that users now allowed to enter more prompts
        val color = ContextCompat.getColor(this@MainActivity, R.color.blue)
        val tintColor = ColorStateList.valueOf(color)
        sendPromtBtn.setImageTintList(tintColor)
        sendPromtBtn.isClickable = true
    }

    fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showToastFromChatRepository(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = mutableListOf<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dispatchTakePictureIntent()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    imageUri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        lateinit var currentPhotoPath: String
    }




    private fun displayImageInImageContainer(imageBitmap: Bitmap) {
        val userImage = findViewById<ImageView>(R.id.user_sent_image)
        userImage.setImageBitmap(imageBitmap)
        userImage.scaleType=ImageView.ScaleType.FIT_CENTER
        findViewById<ConstraintLayout>(R.id.image_container).visibility = View.VISIBLE
    }
    private fun dispatchPickImageFromGalleryIntent() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_PICK -> {
                if (resultCode == RESULT_OK && data != null && data.data != null) {
                    val imageUri = data.data
                    try {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        displayImageInImageContainer(imageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    displayImageInImageContainer(imageBitmap)
                }
            }
        }
    }

}
