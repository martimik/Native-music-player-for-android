package com.musicplayer.data

import java.io.Serializable

// Probably redundant set functions // TODO
class AudioModel : Serializable {

    private var audioID: Long
    private var albumID: Long
    private var title: String
    private var album: String
    private var artist: String
    private var duration: Long
    private var trackNumber: String

    constructor(
        audioID: Long,
        albumId: Long,
        name: String,
        album: String,
        artist: String,
        duration: Long,
        trackNumber: String /*, year: String, numerOftracks: String */
    ) {
        this.audioID = audioID
        this.albumID = albumId
        this.title = name
        this.album = album
        this.artist = artist
        this.duration = duration
        this.trackNumber = trackNumber
    }

    fun getAudioId(): Long {
        return audioID
    }

    fun setPath(audioID: Long) {
        this.audioID = audioID
    }

    fun getAlbumId(): Long {
        return albumID
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getAlbum(): String {
        return album
    }

    fun setAlbum(album: String) {
        this.album = album
    }

    fun getArtist(): String {
        return artist
    }

    fun setArtist(artist: String) {
        this.artist = artist
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