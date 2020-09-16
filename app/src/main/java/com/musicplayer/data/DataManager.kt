package com.musicplayer.data

import android.content.Context
import android.provider.MediaStore

class DataManager {

    companion object {
        private lateinit var instance: DataManager

        fun returnInstance(): DataManager {
            return instance
        }
    }

    private var audioList = mutableListOf<AudioModel>()
    private var albumList = mutableListOf<AlbumModel>()
    private var artistList = mapOf<String, List<AlbumModel>>()

    fun prepare(context: Context) {

        instance = this

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // Projection of audioModel
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.MediaColumns.DURATION,
            MediaStore.Audio.AudioColumns.TRACK
        )

        // Query files from external memory
        val cursor = context.contentResolver.query(
            uri,
            projection,
            MediaStore.Audio.Media.IS_MUSIC + "!= 0",
            null,
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {

                val audioID = cursor.getLong(0)
                val albumID = cursor.getLong(1)
                val name = cursor.getString(2)
                val album = cursor.getString(3)
                val artist = cursor.getString(4)
                val duration = cursor.getLong(5)
                val trackNumber = cursor.getString(6) ?: ""

                val audioModel = AudioModel(
                    audioID,
                    albumID,
                    name,
                    album,
                    artist,
                    duration,
                    trackNumber
                )
                // Log.i("key", albumID.toString())

                audioList.add(audioModel)
            }
            cursor.close()
        }

        groupByAlbum()
        groupByArtist()
    }

    private fun groupByAlbum() {
        audioList.groupBy { it.getAlbum() }.forEach {
            it.value.sortedBy { track -> track.getTrackNumber() }
            albumList.add(
                AlbumModel(
                    it.value[0].getAlbumId(),
                    it.key,
                    it.value[0].getArtist(),
                    it.value
                )
            )
        }
    }

    private fun groupByArtist() {
        artistList = albumList.groupBy { it.getArtist() }
    }

    fun getAllSongs(): MutableList<AudioModel> {
        audioList.sortBy { it.getTitle() }
        return audioList
    }

    fun getSong(position: Int): AudioModel {
        return audioList[position]
    }

    fun getSongIndex(song: AudioModel): Int {
        return audioList.indexOf(song)
    }

    fun getAlbums(): MutableList<AlbumModel> {
        return albumList
    }

    fun getAlbum(position: Int): AlbumModel {
        return albumList[position]
    }

    fun getAlbumIndex(album: AlbumModel): Int {
        return albumList.indexOf(album)
    }

    fun getArtists(): Map<String, List<AlbumModel>> {
        return artistList
    }

    fun getArtist(artist: String): List<AlbumModel>? {
        return artistList[artist]
    }

    // TODO Implement search function
    fun search() {

    }
}