package com.example.alarm.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AlarmSoundPlayer(private val context: Context) {
  private val TAG = "AlarmSoundPlayer"

  private var mediaPlayer: MediaPlayer? = null
  private var audioTrack: AudioTrack? = null
  private var vibrator: Vibrator? = null
  private var vibratorManager: VibratorManager? = null

  private var soundJob: Job? = null
  private var vibrationJob: Job? = null
  private var volumeJob: Job? = null

  private val scope = CoroutineScope(Dispatchers.Default)

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibrator = vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun start(
    soundType: String,
    volumePercent: Int,
    isVibrate: Boolean,
    isGradualVolume: Boolean
  ) {
    stop()

    // 1. Vibration
    if (isVibrate) {
      startVibration()
    }

    // 2. Audio Playback
    val targetVolume = (volumePercent.coerceIn(5, 100) / 100f)
    val startVolume = if (isGradualVolume) 0.1f else targetVolume

    if (soundType.equals(SoundType.DEFAULT.id, ignoreCase = true)) {
      playSystemRingtone(startVolume, targetVolume, isGradualVolume)
    } else {
      playSynthesizedSound(soundType, startVolume, targetVolume, isGradualVolume)
    }
  }

  private fun playSystemRingtone(startVol: Float, targetVol: Float, gradual: Boolean) {
    try {
      var alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      if (alertUri == null) {
        alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      }

      mediaPlayer = MediaPlayer().apply {
        setDataSource(context, alertUri)
        val audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ALARM)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
        setAudioAttributes(audioAttributes)
        isLooping = true
        setVolume(startVol, startVol)
        prepare()
        start()
      }

      if (gradual && targetVol > startVol) {
        volumeJob = scope.launch {
          var current = startVol
          val step = (targetVol - startVol) / 20f
          for (i in 1..20) {
            delay(1000)
            if (!isActive || mediaPlayer == null) break
            current += step
            val v = current.coerceAtMost(targetVol)
            mediaPlayer?.setVolume(v, v)
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to play default system ringtone, falling back to synthesizer: ${e.message}")
      playSynthesizedSound(SoundType.ENERGETIC.id, startVol, targetVol, gradual)
    }
  }

  private fun playSynthesizedSound(
    soundType: String,
    startVol: Float,
    targetVol: Float,
    gradual: Boolean
  ) {
    soundJob = scope.launch {
      val sampleRate = 44100
      val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )

      val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

      val format = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

      try {
        val track = AudioTrack.Builder()
          .setAudioAttributes(attributes)
          .setAudioFormat(format)
          .setBufferSizeInBytes(bufferSize)
          .setTransferMode(AudioTrack.MODE_STREAM)
          .build()

        audioTrack = track
        track.play()

        var currentVol = startVol
        track.setVolume(currentVol)

        if (gradual && targetVol > startVol) {
          volumeJob = scope.launch {
            var vol = startVol
            val step = (targetVol - startVol) / 20f
            for (i in 1..20) {
              delay(1000)
              if (!isActive) break
              vol += step
              currentVol = vol.coerceAtMost(targetVol)
              track.setVolume(currentVol)
            }
          }
        }

        // Generate synthetic waveforms based on sound choice
        val type = SoundType.fromId(soundType)
        while (isActive) {
          when (type) {
            SoundType.ENERGETIC -> {
              // Rapid pulse: 880Hz then 1320Hz then silence
              writeTone(track, sampleRate, 880.0, 150)
              writeTone(track, sampleRate, 1320.0, 150)
              writeSilence(track, sampleRate, 100)
              writeTone(track, sampleRate, 880.0, 150)
              writeTone(track, sampleRate, 1320.0, 250)
              writeSilence(track, sampleRate, 350)
            }
            SoundType.DIGITAL -> {
              // 3 short digital beeps at 1046Hz (High C) then pause
              repeat(3) {
                writeTone(track, sampleRate, 1046.0, 80)
                writeSilence(track, sampleRate, 50)
              }
              writeSilence(track, sampleRate, 400)
            }
            SoundType.GENTLE -> {
              // Soothing chime: 523Hz (C5) then 659Hz (E5) then 784Hz (G5)
              writeTone(track, sampleRate, 523.25, 300, fadeOut = true)
              writeTone(track, sampleRate, 659.25, 300, fadeOut = true)
              writeTone(track, sampleRate, 783.99, 450, fadeOut = true)
              writeSilence(track, sampleRate, 600)
            }
            SoundType.LOUD -> {
              // Piercing alert siren warble: 800Hz to 1600Hz
              writeSweep(track, sampleRate, 750.0, 1500.0, 300)
              writeSweep(track, sampleRate, 1500.0, 750.0, 300)
              writeSilence(track, sampleRate, 100)
            }
            else -> {
              writeTone(track, sampleRate, 880.0, 200)
              writeSilence(track, sampleRate, 150)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error in AudioTrack synthesis: ${e.message}")
      }
    }
  }

  private fun writeTone(track: AudioTrack, sampleRate: Int, freq: Double, durationMs: Int, fadeOut: Boolean = false) {
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val angle = 2.0 * Math.PI * i / (sampleRate / freq)
      val envelope = if (fadeOut) (1.0 - (i.toDouble() / numSamples)) else 1.0
      val sample = (sin(angle) * 32767 * envelope).toInt().toShort()
      buffer[i] = sample
    }
    track.write(buffer, 0, buffer.size)
  }

  private fun writeSweep(track: AudioTrack, sampleRate: Int, startFreq: Double, endFreq: Double, durationMs: Int) {
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    var phase = 0.0
    for (i in 0 until numSamples) {
      val progress = i.toDouble() / numSamples
      val currentFreq = startFreq + (endFreq - startFreq) * progress
      phase += 2.0 * Math.PI * currentFreq / sampleRate
      buffer[i] = (sin(phase) * 32767).toInt().toShort()
    }
    track.write(buffer, 0, buffer.size)
  }

  private fun writeSilence(track: AudioTrack, sampleRate: Int, durationMs: Int) {
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(numSamples)
    track.write(buffer, 0, buffer.size)
  }

  private fun startVibration() {
    vibrationJob = scope.launch {
      val pattern = longArrayOf(0, 500, 200, 500, 200, 500, 600)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createWaveform(pattern, 0)
        vibrator?.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, 0)
      }
    }
  }

  fun stop() {
    soundJob?.cancel()
    soundJob = null
    volumeJob?.cancel()
    volumeJob = null
    vibrationJob?.cancel()
    vibrationJob = null

    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
      mediaPlayer = null
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping mediaPlayer: ${e.message}")
    }

    try {
      audioTrack?.apply {
        pause()
        flush()
        stop()
        release()
      }
      audioTrack = null
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping audioTrack: ${e.message}")
    }

    try {
      vibrator?.cancel()
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling vibrator: ${e.message}")
    }
  }
}
