package com.musicplayer.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.musicplayer.R
import com.musicplayer.services.MusicService
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment {

    private var mContext: Context
    private var musicSrv: MusicService? = null
    private var handler = Handler(Looper.getMainLooper())
    private val fm: FragmentManager

    private lateinit var broadCastReceiver: BroadcastReceiver

    private lateinit var v: View

    constructor(mContext: Context, musicService: MusicService?, fm: FragmentManager) : super() {
        this.mContext = mContext
        this.musicSrv = musicService
        this.fm = fm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.v = inflater.inflate(R.layout.fragment_player, container, false)

        val view = v.findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

        // Play button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_play).setOnClickListener {
            Log.i("test btn", "play")


            val btn = view.findViewById<ImageButton>(R.id.activity_player_btn_play)

            if (musicSrv!!.isPlaying()) {
                musicSrv!!.pausePlay()
                btn.setImageResource(R.drawable.ic_play_circle_filled)
            } else {
                musicSrv!!.resumePlay()
                btn.setImageResource(R.drawable.ic_pause_circle_filled)
            }
        }

        // Next track button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_next).setOnClickListener {
            musicSrv!!.nextTrack()
        }

        // Previous track button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_previous).setOnClickListener {
            musicSrv!!.previousTrack()
            Log.i("test btn", "prev")

        }

        // Seek bar listener
        view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar)
            .setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val progress = seekBar.progress
                    musicSrv!!.seek(progress)
                    updateProgressBar()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    handler.removeCallbacks(updateSeekBar)
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val currentDuration = (progress * 1000).toLong()
                        view.findViewById<TextView>(R.id.activity_player_tv_progress).text =
                            String.format(
                                "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(currentDuration),
                                TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(currentDuration)
                                )
                            )
                    }
                }
            })

        // Shuffle button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_shuffle).setOnClickListener {

            Log.i("test btn", "shuffle")

            val btn = view.findViewById<ImageButton>(R.id.activity_player_btn_shuffle)

            if (musicSrv!!.isShuffled()) {
                btn.setColorFilter(ContextCompat.getColor(mContext, R.color.white))
            } else {
                btn.setColorFilter(ContextCompat.getColor(mContext, R.color.green))
            }
            musicSrv!!.toggleShuffle()
        }

        // Loop button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_loop).setOnClickListener {

            val btn = view.findViewById<ImageButton>(R.id.activity_player_btn_loop)

            if (musicSrv!!.isLooping()) {
                btn.setColorFilter(ContextCompat.getColor(mContext, R.color.white))
            } else {
                btn.setColorFilter(ContextCompat.getColor(mContext, R.color.green))
            }
            musicSrv!!.toggleLoop()
        }

        // Return button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_return).setOnClickListener {
            activity?.finish()
        }

        // Playlist button onClick-listener
        view.findViewById<ImageButton>(R.id.activity_player_btn_playlist).setOnClickListener {
            if (v.findViewById<ConstraintLayout>(R.id.activity_player_cl_container) != null) {
                if (savedInstanceState == null) {
                    fm.beginTransaction()
                        .replace(
                            R.id.activity_player_container,
                            PlaylistFragment(mContext, musicSrv, fm)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    // Stop thread updating seek bar after pausing activity
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateSeekBar)
        mContext.unregisterReceiver(broadCastReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        updateProgressBar()
    }

    // Unbind service on destroy
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
    }

    fun updateProgressBar() {
        handler.postDelayed(updateSeekBar, 100)
    }

    // Thread updating seek bar every second
    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {

            val currentDuration: Long = musicSrv!!.getPosition()

            val view = v.findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

            view.findViewById<TextView>(R.id.activity_player_tv_progress).text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentDuration),
                TimeUnit.MILLISECONDS.toSeconds(currentDuration) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(currentDuration)
                )
            )

            view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).progress =
                (currentDuration / 1000).toInt()

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
        mContext.registerReceiver(broadCastReceiver, IntentFilter("UI_UPDATE"))
    }

    // Update player UI
    private fun updateUI() {

        val currentSong = musicSrv!!.getCurrentTrack()

        val view = v.findViewById<ConstraintLayout>(R.id.activity_player_cl_container)

        if (musicSrv!!.isPlaying()) {
            v.findViewById<ImageButton>(R.id.activity_player_btn_play)
                .setImageResource(R.drawable.ic_pause_circle_filled)
        } else {
            v.findViewById<ImageButton>(R.id.activity_player_btn_play)
                .setImageResource(R.drawable.ic_play_circle_filled)
        }
        if (musicSrv!!.isShuffled()) {
            DrawableCompat.setTint(
                DrawableCompat.wrap(v.findViewById<ImageButton>(R.id.activity_player_btn_shuffle).background),
                ContextCompat.getColor(mContext, R.color.green)
            )
        }

        view.findViewById<TextView>(R.id.activity_player_tv_title).text = currentSong.getTitle()
        view.findViewById<TextView>(R.id.activity_player_tv_artist).text = currentSong.getArtist()

        val duration = currentSong.getDuration()

        view.findViewById<SeekBar>(R.id.activity_player_sb_progressbar).max =
            (duration / 1000).toInt()
        view.findViewById<TextView>(R.id.activity_player_tv_duration).text = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )

        val albumArt = view.findViewById<ImageView>(R.id.activity_player_iv_cover)

        val albumId = currentSong.getAlbumId()

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)

        albumArt.setImageURI(uri)

        if (albumArt.drawable == null) {
            albumArt.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    R.drawable.cover_placeholder
                )
            )
        }
    }
}