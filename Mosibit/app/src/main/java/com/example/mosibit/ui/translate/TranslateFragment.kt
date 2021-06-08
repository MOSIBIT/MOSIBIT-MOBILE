package com.example.mosibit.ui.translate

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mosibit.R
import com.example.mosibit.data.Data
import com.example.mosibit.databinding.FragmentTranslateBinding
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraXPreviewHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.components.PermissionHelper
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import java.io.File
import java.util.*


class TranslateFragment : Fragment() {
    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding
    private lateinit var previewLayout: ViewGroup
    private lateinit var view: ConstraintLayout
    private lateinit var translateViewModel: TranslateViewModel
    private lateinit var data: Data
    private lateinit var processor: FrameProcessor

    companion object {
        private const val TAG = "MainActivity"
        private const val BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb"
        private const val INPUT_VIDEO_STREAM_NAME = "input_video"
        private const val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private const val OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks"
        private const val INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands"
        private const val NUM_HANDS = 2
        private val CAMERA_FACING = CameraFacing.BACK
        private const val FLIP_FRAMES_VERTICALLY = true

        init {
            // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private var previewFrameTexture: SurfaceTexture? = null

    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private var previewDisplayView: SurfaceView? = null

    // Creates and manages an {@link EGLContext}.
    private var eglManager: EglManager? = null

    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private var converter: ExternalTextureConverter? = null

    // ApplicationInfo for retrieving metadata defined in the manifest.
    private var applicationInfo: ApplicationInfo? = null

    // Handles camera access via the {@link CameraX} Jetpack support library.
    private var cameraHelper: CameraXPreviewHelper? = null

    //hand coordinate
    //Wrist Hand
    var wristX = 0.0
    var wristY = 0.0

    //Thumb Finger
    var thumbCmcX = 0.0
    var thumbCmcY = 0.0
    var thumbMcpX = 0.0
    var thumbMcpY = 0.0
    var thumbIpX = 0.0
    var thumbIpY = 0.0
    var thumbTipX = 0.0
    var thumbTipY = 0.0

    //Index Finger
    var indexFingerMcpX = 0.0
    var indexFingerMcpY = 0.0
    var indexFingerPipX = 0.0
    var indexFingerPipY = 0.0
    var indexFingerDipX = 0.0
    var indexFingerDipY = 0.0
    var indexFingerTipX = 0.0
    var indexFingerTipY = 0.0

    //Middle Finger
    var middleFingerMcpX = 0.0
    var middleFingerMcpY = 0.0
    var middleFingerPipX = 0.0
    var middleFingerPipY = 0.0
    var middleFingerDipX = 0.0
    var middleFingerDipY = 0.0
    var middleFingerTipX = 0.0
    var middleFingerTipY = 0.0

    //Ring Finger
    var ringFingerMcpX = 0.0
    var ringFingerMcpY = 0.0
    var ringFingerPipX = 0.0
    var ringFingerPipY = 0.0
    var ringFingerDipX = 0.0
    var ringFingerDipY = 0.0
    var ringFingerTipX = 0.0
    var ringFingerTipY = 0.0

    //Pinky Finger
    var pinkyMcpX = 0.0
    var pinkyMcpY = 0.0
    var pinkyPipX = 0.0
    var pinkyPipY = 0.0
    var pinkyDipX = 0.0
    var pinkyDipY = 0.0
    var pinkyTipX = 0.0
    var pinkyTipY = 0.0

    //For Collecting Frame Size
    var heightRatio = 0.0
    var widthRatio = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        activity?.actionBar?.hide()
        _binding = FragmentTranslateBinding.inflate(LayoutInflater.from(inflater.context), container, false)
        view = inflater.inflate(R.layout.fragment_translate, container, false) as ConstraintLayout
        translateViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[TranslateViewModel::class.java]
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        previewLayout = view.findViewById(R.id.preview_display_layout)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            applicationInfo = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Cannot find application info: $e")
        }
        previewDisplayView = SurfaceView(context)
        setupPreviewDisplayView()

        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(context)
        eglManager = EglManager(null)

        initializeProcessor()

        packetCallback()

        getPrediction()
    }

    // Used to obtain the content view for this application. If you are extending this class, and
    // have a custom layout, override this method and return the custom layout.
    private val contentViewLayoutResId: Int
        get() = R.layout.fragment_translate

