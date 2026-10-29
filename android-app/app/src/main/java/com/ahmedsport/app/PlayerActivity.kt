package com.ahmedsport.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var statusPanel: LinearLayout
    private lateinit var statusMessage: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var retryButton: Button
    private lateinit var fullscreenButton: ImageButton
    private lateinit var muteButton: ImageButton

    private var player: ExoPlayer? = null
    private var currentSourceIndex = 0
    private var isFullscreen = false
    private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        statusPanel = findViewById(R.id.statusPanel)
        statusMessage = findViewById(R.id.statusMessage)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        retryButton = findViewById(R.id.retryButton)
        fullscreenButton = findViewById(R.id.fullscreenButton)
        muteButton = findViewById(R.id.muteButton)

        fullscreenButton.setOnClickListener { setFullscreen(!isFullscreen) }
        muteButton.setOnClickListener { toggleMute() }
        retryButton.setOnClickListener {
            currentSourceIndex = 0
            prepareAndPlay()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullscreen) {
                    setFullscreen(false)
                } else {
                    finish()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (player == null) initializePlayer()
    }

    private fun initializePlayer() {
        if (StreamConfig.streamUrls.isEmpty()) {
            showError("لم يتم إعداد رابط بث صالح. أضف الروابط المصرح بها إلى stream-urls.properties ثم أعد البناء.")
            return
        }

        currentSourceIndex = 0
        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { exoPlayer ->
                playerView.player = exoPlayer
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> showLoading("جارٍ تحميل البث…")
                            Player.STATE_READY -> hideStatus()
                            Player.STATE_ENDED -> showError("انتهى البث الحالي. اضغط إعادة المحاولة للاتصال من جديد.")
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        handleSourceError(error)
                    }
                })
                prepareAndPlay()
            }
    }

    private fun prepareAndPlay() {
        val urls = StreamConfig.streamUrls
        if (urls.isEmpty()) {
            showError("لا توجد مصادر بث متاحة حالياً.")
            return
        }

        val safeIndex = currentSourceIndex.coerceIn(0, urls.lastIndex)
        currentSourceIndex = safeIndex
        showLoading("جارٍ الاتصال بالبث…")

        val mediaItem = MediaItem.Builder()
            .setUri(urls[safeIndex])
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        player?.apply {
            stop()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    private fun handleSourceError(error: PlaybackException) {
        val urls = StreamConfig.streamUrls
        if (currentSourceIndex < urls.lastIndex) {
            currentSourceIndex += 1
            showLoading("تعذر تشغيل المصدر الحالي، نجرب مصدراً بديلاً…")
            prepareAndPlay()
        } else {
            showError("انقطع البث أو تعذر الاتصال. تحقق من الإنترنت ثم اضغط إعادة المحاولة.\nرمز الخطأ: ${error.errorCodeName}")
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        player?.volume = if (isMuted) 0f else 1f
        muteButton.setImageResource(if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume)
        muteButton.contentDescription = if (isMuted) "تشغيل الصوت" else "كتم الصوت"
    }

    private fun showLoading(message: String) {
        statusPanel.visibility = View.VISIBLE
        loadingIndicator.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        statusMessage.text = message
    }

    private fun showError(message: String) {
        statusPanel.visibility = View.VISIBLE
        loadingIndicator.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
        statusMessage.text = message
    }

    private fun hideStatus() {
        statusPanel.visibility = View.GONE
    }

    private fun setFullscreen(enabled: Boolean) {
        isFullscreen = enabled
        WindowCompat.setDecorFitsSystemWindows(window, !enabled)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (enabled) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            fullscreenButton.contentDescription = getString(R.string.exit_fullscreen)
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            fullscreenButton.contentDescription = getString(R.string.fullscreen)
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
        playerView.player = null
    }

    override fun onDestroy() {
        if (isFullscreen) setFullscreen(false)
        super.onDestroy()
    }
}
