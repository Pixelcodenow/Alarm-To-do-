package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

enum class AmbientSound(val displayName: String, val description: String) {
    RAIN("Rain", "Soothing rainfall with gentle drop transients"),
    OCEAN("Ocean", "Rolling waves with natural swelling crests"),
    FOREST("Forest", "Gentle woodland breeze and peaceful birds"),
    SOFT_WIND("Soft Wind", "Calm aerodynamic whispers and warmth"),
    AMBIENT("Ambient", "Harmonic zen singing drone chord"),
    FIREPLACE("Fireplace", "Warm ember rumble with crackling snaps")
}

class ProceduralAudioEngine {
    private val sampleRate = 22050
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 2)

    private var audioTrack: AudioTrack? = null
    private var chimeTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private var previewJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var ambientVolume: Float = 0.75f
        set(value) {
            field = value.coerceIn(0f, 1f)
            audioTrack?.setVolume(field)
        }

    @Volatile
    var chimeVolume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
            chimeTrack?.setVolume(field)
        }

    @Volatile
    var currentSound: AmbientSound? = null
        private set

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var isPaused: Boolean = false
        private set

    init {
        initTracks()
    }

    private fun initTracks() {
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.setVolume(ambientVolume)

            chimeTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            chimeTrack?.setVolume(chimeVolume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(sound: AmbientSound) {
        if (currentSound == sound && isPlaying && !isPaused) return
        stop()

        currentSound = sound
        isPlaying = true
        isPaused = false

        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            initTracks()
        }

        try {
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        synthesisJob = scope.launch {
            generateAudioStream(sound)
        }
    }

    fun pause() {
        if (isPlaying && !isPaused) {
            isPaused = true
            try {
                audioTrack?.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resume() {
        if (isPlaying && isPaused) {
            isPaused = false
            try {
                audioTrack?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        previewJob?.cancel()
        previewJob = null
        synthesisJob?.cancel()
        synthesisJob = null
        isPlaying = false
        isPaused = false
        try {
            audioTrack?.stop()
            audioTrack?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun preview(sound: AmbientSound, durationMs: Long = 3500L, onComplete: (() -> Unit)? = null) {
        play(sound)
        previewJob?.cancel()
        previewJob = scope.launch {
            delay(durationMs)
            stop()
            onComplete?.invoke()
        }
    }

    fun playBellChime(frequency: Double = 528.0) {
        scope.launch {
            try {
                if (chimeTrack?.state != AudioTrack.STATE_INITIALIZED) {
                    initTracks()
                }
                chimeTrack?.play()
                val numSamples = (sampleRate * 2.2).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val decay = exp(-t * 2.2)
                    // Fundamental + subtle fifth harmonic overtone
                    val wave = sin(2.0 * PI * frequency * t) * 0.75 + sin(2.0 * PI * (frequency * 1.5) * t) * 0.25
                    val sample = (wave * decay * Short.MAX_VALUE * 0.65).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = sample.toShort()
                }
                chimeTrack?.write(buffer, 0, numSamples)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun generateAudioStream(sound: AmbientSound) {
        val chunkSamples = 2048
        val pcmBuffer = ShortArray(chunkSamples)
        val random = Random()

        var sampleIndex: Long = 0
        var lowPassFilter1 = 0.0
        var lowPassFilter2 = 0.0
        var brownNoise = 0.0

        // Bird chirp variables for Forest
        var chirpTimer = random.nextInt(sampleRate * 3) + sampleRate * 2
        var chirpSample = 0
        var chirpDuration = 0
        var chirpBaseFreq = 2600.0

        while (scope.isActive && isPlaying) {
            if (isPaused) {
                delay(100)
                continue
            }

            for (i in 0 until chunkSamples) {
                val t = sampleIndex.toDouble() / sampleRate
                var sampleOut = 0.0

                when (sound) {
                    AmbientSound.RAIN -> {
                        // Continuous rainfall: low-pass filtered white noise
                        val white = random.nextDouble() * 2.0 - 1.0
                        lowPassFilter1 += 0.25 * (white - lowPassFilter1)
                        lowPassFilter2 += 0.15 * (lowPassFilter1 - lowPassFilter2)
                        var rainBase = lowPassFilter2 * 0.45

                        // Stochastic raindrop clicks
                        if (random.nextInt(600) == 0) {
                            val dropFreq = 1600.0 + random.nextInt(1200)
                            rainBase += sin(2.0 * PI * dropFreq * t) * (0.35 + random.nextDouble() * 0.35)
                        }
                        sampleOut = rainBase
                    }

                    AmbientSound.OCEAN -> {
                        // Ocean swell: 6.2 second period sine wave modulating pink noise
                        val swellPeriod = 6.2
                        val swellEnvelope = (0.2 + 0.8 * (0.5 * (1.0 + sin(2.0 * PI * (1.0 / swellPeriod) * t)))).pow(1.6)
                        val white = random.nextDouble() * 2.0 - 1.0
                        val filterAlpha = 0.05 + 0.25 * swellEnvelope
                        lowPassFilter1 += filterAlpha * (white - lowPassFilter1)
                        sampleOut = lowPassFilter1 * swellEnvelope * 0.7
                    }

                    AmbientSound.FOREST -> {
                        // Rustling wind through trees + distant bird calls
                        val white = random.nextDouble() * 2.0 - 1.0
                        lowPassFilter1 += 0.08 * (white - lowPassFilter1)
                        var forest = lowPassFilter1 * 0.35

                        // Bird chirp scheduling
                        if (chirpSample >= chirpDuration) {
                            chirpTimer--
                            if (chirpTimer <= 0) {
                                chirpSample = 0
                                chirpDuration = (sampleRate * (0.15 + random.nextDouble() * 0.18)).toInt()
                                chirpBaseFreq = 2400.0 + random.nextInt(1000)
                                chirpTimer = (sampleRate * (2.0 + random.nextDouble() * 3.5)).toInt()
                            }
                        } else {
                            val ct = chirpSample.toDouble() / sampleRate
                            val chirpEnv = sin(PI * (chirpSample.toDouble() / chirpDuration))
                            val freqMod = chirpBaseFreq + sin(ct * 60.0) * 400.0
                            val chirpTone = sin(2.0 * PI * freqMod * ct) * chirpEnv * 0.28
                            forest += chirpTone
                            chirpSample++
                        }
                        sampleOut = forest
                    }

                    AmbientSound.SOFT_WIND -> {
                        // Warm brownian noise with slow resonant sweep
                        val white = random.nextDouble() * 2.0 - 1.0
                        brownNoise += white * 0.05
                        brownNoise *= 0.99
                        val windSwell = 0.5 + 0.5 * sin(2.0 * PI * 0.12 * t)
                        sampleOut = brownNoise * (0.4 + 0.35 * windSwell)
                    }

                    AmbientSound.AMBIENT -> {
                        // Harmonic meditative triad (C3, G3, C4, E4) with subtle detune chorus
                        val c3 = sin(2.0 * PI * 130.81 * t)
                        val g3 = sin(2.0 * PI * 196.00 * t + 0.5)
                        val c4 = sin(2.0 * PI * 261.63 * t + 1.0)
                        val e4 = sin(2.0 * PI * 329.63 * t + 1.5)
                        val detune = sin(2.0 * PI * 262.10 * t) // Chorus shimmer
                        val drone = (c3 * 0.32 + g3 * 0.26 + c4 * 0.22 + e4 * 0.18 + detune * 0.1) * 0.55
                        sampleOut = drone
                    }

                    AmbientSound.FIREPLACE -> {
                        // Fireplace: warm low rumble + distinct crackle/pop clicks
                        val white = random.nextDouble() * 2.0 - 1.0
                        lowPassFilter1 += 0.03 * (white - lowPassFilter1)
                        var fire = lowPassFilter1 * 0.45

                        // Crackle impulses
                        if (random.nextInt(320) == 0) {
                            val popMagnitude = (random.nextDouble() * 0.75) * if (random.nextBoolean()) 1.0 else -1.0
                            fire += popMagnitude
                        }
                        sampleOut = fire
                    }
                }

                val clamped = (sampleOut * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                pcmBuffer[i] = clamped.toShort()
                sampleIndex++
            }

            audioTrack?.write(pcmBuffer, 0, chunkSamples)
        }
    }

    fun release() {
        stop()
        try {
            audioTrack?.release()
            audioTrack = null
            chimeTrack?.release()
            chimeTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
