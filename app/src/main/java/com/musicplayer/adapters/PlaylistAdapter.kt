package com.musicplayer.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.data.AudioModel
import com.musicplayer.services.MusicService
import com.musicplayer.utility.ItemMoveCallback
import com.musicplayer.utility.StartDragListener
import java.util.*
import java.util.concurrent.TimeUnit


class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>,
    ItemMoveCallback.ItemTouchHelperContract {

    var mContext: Context
    private var mData = mutableListOf<AudioModel>()
    private var currentSongPos: Int = 0
    private val musicSrv: MusicService?
    private val mStartDragListener: StartDragListener

    constructor(
        mContext: Context,
        musicService: MusicService?,
        startDragListener: StartDragListener
    ) : super() {
        this.mContext = mContext
        this.musicSrv = musicService
        this.mStartDragListener = startDragListener
        this.mData = musicSrv!!.getPlaylist()
        this.currentSongPos = musicService!!.getCurrentSongPos()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_playlist, parent, false)
        return SongViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val duration = mData[position].getDuration()

        val artistNameAndDuration = String.format(
            mData[position].getArtist() + " - %02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )

        holder.title.text = mData[position].getTitle()
        holder.artist.text = artistNameAndDuration

        if (holder.adapterPosition == musicSrv?.getCurrentSongPos()) {
            holder.removeBtn.setImageResource(R.drawable.ic_play_arrow)
            holder.songListItem.setOnClickListener(null)
        } else {
            holder.removeBtn.setImageResource(R.drawable.ic_clear)
            holder.removeBtn.setOnClickListener {
                musicSrv?.removeFromPlaylist(holder.adapterPosition)
                mData.removeAt(holder.adapterPosition)
                notifyDataSetChanged()
            }

            holder.songListItem.setOnClickListener {
                musicSrv?.selectTrack(holder.adapterPosition)
                notifyDataSetChanged()
            }
        }

        holder.dragBtn.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action ==
                MotionEvent.ACTION_DOWN
            ) {
                mStartDragListener.requestDrag(holder)
            }
            false
        })

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, mData[position].getAlbumId())

        holder.albumCover.setImageURI(uri)

        if (holder.albumCover.drawable == null) {
            holder.albumCover.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    R.drawable.cover_placeholder
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class SongViewHolder : RecyclerView.ViewHolder {

        var songListItem: ConstraintLayout
        var albumCover: ImageView
        var title: TextView
        var artist: TextView
        var dragBtn: ImageButton
        var removeBtn: ImageButton

        constructor(itemView: View) : super(itemView) {
            super.itemView

            songListItem = itemView.findViewById(R.id.row_playlist_container)
            albumCover = itemView.findViewById(R.id.row_playlist_iv_cover)
            title = itemView.findViewById(R.id.row_playlist_tv_title)
            artist = itemView.findViewById(R.id.row_playlist_tv_artist)
            dragBtn = itemView.findViewById(R.id.row_playlist_btn_drag)
            removeBtn = itemView.findViewById(R.id.row_playlist_btn_clear)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        when {
            fromPosition < toPosition -> {
                for (i in fromPosition until toPosition) {
                    Collections.swap(mData, i, i + 1)
                }
            }
            toPosition == 0 -> {
                for (i in fromPosition downTo 1) {
                    Collections.swap(mData, i, i - 1)
                }
            }
            else -> {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(mData, i, i - 1)
                }
            }
        }

        musicSrv?.reorderPlaylist(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: SongViewHolder?) {
        myViewHolder?.itemView?.setBackgroundColor(
            ContextCompat.getColor(
                mContext,
                R.color.colorPrimaryDark
            )
        )
    }

    override fun onRowClear(myViewHolder: SongViewHolder?) {
        myViewHolder?.itemView?.setBackgroundColor(0x00000000)
    }
}