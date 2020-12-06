package com.musicplayer.data

class ArtistModel {
    private var artistId: Long
    private var artistName: String
    private var albumArtId: Long

    constructor(artistId: Long, artistName: String, albumArtId: Long) {
        this.artistId = artistId
        this.artistName = artistName
        this.albumArtId = albumArtId
    }

    fun getArtistId(): Long {
        return artistId
    }

    fun setArtistId(artistId: Long) {
        this.artistId = artistId
    }

    fun setArtistName(artistName: String) {
        this.artistName = artistName
    }

    fun getArtistName(): String {
        return artistName
    }

    fun setAlbumArtId(albumArtId: Long) {
        this.albumArtId = albumArtId
    }

    fun getAlbumArtId(): Long {
        return albumArtId
    }

}