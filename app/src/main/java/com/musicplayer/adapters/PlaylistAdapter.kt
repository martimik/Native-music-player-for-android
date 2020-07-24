package com.musicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.activities.MainActivity
import com.musicplayer.activities.PlayerActivity
import com.musicplayer.data.AudioModel
import com.musicplayer.services.MusicService
import java.util.concurrent.TimeUnit

class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder> {

    var mContext : Context
    private val musicSrv : MusicService?
    var mData = mutableListOf<AudioModel>()

    constructor(mContext: Context, musicService: MusicService?) : super() {
        this.mContext = mContext
        this.musicSrv = musicService
        this.mData = musicSrv!!.getPlaylist()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_playlist, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val duration = mData[position].getDuration()

        val artistNameAndDuration = String.format(mData[position].getArtist() + " - %02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(duration)))

        holder.title.text= mData[position].getTitle()
        holder.artist.text = artistNameAndDuration

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, mData[position].getAlbumId())

        holder.albumCover.setImageURI(uri)

        if(holder.albumCover.drawable == null) {
            holder.albumCover.setImageDrawable(
                ContextCompat.getDrawable(mContext,
                R.drawable.cover_placeholder
            ))
        }

        holder.songListItem.setOnClickListener {

            if (mContext is MainActivity) {
                (mContext as MainActivity).setPlaylist(mData)
                (mContext as MainActivity).setSong(holder.adapterPosition)
            }

            val intent = Intent(mContext, PlayerActivity::class.java)
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class SongViewHolder : RecyclerView.ViewHolder {

        var songListItem : ConstraintLayout
        var albumCover : ImageView
        var title: TextView
        var artist: TextView

        constructor(itemView : View) : super(itemView){
            super.itemView

            songListItem = itemView.findViewById(R.id.row_playlist_container)
            albumCover = itemView.findViewById(R.id.row_playlist_iv_cover)
            title = itemView.findViewById(R.id.row_playlist_tv_title)
            artist = itemView.findViewById(R.id.row_playlist_tv_artist)
        }
    }
}