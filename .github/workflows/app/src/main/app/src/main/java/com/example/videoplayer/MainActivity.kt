package com.example.videoplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.videoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var position = 0L
    private var playWhenReady = true

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            play(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPick.setOnClickListener { pickVideo.launch(arrayOf("video/*")) }

        binding.btnUrl.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.startsWith("http")) play(Uri.parse(url))
            else Toast.makeText(this, "Unesi važeći URL", Toast.LENGTH_SHORT).show()
        }

        intent?.data?.let { play(it) }
    }

    private fun play(uri: Uri) {
        currentUri = uri
        position = 0L
        releasePlayer()
        initPlayer()
    }

    private fun initPlayer() {
        val uri = currentUri ?: return
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.playWhenReady = playWhenReady
            exo.seekTo(position)
            exo.prepare()
        }
    }

    private fun releasePlayer() {
        player?.let {
            position = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
        }
        player = null
    }

    override fun onStart() { super.onStart(); initPlayer() }
    override fun onStop() { super.onStop(); releasePlayer() }
}
