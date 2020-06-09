package com.musicplayer.activities

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.musicplayer.R
import com.musicplayer.data.AudioModel
import com.musicplayer.data.DataManager
import com.musicplayer.fragments.AlbumListFragment
import com.musicplayer.fragments.ArtistListFragment
import com.musicplayer.fragments.SongListFragment
import com.musicplayer.services.MusicService
import com.musicplayer.adapters.SectionsPagerAdapter


class MainActivity : AppCompatActivity() {

    private val dataManager = DataManager()

    private var musicSrv: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false

    override fun onCreate(savedInstanceState: Bundle?) {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 10)
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        val sectionsPagerAdapter = SectionsPagerAdapter(
            this,
            supportFragmentManager
        )

        dataManager.prepare(this)

        // sectionsPagerAdapter.addFragment(PlaylistFragment(), "Playlists") // TODO
        sectionsPagerAdapter.addFragment(AlbumListFragment(this), "Albums")
        sectionsPagerAdapter.addFragment(ArtistListFragment(this), "Artists")
        sectionsPagerAdapter.addFragment(SongListFragment(this), "Songs")

        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)

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
            this.startService(playIntent)
        }
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