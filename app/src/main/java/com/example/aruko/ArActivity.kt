package com.example.aruko

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.ModelRenderable

import com.google.ar.core.HitResult
import android.view.MotionEvent
import com.google.ar.sceneform.assets.RenderableSource
import android.widget.Toast
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import android.app.Activity
import android.os.Build

import android.app.ActivityManager
import android.app.AlertDialog
import android.net.Uri
import android.util.Log

import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import java.util.function.Consumer


class ArActivity : AppCompatActivity() {
    var arFragment: ArFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) return
        setContentView(R.layout.activity_ar)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        arFragment!!.arSceneView.planeRenderer.isVisible = false
        arFragment!!.setOnTapArPlaneListener { hitresult: HitResult, plane: Plane, motionevent: MotionEvent? ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return@setOnTapArPlaneListener
            val anchor = hitresult.createAnchor()
            placeObject(arFragment, anchor)
        }
    }

    private fun placeObject(arFragment: ArFragment?, anchor: Anchor) {
        val GLTF_ASSET = "Red Sphere.glb"

        ModelRenderable.builder()
            .setSource(
                this, RenderableSource.builder().setSource(
                    this,
                    Uri.parse(GLTF_ASSET),
                    RenderableSource.SourceType.GLB
                )
                    .setScale(0.5f)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            .setRegistryId(GLTF_ASSET)
            .build()
            .thenAccept(Consumer { modelRenderable: ModelRenderable ->
                addNodeToScene(
                    arFragment,
                    anchor,
                    modelRenderable
                )
            })
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(arFragment: ArFragment?, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment!!.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)

    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MIN_OPENGL_VERSION = 3.0
    }
}