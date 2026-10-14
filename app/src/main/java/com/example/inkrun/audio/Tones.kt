package com.example.inkrun.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.inkrun.core.Constants
import kotlin.math.PI
import kotlin.math.sin

object Tones {

    private var audioTrack: AudioTrack? = null
    private var enabled = true
    private val masterVolume = Constants.MASTER_VOLUME

    private val bufferSize by lazy {
        AudioTrack.getMinBufferSize(
            Constants.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
    }

    fun initialize() {
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                android.media.AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(Constants.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun playJump() {
        playTone(440.0, 0.15, 0.3) // A4
    }

    fun playLand() {
        playTone(220.0, 0.1, 0.2) // A3
    }

    fun playCoin() {
        playTone(880.0, 0.1, 0.4) // A5
    }

    fun playHazard() {
        playTone(110.0, 0.3, 0.5) // A2
    }

    fun playLevelComplete() {
        // Play a short melody
        playSequence(arrayOf(
            Pair(523.25, 0.15), // C5
            Pair(659.25, 0.15), // E5
            Pair(783.99, 0.15), // G5
            Pair(1046.50, 0.3)  // C6
        ))
    }

    fun playGameOver() {
        playSequence(arrayOf(
            Pair(220.0, 0.2),   // A3
            Pair(196.0, 0.2),   // G3
            Pair(174.61, 0.2),  // F3
            Pair(164.81, 0.4)   // E3
        ))
    }

    private fun playTone(frequency: Double, durationSec: Double, volume: Double) {
        if (!enabled || audioTrack == null) return

        val sampleRate = Constants.SAMPLE_RATE
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val amplitude = (32767 * volume * masterVolume).toInt()
        val omega = 2 * PI * frequency / sampleRate

        for (i in 0 until numSamples) {
            // Simple sine wave with exponential decay
            val decay = 1.0 - (i.toDouble() / numSamples) * 0.8
            val value = (amplitude * sin(omega * i) * decay).toShort()
            samples[i] = value
        }

        audioTrack?.write(samples, 0, numSamples)
    }

    private fun playSequence(notes: Array<Pair<Double, Double>>) {
        if (!enabled || audioTrack == null) return

        val sampleRate = Constants.SAMPLE_RATE
        var totalSamples = 0
        for (note in notes) {
            totalSamples += (sampleRate * note.second).toInt()
        }

        val samples = ShortArray(totalSamples)
        var offset = 0

        for (note in notes) {
            val frequency = note.first
            val durationSec = note.second
            val numSamples = (sampleRate * durationSec).toInt()
            val amplitude = (32767 * 0.3 * masterVolume).toInt()
            val omega = 2 * PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                val decay = 1.0 - (i.toDouble() / numSamples) * 0.5
                val value = (amplitude * sin(omega * i) * decay).toShort()
                samples[offset + i] = value
            }
            offset += numSamples
        }

        audioTrack?.write(samples, 0, totalSamples)
    }

    fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}