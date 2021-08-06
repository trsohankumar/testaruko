package com.example.aruko.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import org.opencv.core.Mat


object CameraParameters {
    private const val CAMERA_MATRIX_ROWS = 3
    private const val CAMERA_MATRIX_COLS = 3
    private const val DISTORTION_COEFFICIENTS_SIZE = 5
    private const val CAMERA_CALIBRATION_PKG = "mg.rivolink.app.aruco.camera.calibration"
    private const val CAMERA_CALIBRATION_PREFS = "CameraCalibrationActivity"
    fun tryLoad(activity: Activity, cameraMatrix: Mat, distCoeffs: Mat): Boolean {
        try {
            val context = activity.createPackageContext(
                CAMERA_CALIBRATION_PKG,
                Context.CONTEXT_IGNORE_SECURITY
            )
            val cameraPrefs =
                context.getSharedPreferences(CAMERA_CALIBRATION_PREFS, Context.MODE_PRIVATE)
            if (cameraPrefs.getFloat("0", -1f) == -1f) return false else load(
                cameraPrefs,
                cameraMatrix,
                distCoeffs
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return true
    }

    private fun load(sharedPref: SharedPreferences, cameraMatrix: Mat, distCoeffs: Mat) {
        val cameraMatrixArray = DoubleArray(CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS)
        for (i in 0 until CAMERA_MATRIX_ROWS) {
            for (j in 0 until CAMERA_MATRIX_COLS) {
                val id = i * CAMERA_MATRIX_ROWS + j
                cameraMatrixArray[id] =
                    sharedPref.getFloat(Integer.toString(id), -1f).toDouble()
            }
        }
        cameraMatrix.put(0, 0, *cameraMatrixArray)
        val distortionCoefficientsArray = DoubleArray(DISTORTION_COEFFICIENTS_SIZE)
        val shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS
        for (i in shift until DISTORTION_COEFFICIENTS_SIZE + shift) {
            distortionCoefficientsArray[i - shift] =
                sharedPref.getFloat(Integer.toString(i), -1f).toDouble()
        }
        distCoeffs.put(0, 0, *distortionCoefficientsArray)
    }
}