    override fun onResume() {
        super.onResume()
        converter = ExternalTextureConverter(
            eglManager!!.context, 2
        )
        converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter!!.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(context as Activity?)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        converter!!.close()

        // Hide preview display until we re-open the camera again.
        previewDisplayView!!.visibility = View.GONE
        _binding?.textView?.text  = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onCameraStarted(surfaceTexture: SurfaceTexture?) {
        previewFrameTexture = surfaceTexture
        // Make the display view visible to start showing the preview. This triggers the
        // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
        previewDisplayView!!.visibility = View.VISIBLE
    }

    private fun cameraTargetResolution(): Size? {
        return null // No preference and let the camera (helper) decide.
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            onCameraStarted(
                surfaceTexture
            )
        }
        val cameraFacing = CAMERA_FACING
        cameraHelper!!.startCamera(
            context as Activity?,
            cameraFacing,  /*unusedSurfaceTexture=*/
            null,
            cameraTargetResolution()
        )
    }

    private fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }

    private fun onPreviewDisplaySurfaceChanged(width: Int, height: Int) {
        // (Re-)Compute the ideal size of the camera-preview display (the area that the
        // camera-preview frames get rendered onto, potentially with scaling and rotation)
        // based on the size of the SurfaceView that contains the display.
        val viewSize = computeViewSize(width, height)
        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
        val isCameraRotated = cameraHelper!!.isCameraRotated
        heightRatio = displaySize.height.toDouble()
        widthRatio = displaySize.width.toDouble()

        // Connect the converter to the camera-preview frames as its input (via
        // previewFrameTexture), and configure the output width and height as the computed
        // display size.
        converter!!.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            if (isCameraRotated) displaySize.height else displaySize.width,
            if (isCameraRotated) displaySize.width else displaySize.height
        )
    }

    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = previewLayout
        viewGroup.addView(previewDisplayView)
        previewDisplayView!!
            .holder
            .addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        onPreviewDisplaySurfaceChanged(width, height)
                        _binding?.textView?.text  = "....."
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(null)
                    }
                })
    }

    private fun initializeProcessor() {
        processor = FrameProcessor(
            context,
            eglManager!!.nativeContext,
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME
        )
        processor
            .videoSurfaceOutput
            .setFlipY(FLIP_FRAMES_VERTICALLY)
        PermissionHelper.checkAndRequestCameraPermissions(context as Activity?)
        val packetCreator = processor.packetCreator
        val inputSidePackets: MutableMap<String, Packet> = HashMap()
        inputSidePackets[INPUT_NUM_HANDS_SIDE_PACKET_NAME] =
            packetCreator.createInt32(NUM_HANDS)
        processor.setInputSidePackets(inputSidePackets)
    }

    private fun packetCallback() {

        val sd: File = Environment.getExternalStorageDirectory()
        val debugFolder = File(sd, "mediapipe")
        if (!debugFolder.exists()) {
            debugFolder.mkdirs()
        }
        processor.addPacketCallback(
            OUTPUT_LANDMARKS_STREAM_NAME
        ) { packet: Packet ->
            Log.v(TAG, "Received multi-hand landmarks packet.")
            val multiHandLandmarks =
                PacketGetter.getProtoVector(
                    packet,
                    NormalizedLandmarkList.parser()
                )
            Log.v(
                TAG,
                "[TS:"
                        + packet.timestamp
                        + "] "
                        + getMultiHandLandmarksDebugString(multiHandLandmarks)
            )
        }
//        processor.addPacketCallback(
//            OUTPUT_LANDMARKS_STREAM_NAME
//        ) { packet: Packet? ->
//            //declare variable for collecting data from getMultiHandLandmarksDebugString function
//            val handData = ArrayList<ArrayList<ArrayList<Double>>>()
//            val handsCoordinates = ArrayList<ArrayList<Double>>()
//            //Wrist Hand
//            val listWristX = ArrayList<Double>()
//            val listWristY = ArrayList<Double>()
//
//            //listThumb Finger
//            val listThumbCmcX = ArrayList<Double>()
//            val listThumbCmcY = ArrayList<Double>()
//            val listThumbMcpX = ArrayList<Double>()
//            val listThumbMcpY = ArrayList<Double>()
//            val listThumbIpX = ArrayList<Double>()
//            val listThumbIpY = ArrayList<Double>()
//            val listThumbTipX = ArrayList<Double>()
//            val listThumbTipY = ArrayList<Double>()
//
//            //listIndex Finger
//            val listIndexFingerMcpX = ArrayList<Double>()
//            val listIndexFingerMcpY = ArrayList<Double>()
//            val listIndexFingerPipX = ArrayList<Double>()
//            val listIndexFingerPipY = ArrayList<Double>()
//            val listIndexFingerDipX = ArrayList<Double>()
//            val listIndexFingerDipY = ArrayList<Double>()
//            val listIndexFingerTipX = ArrayList<Double>()
//            val listIndexFingerTipY = ArrayList<Double>()
//
//            //listMiddle Finger
//            val listMiddleFingerMcpX = ArrayList<Double>()
//            val listMiddleFingerMcpY = ArrayList<Double>()
//            val listMiddleFingerPipX = ArrayList<Double>()
//            val listMiddleFingerPipY = ArrayList<Double>()
//            val listMiddleFingerDipX = ArrayList<Double>()
//            val listMiddleFingerDipY = ArrayList<Double>()
//            val listMiddleFingerTipX = ArrayList<Double>()
//            val listMiddleFingerTipY = ArrayList<Double>()
//
//            //listRing Finger
//            val listRingFingerMcpX = ArrayList<Double>()
//            val listRingFingerMcpY = ArrayList<Double>()
//            val listRingFingerPipX = ArrayList<Double>()
//            val listRingFingerPipY = ArrayList<Double>()
//            val listRingFingerDipX = ArrayList<Double>()
//            val listRingFingerDipY = ArrayList<Double>()
//            val listRingFingerTipX = ArrayList<Double>()
//            val listRingFingerTipY = ArrayList<Double>()
//
//            //listPinky Finger
//            val listPinkyMcpX = ArrayList<Double>()
//            val listPinkyMcpY = ArrayList<Double>()
//            val listPinkyPipX = ArrayList<Double>()
//            val listPinkyPipY = ArrayList<Double>()
//            val listPinkyDipX = ArrayList<Double>()
//            val listPinkyDipY = ArrayList<Double>()
//            val listPinkyTipX = ArrayList<Double>()
//            val listPinkyTipY = ArrayList<Double>()
//            //add data to arraylist handData
//            handData.add(0, handsCoordinates)
//            handsCoordinates.add(0, listWristX)
//            handsCoordinates.add(1, listWristY)
//            handsCoordinates.add(2, listThumbCmcX)
//            handsCoordinates.add(3, listThumbCmcY)
//            handsCoordinates.add(4, listThumbMcpX)
//            handsCoordinates.add(5, listThumbMcpY)
//            handsCoordinates.add(6, listThumbIpX)
//            handsCoordinates.add(7, listThumbIpY)
//            handsCoordinates.add(8, listThumbTipX)
//            handsCoordinates.add(9, listThumbTipY)
//            handsCoordinates.add(10, listIndexFingerMcpX)
//            handsCoordinates.add(11, listIndexFingerMcpY)
//            handsCoordinates.add(12, listIndexFingerPipX)
//            handsCoordinates.add(13, listIndexFingerPipY)
//            handsCoordinates.add(14, listIndexFingerDipX)
//            handsCoordinates.add(15, listIndexFingerDipY)
//            handsCoordinates.add(16, listIndexFingerTipX)
//            handsCoordinates.add(17, listIndexFingerTipY)
//            handsCoordinates.add(18, listMiddleFingerMcpX)
//            handsCoordinates.add(19, listMiddleFingerMcpY)
//            handsCoordinates.add(20, listMiddleFingerPipX)
//            handsCoordinates.add(21, listMiddleFingerPipY)
//            handsCoordinates.add(22, listMiddleFingerDipX)
//            handsCoordinates.add(23, listMiddleFingerDipY)
//            handsCoordinates.add(24, listMiddleFingerTipX)
//            handsCoordinates.add(25, listMiddleFingerTipY)
//            handsCoordinates.add(26, listRingFingerMcpX)
//            handsCoordinates.add(27, listRingFingerMcpY)
//            handsCoordinates.add(28, listRingFingerPipX)
//            handsCoordinates.add(29, listRingFingerPipY)
//            handsCoordinates.add(30, listRingFingerDipX)
//            handsCoordinates.add(31, listRingFingerDipY)
//            handsCoordinates.add(32, listRingFingerTipX)
//            handsCoordinates.add(33, listRingFingerTipY)
//            handsCoordinates.add(34, listPinkyMcpX)
//            handsCoordinates.add(35, listPinkyMcpY)
//            handsCoordinates.add(36, listPinkyPipX)
//            handsCoordinates.add(37, listPinkyPipY)
//            handsCoordinates.add(38, listPinkyDipX)
//            handsCoordinates.add(39, listPinkyDipY)
//            handsCoordinates.add(40, listPinkyTipX)
//            handsCoordinates.add(41, listPinkyTipY)
//            //Initialize data
//            data = Data(handData)
//            //set prediction with data
//            translateViewModel.post(data)
//            println("Width :$widthRatio")
//            println("Height :$heightRatio")
//            println("Data Landmarks :$handData")
//        }

    }

    private fun getMultiHandLandmarksDebugString(multiHandLandmarks: List<NormalizedLandmarkList>): String {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks"
        }
        var multiHandLandmarksStr =
            """
                Number of hands detected: ${multiHandLandmarks.size}
                
            """
                .trimIndent()
        var handIndex = 0
        for (landmarks in multiHandLandmarks) {
            multiHandLandmarksStr +=
                """	#Hand landmarks for hand[$handIndex]: ${landmarks.landmarkCount}
                """.trimMargin()

            var landmarkIndex = 0
            for (landmark in landmarks.landmarkList) {
                multiHandLandmarksStr +=
                    """|
                        Landmark [$landmarkIndex]: (${landmark.x},${landmark.y}, ${landmark.z})
                    """.trimMargin()
                ++landmarkIndex
            }
//            wristX = multiHandLandmarks[0].getLandmark(0).x.toDouble()
//            wristY = multiHandLandmarks[0].getLandmark(0).y.toDouble()
//            thumbCmcX = multiHandLandmarks[0].getLandmark(1).x.toDouble()
//            thumbCmcY = multiHandLandmarks[0].getLandmark(1).y.toDouble()
//            thumbMcpX = multiHandLandmarks[0].getLandmark(2).x.toDouble()
//            thumbMcpY = multiHandLandmarks[0].getLandmark(2).y.toDouble()
//            thumbIpX = multiHandLandmarks[0].getLandmark(3).x.toDouble()
//            thumbIpY = multiHandLandmarks[0].getLandmark(3).y.toDouble()
//            thumbTipX = multiHandLandmarks[0].getLandmark(4).x.toDouble()
//            thumbTipY = multiHandLandmarks[0].getLandmark(4).y.toDouble()
//            indexFingerMcpX = multiHandLandmarks[0].getLandmark(5).x.toDouble()
//            indexFingerMcpY = multiHandLandmarks[0].getLandmark(5).y.toDouble()
//            indexFingerPipX = multiHandLandmarks[0].getLandmark(6).x.toDouble()
//            indexFingerPipY = multiHandLandmarks[0].getLandmark(6).y.toDouble()
//            indexFingerDipX = multiHandLandmarks[0].getLandmark(7).x.toDouble()
//            indexFingerDipY = multiHandLandmarks[0].getLandmark(7).y.toDouble()
//            indexFingerTipX = multiHandLandmarks[0].getLandmark(8).x.toDouble()
//            indexFingerTipY = multiHandLandmarks[0].getLandmark(8).y.toDouble()
//            middleFingerMcpX = multiHandLandmarks[0].getLandmark(9).x.toDouble()
//            middleFingerMcpY = multiHandLandmarks[0].getLandmark(9).y.toDouble()
//            middleFingerPipX = multiHandLandmarks[0].getLandmark(10).x.toDouble()
//            middleFingerPipY = multiHandLandmarks[0].getLandmark(10).y.toDouble()
//            middleFingerDipX = multiHandLandmarks[0].getLandmark(11).x.toDouble()
//            middleFingerDipY = multiHandLandmarks[0].getLandmark(11).y.toDouble()
//            middleFingerTipX = multiHandLandmarks[0].getLandmark(12).x.toDouble()
//            middleFingerTipY = multiHandLandmarks[0].getLandmark(12).y.toDouble()
//            ringFingerMcpX = multiHandLandmarks[0].getLandmark(13).x.toDouble()
//            ringFingerMcpY = multiHandLandmarks[0].getLandmark(13).y.toDouble()
//            ringFingerPipX = multiHandLandmarks[0].getLandmark(14).x.toDouble()
//            ringFingerPipY = multiHandLandmarks[0].getLandmark(14).y.toDouble()
//            ringFingerDipX = multiHandLandmarks[0].getLandmark(15).x.toDouble()
//            ringFingerDipY = multiHandLandmarks[0].getLandmark(15).y.toDouble()
//            ringFingerTipX = multiHandLandmarks[0].getLandmark(16).x.toDouble()
//            ringFingerTipY = multiHandLandmarks[0].getLandmark(16).y.toDouble()
//            pinkyMcpX = multiHandLandmarks[0].getLandmark(17).x.toDouble()
//            pinkyMcpY = multiHandLandmarks[0].getLandmark(17).y.toDouble()
//            pinkyPipX = multiHandLandmarks[0].getLandmark(18).x.toDouble()
//            pinkyPipY = multiHandLandmarks[0].getLandmark(18).y.toDouble()
//            pinkyDipX = multiHandLandmarks[0].getLandmark(19).x.toDouble()
//            pinkyDipY = multiHandLandmarks[0].getLandmark(19).y.toDouble()
//            pinkyTipX = multiHandLandmarks[0].getLandmark(20).x.toDouble()
//            pinkyTipY = multiHandLandmarks[0].getLandmark(20).y.toDouble()

            ++handIndex
        }
        return multiHandLandmarksStr
    }

    private fun getPrediction() {
        translateViewModel.testing.observe(viewLifecycleOwner, {
            _binding?.textView?.text = it.alphabet
        })
    }

}