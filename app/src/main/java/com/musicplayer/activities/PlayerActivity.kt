package com.musicplayer.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.musicplayer.R
import com.musicplayer.fragments.PlayerFragment
import com.musicplayer.services.MusicService

class PlayerActivity : FragmentActivity() {

    private lateinit var musicSrv: MusicService
    private var musicBound = false
    private val fm: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        bindService()
    }

    // Service connection
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            val binder = service as MusicService.MusicBinder
            musicSrv = binder.getService()

            musicBound = true

            startFragment()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    // Bind musicService on start
    private fun bindService() {
        bindService(
            Intent(this, MusicService::class.java), musicConnection, Context.BIND_AUTO_CREATE
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

    fun startFragment() {
        fm.beginTransaction().replace(R.id.activity_player_container, PlayerFragment(this, musicSrv, fm)).commit()
    }
}