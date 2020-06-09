package com.musicplayer.activities

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.musicplayer.R
import com.musicplayer.services.MusicService
import java.util.concurrent.TimeUnit


class PlayerActivity : AppCompatActivity() {

    private var musicSrv: MusicService? = null
    private var musicBound = false

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

            view.findViewById<TextView>(R.id.activity_player_tv_title).text = currentSong?.getTitle()
            view.findViewById<TextView>(R.id.activity_player_tv_artist).text =
                currentSong?.getArtist()
            // view.findViewById<TextView>(R.id.progressTextView).text = // TODO
            view.findViewById<Button>(R.id.activity_player_btn_play).setBackgroundResource(
                R.drawable.ic_pause_circle_filled
            )

            val duration = currentSong!!.getDuration()
            view.findViewById<TextView>(R.id.activity_player_tv_duration).text = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)))

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
}