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
import com.musicplayer.activities.ArtistDetailActivity
import com.musicplayer.data.AlbumModel
import com.musicplayer.data.ArtistModel
import com.musicplayer.data.DataManager

class ArtistListAdapter(private var mContext: Context) : RecyclerView.Adapter<ArtistListAdapter.ArtistViewHolder>() {

    private var mData: List<ArtistModel> = DataManager().getArtists(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_artist_list, parent, false)

        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {

        holder.artistName.text = mData[holder.adapterPosition].getArtistName()

        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, mData[holder.adapterPosition].getAlbumArtId())
        holder.artistCover.setImageURI(uri)

        if (holder.artistCover.drawable == null) {
            holder.artistCover.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext, R.drawable.artist_placeholder
                )
            )
        }

        holder.artistListItem.setOnClickListener {
            val intent = Intent(mContext, ArtistDetailActivity::class.java).apply {
                putExtra("artist_id", mData[holder.adapterPosition].getArtistId())
            }
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ArtistViewHolder : RecyclerView.ViewHolder {
        var artistListItem: LinearLayout
        var artistCover: ImageView
        var artistName: TextView

        constructor(itemView: View) : super(itemView) {
            super.itemView

            artistListItem = itemView.findViewById(R.id.row_artist_list)
            artistCover = itemView.findViewById(R.id.row_artist_list_iv_cover)
            artistName = itemView.findViewById(R.id.row_artist_list_tv_artist)
        }
    }
}