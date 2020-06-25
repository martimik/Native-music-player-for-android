package com.musicplayer.services

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musicplayer.data.AudioModel
import java.lang.reflect.Type


class MusicService : Service(),
    OnPreparedListener,
    MediaPlayer.OnErrorListener,
    OnCompletionListener  {

    private val musicBind = MusicBinder()
    private val gson : Gson = Gson()

    private lateinit var player : MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    private var playList = mutableListOf<AudioModel>()
    private var songPos: Int = 0
    private var timePos : Int = 0
    private var startPlayback : Boolean = false

    inner class MusicBinder : Binder() {
        fun getService() : MusicService {
            return this@MusicService
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()

        sharedPreferences  = this.getSharedPreferences("playerCache", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("cachedPlaylist", null)

        val listType: Type = object : TypeToken<List<AudioModel?>?>() {}.type

        if(data != null){
            playList = gson.fromJson(data, listType)
        }

        songPos = sharedPreferences.getInt("songPos", 0)
        timePos = sharedPreferences.getInt("timePos", 0)

        initMusicPlayer()

    }

    private fun initMusicPlayer(){

        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())

        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)

        if(playList.size != 0) {
            val song = playList[songPos]
            val trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getAudioId())

            // Set media player data source
            try {
                player.setDataSource(applicationContext, trackUri)
                player.prepare()
                player.seekTo(timePos)

            } catch (e: Exception) {
                Log.e("Player", "Data source error.", e)
            }
        }
    }

    fun setSource(start : Boolean){

        player.reset()
        startPlayback = start

        if(playList.size != 0) {
            val song = playList[songPos]
            val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getAudioId()
            )

            try {
                player.setDataSource(applicationContext, trackUri)
                player.prepareAsync()

                editor = sharedPreferences.edit()
                editor.putInt("songPos", songPos)
                editor.apply()

            } catch (e: Exception) {
                Log.e("Player", "Data source error.", e)
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        if(startPlayback) mp.start()

        val intent = Intent("UI_UPDATE")
        this.sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Player: ","Start")
        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()

        editor = sharedPreferences.edit()
        editor.putInt("timePos", player.currentPosition)
        editor.apply()

        player.release()
        Log.i("Player: ","Stop")
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {

        if(songPos < (playList.size  - 1)){
            songPos += 1
            setSource(true)
        }
        else {
            songPos = 0
            setSource(false)
        }
    }

    fun setPlaylist(newPlaylist: MutableList<AudioModel>) {

        playList = newPlaylist

        val objectString = gson.toJson(newPlaylist)

        editor = sharedPreferences.edit()
        editor.putString("cachedPlaylist", objectString)
        editor.apply()
    }

    fun playlistExists() : Boolean {
        return playList.isNotEmpty()
    }

    fun setSong(newSong : Int) {
        songPos = newSong
    }

    fun getSong() : AudioModel {
        return playList[songPos]
    }

    fun getStatus() : Boolean {
        return player.isPlaying
    }

    fun getPosition() : Long {
        return player.currentPosition.toLong()
    }

    fun startPlayer() {
        player.prepareAsync()
    }

    fun seek(tPos : Int) {
        player.seekTo(tPos * 1000)
    }

    fun nextSong(){
        if(songPos < (playList.size - 1)) {
            songPos += 1
        }
        else {
            songPos = 0
        }

        setSource(player.isPlaying)
    }

    fun prevSong(){
        when {
            player.currentPosition > 10000 -> {
                player.seekTo(0)
            }
            songPos > 0 -> {
                songPos -= 1
            }
            else -> {
                songPos = (playList.size - 1)
            }
        }

        setSource(player.isPlaying)
    }

    fun pausePlay() {
        if(player.isPlaying) {
            player.pause()
        }
    }

    fun resumePlay() {
        if(!player.isPlaying) {
            player.start()
        }
    }
}
