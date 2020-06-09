package com.musicplayer.data

import java.io.Serializable

// Probably redundant set functions // TODO
class AlbumModel : Serializable {

    private var albumKey: Long
    private var albumName: String
    private var artistName: String
    private var songList: List<AudioModel>

    constructor(albumKey: Long, albumName: String, artist: String, songList: List<AudioModel>){
        this.albumKey = albumKey
        this.albumName = albumName
        this.artistName = artist
        this.songList = songList
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

    fun getSongs() : List<AudioModel> {
        return songList
    }

    fun getAlbumKey(): Long {
        return albumKey
    }

    fun setAlbumKey(albumKey: Long) {
        this.albumKey = albumKey
    }
}