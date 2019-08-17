package com.valerasetrakov.data

import java.io.File
import java.lang.Exception

object VideoRepository {

    fun handleVideo (videoFragment: VideoFragment) {
        print("VideoRepository handleVideo videoFragment $videoFragment")
        uploadVideo(videoFragment)
        removeVideo(videoFragment)
    }

    private fun removeVideo (videoFragment: VideoFragment) {
//        removeVideo(videoFragment.video)
    }

    private fun removeVideo (videoPath: String) {
        print("VideoRepository removeVideo $videoPath")
        val videoFile = File(videoPath)
        val isDelete = videoFile.delete()
        if (!isDelete)
            throw RemoveFileException(videoPath)
    }

    private fun uploadVideo (videoFragment: VideoFragment) {
        print("VideoRepository uploadVideo videoFragment $videoFragment")
        VideoApi.requestUploadVideoFragment(videoFragment)
    }


    class RemoveFileException (file:String): Exception("Remove file $file error")
}