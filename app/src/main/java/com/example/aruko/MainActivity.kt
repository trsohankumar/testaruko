package com.example.aruko

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aruko.databinding.ActivityMainBinding
import com.example.aruko.utils.CameraParameters
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.aruco.Aruco
import org.opencv.aruco.DetectorParameters
import org.opencv.aruco.DetectorParameters.*
import org.opencv.aruco.Dictionary
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*



class MainActivity : AppCompatActivity() , CameraBridgeViewBase.CvCameraViewListener2 {

    private var cameraMatrix: Mat? = null
    private var distCoeffs: Mat? = null

    private var rgb: Mat? = null
    private var gray: Mat? = null
    private var rvecs: Mat? = null
    private var tvecs: Mat? = null

    private var ids: MatOfInt? = null
    private var corners: List<Mat>? = null
    private var dictionary: Dictionary? = null
    private var parameters: DetectorParameters? = null

  //  private var renderer: Renderer3D? = null
    private var camera: CameraBridgeViewBase? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    private val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {

            if (status == SUCCESS) {

                var message = ""
                message = if (loadCameraParams())
                    getString(R.string.success_ocv_loading)
                else{
                    getString(R.string.success_ocv_loading)
                   // getString(R.string.error_camera_params)
                }


                camera!!.enableView()
               Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity,"IM here",Toast.LENGTH_SHORT).show()
                super.onManagerConnected(status)
            }
        }
    }


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)



        camera = findViewById(R.id.main_camera)
        camera!!.visibility = View.VISIBLE
        camera!!.setCvCameraViewListener(this)
       // cameraView.setMaxFrameSize(1280, 720);
        camera!!.height


    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        else Toast.makeText(
            this,
            getString(R.string.error_native_lib),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun loadCameraParams(): Boolean {
        cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1)
        distCoeffs = Mat.zeros(5, 1, CvType.CV_64FC1)
        return CameraParameters.tryLoad(this, cameraMatrix!!, distCoeffs!!)
    }

    override fun onPause() {
        super.onPause()
        if (camera != null) camera!!.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (camera != null) camera!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        rgb = Mat()
        corners = LinkedList()
        parameters = create()
        dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50)
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat? {
        Imgproc.cvtColor(inputFrame.rgba(), rgb, Imgproc.COLOR_RGBA2RGB)
        gray = inputFrame.gray()
        ids = MatOfInt()
        Aruco.detectMarkers(gray, dictionary, corners, ids, parameters)
        if (corners!!.isNotEmpty()) {
            Aruco.drawDetectedMarkers(rgb, corners, ids)
            rvecs = Mat()
            tvecs = Mat()
            //code to get the id of the detected markers
            val id = ids!![0, 0][0].toInt()

            Aruco.estimatePoseSingleMarkers(corners, 0.04f, cameraMatrix, distCoeffs, rvecs, tvecs)
        //    Log.d("Corner",corners!![0].toString())

            //if(id == 33){
                Imgproc.circle(rgb, Point(375.0,250.0),50, Scalar(0.0,0.0,0.0),10)
           // }





        }

        return rgb
    }

    override fun onCameraViewStopped() {
        rgb!!.release()
    }

}


