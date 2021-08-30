package com.example.cameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraapp.databinding.ActivityMainBinding
import kotlin.coroutines.cancellation.CancellationException


class MainActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE =1
        private const val CAMERA = 2
        private const val TAG ="camerax"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


    }

    private var imageCapture:ImageCapture? = null
    private var similarProductsAdapter:SimilarProductsAdapter? = null
    private var similarProductsList:ArrayList<SimilarProduct>? = null

    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if(allPermissionsGranted()){
            Toast.makeText(this, "Permissions Granted",Toast.LENGTH_SHORT).show()
            startCamera()

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS,
                CAMERA_PERMISSION_CODE
            )
        }
        similarProductsList = Products.defaultProductsList()
        setUpSimilarProducts()
    }


    private fun startCamera(){
        val cameraProvideFuture = ProcessCameraProvider
            .getInstance(this)

        cameraProvideFuture.addListener({
            val cameraProvider:ProcessCameraProvider = cameraProvideFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.viewCamera.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build()


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview,imageCapture
                )
            }catch ( e:Exception){
                Log.d(TAG,"startCameraFail")
            }
        },ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(allPermissionsGranted()){
                startCamera()
            }else{
                Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(
                baseContext,it
            ) == PackageManager.PERMISSION_GRANTED
        }


    private fun setUpSimilarProducts(){
        binding.rvSimilarProducts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        similarProductsAdapter = SimilarProductsAdapter(similarProductsList!!,this)
        binding.rvSimilarProducts.adapter = similarProductsAdapter
    }


}