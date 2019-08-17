package com.example.video

import android.view.Surface
import com.example.video.consumer.SourceHolder
import com.example.video.producer.DistanceHolder

class SurfaceHolder: DistanceHolder<Surface>, SourceHolder<Surface> {

    private lateinit var surface: Surface

    override val distance: Surface
        get() = surface

    override var source: Surface
        get() = surface
        set(value) {
            surface = value
        }
}