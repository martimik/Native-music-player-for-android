package com.musicplayer.activities

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.musicplayer.R
import com.musicplayer.adapters.ArtistDetailAdapter
import com.musicplayer.data.DataManager
import com.musicplayer.services.MusicService

class ArtistDetailActivity : AppCompatActivity() {

    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false

    private lateinit var broadCastReceiver : BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_artist_detail)

        registerReceiver()

        val albumString = intent.extras?.getString("artist")
        val mData = DataManager.returnInstance().getArtist(albumString!!)

        val view = findViewById<CoordinatorLayout>(R.id.artistDetailContainer)

        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).title = albumString
        val artistArt = view.findViewById<ImageView>(R.id.app_bar_image)

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")

        for(album in mData!!){
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, album.getAlbumKey())
            artistArt.setImageURI(uri)
            if(artistArt.drawable != null) break
        }

        if(artistArt.drawable == null) {
            artistArt.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.artist_placeholder
            ))
        }

        findViewById<ConstraintLayout>(R.id.small_player).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.view_small_player_btn_play).setOnClickListener {
            if(musicSrv!!.isPlaying()){
                musicSrv?.pausePlay()
                it.setBackgroundResource(R.drawable.ic_play_circle_outline)
            } else {
                musicSrv?.resumePlay()
                it.setBackgroundResource(R.drawable.ic_pause_circle_outline)
            }
        }

        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).setCollapsedTitleTextColor(
            ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.white
            )))
        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).setExpandedTitleTextColor(
            ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.white
            )))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = ArtistDetailAdapter(this, mData)

    }

    // Service connection
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder

            musicSrv = binder.getService()
            musicBound = true

            updateSmallPlayer()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    // Bind musicService on start
    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        }
        musicBound = true
    }

    // Update ui when resuming activity
    override fun onResume() {
        super.onResume()
        updateSmallPlayer()
    }

    // Unbind service on destroy
    override fun onDestroy() {
        super.onDestroy()
        if(musicBound) {
            this.unbindService(musicConnection)
            musicBound = false
        }

        unregisterReceiver(broadCastReceiver)
    }

    private fun updateSmallPlayer(){

        val smallPlayer = findViewById<ConstraintLayout>(R.id.small_player)

        if(musicSrv != null && musicSrv!!.playlistExists()) {

            val curSong = musicSrv?.getCurrentTrack()

            smallPlayer.visibility = View.VISIBLE
            smallPlayer.findViewById<TextView>(R.id.view_small_player_tv_title).text = curSong?.getTitle()
            smallPlayer.findViewById<TextView>(R.id.view_small_player_tv_artist).text = curSong?.getArtist()

            val albumArt = smallPlayer.findViewById<ImageView>(R.id.view_small_player_iv_cover)

            val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, curSong!!.getAlbumId())

            albumArt.setImageURI(uri)

            if(albumArt.drawable == null) {
                albumArt.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.cover_placeholder
                ))
            }

            if(musicSrv!!.isPlaying()){
                smallPlayer.findViewById<Button>(R.id.view_small_player_btn_play).setBackgroundResource(
                    R.drawable.ic_pause_circle_outline
                )
            } else {
                smallPlayer.findViewById<Button>(R.id.view_small_player_btn_play).setBackgroundResource(
                    R.drawable.ic_play_circle_outline
                )
            }

        } else {
            smallPlayer.visibility = View.GONE
        }
    }

    private fun registerReceiver() {
        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val otpCode = intent.getStringExtra("UI_UPDATE")

                updateSmallPlayer()
            }
        }
        registerReceiver(broadCastReceiver, IntentFilter("UI_UPDATE"))
    }
}