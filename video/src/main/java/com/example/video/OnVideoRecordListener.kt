package com.example.video

interface OnVideoRecordListener {
    fun onRecordVideo(videoFilePath: String)
    fun onRecordStartVideo(videoFilePath: String)
    fun onRecordEndVideo(videoFilePath: String)
    /**
     * If first video fragment is end video fragment
     */
    fun onRecordStartEndVideo(videoFilePath: String)
}