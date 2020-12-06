package com.musicplayer.data

class AlbumModel {

    private var albumId: Long
    private var albumName: String
    private var artistName: String
    private var tracks: List<AudioModel>

    constructor(albumId: Long, albumName: String, artist: String, songList: List<AudioModel>) {
        this.albumId = albumId
        this.albumName = albumName
        this.artistName = artist
        this.tracks = songList
    }

    fun getAlbumName(): String {
        return albumName
    }

    fun setAlbumName(albumName: String) {
        this.albumName = albumName
    }

    fun getArtist(): String {
        return artistName
    }

    fun setArtistName(aArtist: String) {
        this.artistName = aArtist
    }

    fun getAlbumId(): Long {
        return albumId
    }

    fun setAlbumKey(albumKey: Long) {
        this.albumId = albumKey
    }

    fun setSongList(songList: List<AudioModel>) {
        this.tracks = songList
    }

    fun getSongList(): List<AudioModel> {
        return tracks
    }
}