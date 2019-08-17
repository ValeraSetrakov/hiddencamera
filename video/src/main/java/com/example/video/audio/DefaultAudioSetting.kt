package com.example.video.audio

import android.media.AudioFormat
import android.media.CamcorderProfile
import android.media.MediaCodecInfo
import android.media.MediaRecorder

class DefaultAudioSetting(camcorderProfile: CamcorderProfile) : AudioConsumer.Setting(
    sampleRate = camcorderProfile.audioSampleRate,
    channelCount = camcorderProfile.audioChannels,
    bitRate = camcorderProfile.audioBitRate,
    mime = "audio/mp4a-latm",
    profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC,
    maxInputSize = 16384,
    audioSource = MediaRecorder.AudioSource.MIC,
    channelType = AudioFormat.CHANNEL_IN_MONO,
    encodingType = AudioFormat.ENCODING_PCM_16BIT
)