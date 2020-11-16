package com.musicplayer.data

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

class DataManager {

    fun getAudio(context: Context): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return audioQuery(query)
    }

    fun getAudio(context: Context, audioId : String): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 OR " + MediaStore.Audio.Media._ID + "= " + audioId, null, null
        )
        return audioQuery(query)
    }

    fun getAlbum(context: Context, albumId : String): List<AudioModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 OR " + MediaStore.Audio.Albums.ALBUM_ID + "= " + albumId, null, null
        )

        return audioQuery(query).sortedBy { it.getTrackNumber() }
    }

    fun getAlbums(context: Context): List<AlbumModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return groupByAlbum(audioQuery(query))
    }

    fun getAlbums(context: Context, artistId: String): List<AlbumModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND" + MediaStore.Audio.AlbumColumns.ARTIST_ID + "=" + artistId, null, null
        )

        return groupByAlbum(audioQuery(query))
    }

    fun getArtists(context: Context): List<ArtistModel> {
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, artistProjection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null
        )

        return artistQuery(query)
    }

    private fun groupByAlbum(audioList: List<AudioModel>): MutableList<AlbumModel> {
        var albumList = mutableListOf<AlbumModel>()
        audioList.groupBy { it.getAlbumName() }.forEach {
            it.value.sortedBy { track -> track.getTrackNumber() }
            albumList.add(
                AlbumModel(
                    it.value[0].getAlbumId(), it.key, it.value[0].getArtistName(), it.value
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
                val trackNumber = cursor.getString(6) ?: ""

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

            val artistModel = ArtistModel(artistId, artistName)
            artistList.add(artistModel)
        }
        cursor.close()
    }

    return artistList.distinctBy { it.getArtistId() }
}

    private val artistProjection = arrayOf(
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST
    )

    // TODO Implement search function
    fun search() {

    }
}