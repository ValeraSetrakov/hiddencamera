package com.example.video.consumer

/** Media recorder stream **/
//class MRStreamConsumer: Consumer<Surface>() {
//
//    private val mediaRecorder = MediaRecorder()
//    val parcelFileDescriptors = ParcelFileDescriptor.createPipe()
//    val receiver = parcelFileDescriptors[0]
//    val sender  = parcelFileDescriptors[1]
//    val inStream = ParcelFileDescriptor.AutoCloseInputStream(receiver)
//
//    lateinit var surface: Surface
//
//    override fun prepare(): Surface {
//        videoModulePrintThreadName("Prepare stream recorder")
//        val camcorderProfile = CamcorderProfile.get(cameraId.toInt(), CamcorderProfile.QUALITY_LOW)
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//        mediaRecorder.setProfile(camcorderProfile)
//        mediaRecorder.setOutputFile(sender.fileDescriptor)
//        mediaRecorder.prepare()
//        surface = mediaRecorder.surface
//        return surface
////        mediaRecorder.defaultPrepare(sender.fileDescriptor, cameraId)
//    }
//
//    override fun start() {
//        videoModulePrintThreadName("Start stream recorder")
//        mediaRecorder.start()
//        while (true){
//            val videoFragment = inStream.readBytes()
//            videoModulePrintThreadName("Video fragment size ${videoFragment.size}")
//        }
//    }
//
//    override fun stop() {
//        videoModulePrintThreadName("Stop stream recorder")
//        mediaRecorder.stop()
//    }
//}