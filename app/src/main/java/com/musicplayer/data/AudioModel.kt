package com.musicplayer.data

class AudioModel {
    private var audioId: Long
    private var albumId: Long
    private var artistId: Long
    private var title: String
    private var albumName: String
    private var artistName: String
    private var duration: Long
    private var trackNumber: String

    constructor(audioID: Long, albumId: Long, artistId: Long, title: String, albumName: String, artistName: String, duration: Long, trackNumber: String ) {
        this.audioId = audioID
        this.albumId = albumId
        this.artistId = artistId
        this.title = title
        this.albumName = albumName
        this.artistName = artistName
        this.duration = duration
        this.trackNumber = trackNumber
    }

    fun getAudioId(): Long {
        return audioId
    }

    fun setAudioId(audioID: Long) {
        this.audioId = audioID
    }

    fun getAlbumId(): Long {
        return albumId
    }

    fun setAlbumId(albumId: Long) {
        this.albumId = albumId
    }

    fun getArtistId(): Long {
        return artistId
    }

    fun setArtistId(artistId: Long) {
        this.artistId = artistId
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getAlbumName(): String {
        return albumName
    }

    fun setAlbumName(album: String) {
        this.albumName = album
    }

    fun getArtistName(): String {
        return artistName
    }

    fun setArtistName(artist: String) {
        this.artistName = artist
    }

    fun getDuration(): Long {
        return duration
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun getTrackNumber(): String {
        return trackNumber
    }

    fun setTrackNumber(trackNumber: String) {
        this.trackNumber = trackNumber
    }
}