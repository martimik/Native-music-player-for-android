package com.musicplayer.data

class ArtistModel {
    private var artistId: Long
    private var artistName: String
    private lateinit var albums: List<String>

    constructor(artistId: Long, artistName: String) {
        this.artistId = artistId
        this.artistName = artistName
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


    fun getAlbums(): List<String> {
        return albums
    }

    fun setAlbums(albums: List<String>) {
        this.albums = albums
    }

}