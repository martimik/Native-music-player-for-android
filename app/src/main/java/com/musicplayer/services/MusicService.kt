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
import java.util.*


class MusicService : Service(), OnPreparedListener, MediaPlayer.OnErrorListener, OnCompletionListener {

    private val musicBind = MusicBinder()
    private val gson: Gson = Gson()

    private lateinit var player: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var playList = mutableListOf<AudioModel>()
    private var playOrder = mutableListOf<Int>()
    private var songPos: Int = 0
    private var timePos: Int = 0
    private var isShuffled: Boolean = false
    private var startPlayback: Boolean = false

    inner class MusicBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()

        sharedPreferences = this.getSharedPreferences("playerCache", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("cachedPlaylist", null)

        val listType: Type = object : TypeToken<List<AudioModel?>?>() {}.type

        if (data != null) {
            playList = gson.fromJson(data, listType)
        }

        songPos = sharedPreferences.getInt("songPos", 0)
        timePos = sharedPreferences.getInt("timePos", 0)
        isShuffled = sharedPreferences.getBoolean("isShuffled", false)

        setOrder()
        initMusicPlayer()

    }

    private fun initMusicPlayer() {

        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()
        )

        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)

        if (playList.size != 0) {

            val song = playList[playOrder[songPos]]

            val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getAudioId()
            )

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

    private fun setSource(start: Boolean) {

        player.reset()
        startPlayback = start

        if (playList.size != 0) {
            val song = playList[playOrder[songPos]]
            val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getAudioId()
            )

            try {
                player.setDataSource(applicationContext, trackUri)
                player.prepareAsync()

                editor = sharedPreferences.edit()
                editor.putInt("songPos", playOrder[songPos])
                editor.apply()

            } catch (e: Exception) {
                Log.e("Player", "Data source error.", e)
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (startPlayback) mp.start()

        val intent = Intent("UI_UPDATE")
        this.sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Player: ", "Start")
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
        Log.i("Player: ", "Stop")
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {

        if (songPos < (playList.size - 1)) {
            songPos += 1
            setSource(true)
        }
        else {
            songPos = 0
            setSource(false)
        }
    }

    fun playlistExists(): Boolean {
        return playList.isNotEmpty()
    }

    fun setPlaylist(newPlaylist: MutableList<AudioModel>) {

        playList = newPlaylist

        setOrder()
        savePlaylist()
    }

    fun getPlaylist(): MutableList<AudioModel> {

        val temp = mutableListOf<AudioModel>()

        for (i in 0 until playOrder.size) {
            val pos = playOrder[i]
            temp.add(playList[pos])
        }

        return temp
    }

    fun addToPlaylist(songs: MutableList<AudioModel>) {
        playList = (playList + songs) as MutableList<AudioModel>
        playOrder.add(playList.size - 1)

        savePlaylist()
    }

    fun removeFromPlaylist(pos: Int) {
        playList.removeAt(playOrder[pos])
        playOrder.remove(pos)

        for (i in 0 until playList.size) {
            if (playOrder[i] > pos) playOrder[i] -= 1
        }

        savePlaylist()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun isLooping(): Boolean {
        return player.isLooping
    }

    fun isShuffled(): Boolean {
        return isShuffled
    }

    fun setSong(newSong: Int) {
        songPos = newSong
        setSource(true)
    }

    fun selectTrack(adapterPos: Int) {
        songPos = adapterPos
        setSource(player.isPlaying)
    }

    fun getCurrentTrack(): AudioModel {
        return playList[playOrder[songPos]]
    }

    fun getCurrentSongPos(): Int {
        return songPos
    }

    fun getPosition(): Long {
        return player.currentPosition.toLong()
    }

    fun startPlayer() {
        player.prepareAsync()
    }

    fun pausePlay() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun resumePlay() {
        if (!player.isPlaying) {
            player.start()
        }
    }

    fun nextTrack() {
        if (songPos < (playList.size - 1)) {
            songPos += 1
        }
        else {
            songPos = 0
        }

        setSource(player.isPlaying)
    }

    fun previousTrack() {
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

    fun seek(tPos: Int) {
        player.seekTo(tPos * 1000)
    }

    fun toggleLoop() {
        player.isLooping = !player.isLooping
    }

    fun toggleShuffle() {

        isShuffled = !isShuffled

        if (!isShuffled) songPos = playOrder[songPos]
        setOrder()

        editor = sharedPreferences.edit()
        editor.putBoolean("isShuffled", isShuffled)
        editor.apply()

    }

    private fun setOrder() {
        val size = playList.size - 1
        playOrder = if (isShuffled) {
            val temp = (0..size).toMutableList()
            temp.removeAt(songPos)
            temp.shuffle()
            temp.add(temp[0])
            temp[0] = songPos
            songPos = 0
            temp
        }
        else {
            (0..size).toMutableList()
        }
    }

    fun reorderPlaylist(from: Int, to: Int) {

        val curSong = playList[playOrder[songPos]]

        when {
            from < to -> {
                for (i in from until to) {
                    Collections.swap(playOrder, i, i + 1)
                }
            }
            to == 0 -> {
                for (i in from downTo 1) {
                    Collections.swap(playOrder, i, i - 1)
                }
            }
            else -> {
                for (i in from downTo to + 1) {
                    Collections.swap(playOrder, i, i - 1)
                }
            }
        }

        songPos = playOrder.indexOf(playList.indexOf(curSong))

        val intent = Intent("UI_UPDATE")
        this.sendBroadcast(intent)
    }

    private fun savePlaylist() {
        val objectString = gson.toJson(playList)

        editor = sharedPreferences.edit()
        editor.putString("cachedPlaylist", objectString)
        editor.apply()
    }
}