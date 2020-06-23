package com.musicplayer.activities

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.musicplayer.R
import com.musicplayer.services.MusicService
import java.time.LocalTime
import java.util.concurrent.TimeUnit


class PlayerActivity : AppCompatActivity() {

    private var musicSrv: MusicService? = null
    private var musicBound = false
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_player)

        bindService()

        val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

        view.findViewById<Button>(R.id.activity_player_btn_play).setOnClickListener {
            if(musicSrv!!.getStatus()){
                musicSrv?.pausePlay()
                it.setBackgroundResource(R.drawable.ic_play_circle_filled)
            } else {
                musicSrv?.resumePlay()
                it.setBackgroundResource(R.drawable.ic_pause_circle_filled)
            }
        }

        view.findViewById<Button>(R.id.activity_player_btn_next).setOnClickListener() {
            musicSrv?.nextSong()
            updateUI()
        }

        view.findViewById<Button>(R.id.activity_player_btn_previous).setOnClickListener() {
            musicSrv?.prevSong()
            updateUI()
        }

        view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                musicSrv!!.seek(progress)
                updateProgressBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                handler.removeCallbacks(updateTimeTask)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    val currentDuration = (progress * 1000 ).toLong()
                    view.findViewById<TextView>(R.id.activity_player_tv_progress).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(currentDuration), TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration)))
                }
            }
        })
/*
        // TODO
        view.findViewById<Button>(R.id.player_activity_btn_shuffle).setOnClickListener() {

        }

        // TODO
        view.findViewById<Button>(R.id.player_activity_btn_replay).setOnClickListener() {

        }
 */
        view.findViewById<Button>(R.id.activity_player_btn_return).setOnClickListener() {
            this.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    // Service connection
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            val binder = service as MusicService.MusicBinder
            musicSrv = binder.getService()

            musicBound = true

            updateUI()

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

    // Unbind service on destroy
    override fun onDestroy() {
        super.onDestroy()
        if (musicBound) {
            this.unbindService(musicConnection)
            musicBound = false
        }
    }

    // Update player UI
    private fun updateUI() {

        if (musicSrv != null) {

            val currentSong = musicSrv?.getSong()

            val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

            if(musicSrv!!.getStatus()){
                view.findViewById<Button>(R.id.activity_player_btn_play).setBackgroundResource(R.drawable.ic_pause_circle_filled)
            } else {
                view.findViewById<Button>(R.id.activity_player_btn_play).setBackgroundResource(R.drawable.ic_play_circle_filled)
            }

            view.findViewById<TextView>(R.id.activity_player_tv_title).text = currentSong?.getTitle()
            view.findViewById<TextView>(R.id.activity_player_tv_artist).text = currentSong?.getArtist()

            updateProgressBar()

            val duration = currentSong!!.getDuration()

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
        handler.postDelayed(updateTimeTask, 100)
    }

    private val updateTimeTask: Runnable = object : Runnable {
        override fun run() {
            //val totalDuration: Long = musicSrv!!.getSong().getDuration()
            val currentDuration: Long = musicSrv!!.getPosition()

            val view = findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

            //view.findViewById<TextView>(R.id.activity_player_tv_duration).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(totalDuration), TimeUnit.MILLISECONDS.toSeconds(totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration)))
            view.findViewById<TextView>(R.id.activity_player_tv_progress).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(currentDuration), TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration)))

            // Updating progress bar
            view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).progress = (currentDuration / 1000).toInt()

            // Running this thread after 100 milliseconds
            handler.postDelayed(this, 100)

        }
    }
}