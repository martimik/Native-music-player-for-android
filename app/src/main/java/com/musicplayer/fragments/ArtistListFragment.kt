package com.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.musicplayer.adapters.ArtistListAdapter
import com.musicplayer.R

class ArtistListFragment : Fragment {

    private var mContext : Context
    private lateinit var v : View
    private lateinit var recyclerView : RecyclerView

    constructor(mContext : Context) : super() {
        this.mContext = mContext
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.v = inflater.inflate(R.layout.recycler_view, container, false)
        recyclerView = v.findViewById(R.id.list_recyclerview)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        val artistListAdapter = ArtistListAdapter(mContext)
        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = artistListAdapter
        return v
    }
}