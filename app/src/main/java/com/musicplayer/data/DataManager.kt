package com.musicplayer.data

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log

class DataManager {

    fun getAudio(context: Context): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return audioQuery(query)
    }

    fun getAudio(context: Context, audioId: Long): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media._ID + "= " + audioId.toString(), null, null
        )
        return audioQuery(query)
    }

    fun getAudio(context: Context, audioIds: List<Long>): List<AudioModel> {
        var audioList = mutableListOf<AudioModel>()
        audioIds.forEach {
            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Albums.ALBUM_ID + "= " + it.toString(), null, null
            )
            audioList.plus(audioQuery(query))
        }
        return audioList
    }

    fun getAlbum(context: Context, albumId : Long): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Albums.ALBUM_ID + "= " + albumId.toString(), null, null
        )

        return audioQuery(query)
    }

    fun getAlbums(context: Context): List<AlbumModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return groupByAlbum(audioQuery(query))
    }

    fun getAlbums(context: Context, artistId: Long): List<AlbumModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.AlbumColumns.ARTIST_ID + "=" + artistId.toString(), null, null
        )

        return groupByAlbum(audioQuery(query))
    }

    fun getArtists(context: Context): List<ArtistModel> {

        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, artistProjection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return artistQuery(query).distinctBy{ it.getArtistId() }
    }

    private fun groupByAlbum(audioList: List<AudioModel>): MutableList<AlbumModel> {
        var albumList = mutableListOf<AlbumModel>()
        audioList.groupBy { it.getAlbumId() }.forEach {
            it.value.sortedBy { track -> track.getTrackNumber() }
            albumList.add(
                AlbumModel(
                    it.key, it.value[0].getAlbumName(), it.value[0].getArtistName(), it.value
                )
            )
        }
        return albumList
    }

    private fun audioQuery(cursor: Cursor?): List<AudioModel> {
        var audioList: MutableList<AudioModel> = mutableListOf();
        // Query files from external memory
        if (cursor != null) {
            while (cursor.moveToNext()) {

                val audioID = cursor.getLong(0)
                val albumID = cursor.getLong(1)
                val artistId = cursor.getLong(2)
                val title = cursor.getString(3)
                val albumName = cursor.getString(4)
                val artistName = cursor.getString(5)
                val duration = cursor.getLong(6)
                val trackNumber = cursor.getInt(6)

                val audioModel = AudioModel(
                    audioID, albumID, artistId, title, albumName, artistName, duration, trackNumber
                )

                audioList.add(audioModel)
            }
            cursor.close()
        }
        return audioList
    }

    // Projection of audioModel
    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Albums.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.MediaColumns.DURATION,
        MediaStore.Audio.AudioColumns.TRACK
    )

private fun artistQuery(cursor: Cursor?): List<ArtistModel> {
    var artistList: MutableList<ArtistModel> = mutableListOf();
    // Query files from external memory
    if(cursor != null) {
        while (cursor.moveToNext()) {
            val artistId = cursor.getLong(0)
            val artistName = cursor.getString(1)
            val albumId = cursor.getLong(2)

            val artistModel = ArtistModel(artistId, artistName, albumId)
            artistList.add(artistModel)
        }
        cursor.close()
    }

    return artistList
}

    private val artistProjection = arrayOf(
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.Albums.ALBUM_ID
    )

    // TODO Implement search function
    fun search() {

    }
}