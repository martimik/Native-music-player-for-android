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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.musicplayer.R
import com.musicplayer.adapters.AlbumDetailAdapter
import com.musicplayer.data.DataManager
import com.musicplayer.data.AudioModel
import com.musicplayer.services.MusicService


class AlbumDetailActivity : AppCompatActivity() {

    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_album_detail)

        val albumString = intent.extras?.getInt("albumPosition")
        val mData = DataManager.returnInstance().getAlbum(albumString!!)

        val view = findViewById<CoordinatorLayout>(R.id.albumDetailContainer)

        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).title = mData.getAlbumName()
        view.findViewById<ImageView>(R.id.app_bar_image).setImageDrawable(
            ContextCompat.getDrawable(this,
                R.drawable.ic_music_icon
            ))
        view.findViewById<TextView>(R.id.activity_album_detail_tv_artist).text = mData.getArtist()
        view.findViewById<TextView>(R.id.activity_album_detail_tv_year).text = String.format("%01d Tracks", mData.getSongs().size)

        val albumArt = view.findViewById<ImageView>(R.id.app_bar_image)

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, mData.getAlbumKey())

        albumArt.setImageURI(uri)

        if(albumArt.drawable == null) {
            albumArt.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.cover_placeholder
            ))
        }

        findViewById<LinearLayout>(R.id.small_player).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.view_small_player_btn_play).setOnClickListener {
            if(musicSrv!!.getStatus()){
                musicSrv?.pausePlay()
                it.setBackgroundResource(R.drawable.ic_play_circle_outline)
            } else {
                musicSrv?.resumePlay()
                it.setBackgroundResource(R.drawable.ic_pause_circle_outline)
            }
        }

        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).setCollapsedTitleTextColor(ColorStateList.valueOf(ContextCompat.getColor(this,
            R.color.white
        )))
        view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).setExpandedTitleTextColor(ColorStateList.valueOf(ContextCompat.getColor(this,
            R.color.white
        )))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AlbumDetailAdapter(this, mData.getSongs() as MutableList<AudioModel>)

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
    }

    fun setSong(songPos : Int){
        musicSrv?.setSong(songPos)
        musicSrv?.playSong()
    }

    fun setPlaylist(newPlaylist: MutableList<AudioModel>) {
        musicSrv?.setPlaylist(newPlaylist)
    }

    private fun updateSmallPlayer(){

        val smallPlayer = findViewById<LinearLayout>(R.id.small_player)

        if(musicSrv != null && musicSrv!!.playlistExists()) {

            val curSong = musicSrv?.getSong()

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

            if(musicSrv!!.getStatus()){
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
}