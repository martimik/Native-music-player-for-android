package com.musicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.activities.AlbumDetailActivity
import com.musicplayer.data.AlbumModel
import com.musicplayer.data.DataManager

class ArtistDetailAdapter : RecyclerView.Adapter<ArtistDetailAdapter.AlbumViewHolder> {

    private var mContext: Context
    private var mData: List<AlbumModel>

    constructor(mContext: Context, albumList: List<AlbumModel>) : super() {
        this.mContext = mContext
        this.mData = albumList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_album_list, parent, false)
        val viewHolder = AlbumViewHolder(view)

        viewHolder.albumListItem.setOnClickListener {

            val intent = Intent(mContext, AlbumDetailActivity::class.java).apply {
                putExtra(
                    "albumPosition",
                    DataManager.returnInstance().getAlbumIndex(mData[viewHolder.adapterPosition])
                )
            }

            mContext.startActivity(intent)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {

        holder.albumName.text = mData[position].getAlbumName()
        holder.artistName.text = mData[position].getArtist()

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, mData[position].getAlbumKey())

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


    class AlbumViewHolder : RecyclerView.ViewHolder {

        var albumListItem: LinearLayout
        var albumCover: ImageView
        var albumName: TextView
        var artistName: TextView

        constructor(itemView: View) : super(itemView) {
            super.itemView

            albumListItem = itemView.findViewById(R.id.item_album_list)
            albumCover = itemView.findViewById(R.id.item_album_list_iv_cover)
            albumName = itemView.findViewById(R.id.item_album_list_tv_album)
            artistName = itemView.findViewById(R.id.item_album_list_tv_artist)
        }
    }
}