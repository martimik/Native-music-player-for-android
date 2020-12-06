package com.musicplayer.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.musicplayer.R
import com.musicplayer.adapters.SectionsPagerAdapter
import com.musicplayer.data.AudioModel
import com.musicplayer.fragments.AlbumListFragment
import com.musicplayer.fragments.ArtistListFragment
import com.musicplayer.fragments.SongListFragment
import com.musicplayer.services.MusicService


class MainActivity : AppCompatActivity() {
    private lateinit var musicSrv: MusicService
    private lateinit var playIntent: Intent
    private var musicBound = false

    private lateinit var broadCastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        }

        super.onCreate(null)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        registerReceiver()
        createNotificationChannel()

        //sectionsPagerAdapter.addFragment(PlaylistFragment(this, musicSrv), "Playlists")
        sectionsPagerAdapter.addFragment(AlbumListFragment(this), "Albums")
        sectionsPagerAdapter.addFragment(ArtistListFragment(this), "Artists")
        sectionsPagerAdapter.addFragment(SongListFragment(this), "Songs")

        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)

        findViewById<ConstraintLayout>(R.id.small_player).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.view_small_player_btn_play).setOnClickListener {
            if (musicSrv.isPlaying()) {
                musicSrv.pausePlay()
                it.setBackgroundResource(R.drawable.ic_play_circle_outline)
            }
            else {
                musicSrv.resumePlay()
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

    private fun createNotificationChannel() {
        val name = "Media Player Controls"
        val descriptionText = "Media Player Controls"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("1", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Bind musicService on start
    override fun onStart() {
        super.onStart()
        playIntent = Intent(this, MusicService::class.java)
        this.startService(playIntent)
        this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
    }

    // Update ui when resuming activity
    override fun onResume() {
        super.onResume()
        updateSmallPlayer()
    }

    // Unbind service on destroy
    override fun onDestroy() {
        super.onDestroy()
        if (musicBound) {
            this.unbindService(musicConnection)
            musicBound = false
            stopService(Intent(this, MusicService::class.java))
        }
        unregisterReceiver(broadCastReceiver)
    }

    fun setSong(songPos: Int) {
        musicSrv.setSong(songPos)
    }

    fun setPlaylist(newPlaylist: List<AudioModel>) {
        musicSrv.setPlaylist(newPlaylist)
    }

    fun addToPlaylist(tracks: List<AudioModel>) {
        musicSrv.addToPlaylist(tracks)
    }

    fun updateSmallPlayer() {
        val smallPlayer = findViewById<ConstraintLayout>(R.id.small_player)

        if (musicBound && musicSrv.playlistExists()) {
            val curSong = musicSrv.getCurrentTrack()

            smallPlayer.visibility = View.VISIBLE
            smallPlayer.findViewById<TextView>(R.id.view_small_player_tv_title).text = curSong.getTitle()
            smallPlayer.findViewById<TextView>(R.id.view_small_player_tv_artist).text = curSong.getArtistName()

            val albumArt = smallPlayer.findViewById<ImageView>(R.id.view_small_player_iv_cover)

            val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, curSong.getAlbumId())

            albumArt.setImageURI(uri)

            if (albumArt.drawable == null) {
                albumArt.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.cover_placeholder
                    )
                )
            }
            if (musicSrv.isPlaying()) {
                smallPlayer.findViewById<Button>(R.id.view_small_player_btn_play).setBackgroundResource(
                    R.drawable.ic_pause_circle_outline
                )
            }
            else {
                smallPlayer.findViewById<Button>(R.id.view_small_player_btn_play).setBackgroundResource(
                    R.drawable.ic_play_circle_outline
                )
            }
        }
        else {
            smallPlayer.visibility = View.GONE
        }
    }

    private fun registerReceiver() {
        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // val otpCode = intent.getStringExtra("UI_UPDATE")
                updateSmallPlayer()
            }
        }
        registerReceiver(broadCastReceiver, IntentFilter("UI_UPDATE"))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                else {
                    Toast.makeText(this@MainActivity, "Permission to read external storage is required to load music files", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}