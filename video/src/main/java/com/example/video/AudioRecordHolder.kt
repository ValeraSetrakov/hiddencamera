package com.example.video

import android.media.AudioRecord
import com.example.video.consumer.SourceHolder
import com.example.video.producer.DistanceHolder

class AudioRecordHolder: SourceHolder<AudioRecord>, DistanceHolder<AudioRecord> {
    private lateinit var target: AudioRecord

    override val distance: AudioRecord
        get() = target

    override var source: AudioRecord
        get() = target
        set(value) {
            target = value
        }
}