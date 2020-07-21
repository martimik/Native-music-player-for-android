package com.musicplayer.activities

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.musicplayer.R
import com.musicplayer.services.MusicService
import java.util.concurrent.TimeUnit


class PlayerActivity : AppCompatActivity() {

    private var musicSrv: MusicService? = null
    private var musicBound = false
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var broadCastReceiver : BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_player)

        bindService()
        registerReceiver()

        val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

        view.findViewById<Button>(R.id.activity_player_btn_play).setOnClickListener {
            if(musicSrv!!.isPlaying()) {
                musicSrv!!.pausePlay()
                it.setBackgroundResource(R.drawable.ic_play_circle_filled)
            } else {
                musicSrv!!.resumePlay()
                it.setBackgroundResource(R.drawable.ic_pause_circle_filled)
            }
        }

        view.findViewById<Button>(R.id.activity_player_btn_next).setOnClickListener() {
            musicSrv!!.nextTrack()
        }

        view.findViewById<Button>(R.id.activity_player_btn_previous).setOnClickListener() {
            musicSrv!!.previousTrack()
        }

        view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                musicSrv!!.seek(progress)
                updateProgressBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                handler.removeCallbacks(updateSeekBar)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    val currentDuration = (progress * 1000 ).toLong()
                    view.findViewById<TextView>(R.id.activity_player_tv_progress).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(currentDuration), TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration)))
                }
            }
        })

        // TODO
        view.findViewById<Button>(R.id.activity_player_btn_shuffle).setOnClickListener() {

            val btn = view.findViewById<Button>(R.id.activity_player_btn_shuffle)

            if(musicSrv!!.isShuffled()) {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(btn.background),
                    ContextCompat.getColor(this, R.color.white)
                )
            } else {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(btn.background),
                    ContextCompat.getColor(this, R.color.green)
                )
            }
            musicSrv!!.toggleShuffle()
        }

        // TODO
        view.findViewById<Button>(R.id.activity_player_btn_loop).setOnClickListener() {

            val btn = view.findViewById<Button>(R.id.activity_player_btn_loop)

            if(musicSrv!!.isLooping()){
                DrawableCompat.setTint(
                    DrawableCompat.wrap(btn.background),
                    ContextCompat.getColor(this, R.color.white)
                )
            } else {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(btn.background),
                    ContextCompat.getColor(this, R.color.green)
                )
            }
            musicSrv!!.toggleLoop()
        }

        view.findViewById<Button>(R.id.activity_player_btn_return).setOnClickListener() {
            this.finish()
        }
    }

    // Service connection
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            val binder = service as MusicService.MusicBinder
            musicSrv = binder.getService()

            musicBound = true

            updateUI()
            updateProgressBar()

        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    // Bind musicService on start
    private fun bindService() {
        bindService(
            Intent(this, MusicService::class.java),
            musicConnection,
            Context.BIND_AUTO_CREATE
        )
        musicBound = true
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateSeekBar)
    }

    // Unbind service on destroy
    override fun onDestroy() {
        super.onDestroy()
        if (musicBound) {
            this.unbindService(musicConnection)
            musicBound = false
        }
        handler.removeCallbacks(updateSeekBar)
    }

    // Update player UI
    private fun updateUI() {

        if (musicSrv != null) {

            val currentSong = musicSrv!!.getCurrentTrack()

            val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

            if(musicSrv!!.isPlaying()){
                findViewById<Button>(R.id.activity_player_btn_play).setBackgroundResource(R.drawable.ic_pause_circle_filled)
            } else {
                findViewById<Button>(R.id.activity_player_btn_play).setBackgroundResource(R.drawable.ic_play_circle_filled)
            }
            if(musicSrv!!.isShuffled()){
                DrawableCompat.setTint(
                    DrawableCompat.wrap(findViewById<Button>(R.id.activity_player_btn_shuffle).background),
                    ContextCompat.getColor(this, R.color.green)
                )
            }

            view.findViewById<TextView>(R.id.activity_player_tv_title).text = currentSong.getTitle()
            view.findViewById<TextView>(R.id.activity_player_tv_artist).text = currentSong.getArtist()

            val duration = currentSong.getDuration()

            view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).max = (duration / 1000).toInt()
            view.findViewById<TextView>(R.id.activity_player_tv_duration).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))

            val albumArt = view.findViewById<ImageView>(R.id.activity_player_iv_cover)

            val albumId = currentSong.getAlbumId()

            val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)

            albumArt.setImageURI(uri)

            if (albumArt.drawable == null) {
                albumArt.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.cover_placeholder
                    )
                )
            }
        }
    }

    fun updateProgressBar() {
        handler.postDelayed(updateSeekBar, 100)
    }

    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {

            val currentDuration: Long = musicSrv!!.getPosition()

            val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

            view.findViewById<TextView>(R.id.activity_player_tv_progress).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(currentDuration), TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration)))

            view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).progress = (currentDuration / 1000).toInt()

            // Run this thread again after 1 second
            handler.postDelayed(this, 100)

        }
    }

    private fun registerReceiver() {
        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // val otpCode = intent.getStringExtra("UI_UPDATE")

                updateUI()
            }
        }
        registerReceiver(broadCastReceiver, IntentFilter("UI_UPDATE"))
    }
}