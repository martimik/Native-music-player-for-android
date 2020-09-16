package com.musicplayer.utility

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.adapters.PlaylistAdapter

class ItemMoveCallback : ItemTouchHelper.Callback {

    private val mAdapter: ItemTouchHelperContract

    constructor(adapter: ItemTouchHelperContract) : super() {
        this.mAdapter = adapter
    }

    override fun isLongPressDragEnabled(): Boolean {
        //return super.isLongPressDragEnabled()
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("Not yet implemented")
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

        if (viewHolder is PlaylistAdapter.SongViewHolder) {
            val myViewHolder: PlaylistAdapter.SongViewHolder = viewHolder as PlaylistAdapter.SongViewHolder
            mAdapter.onRowSelected(myViewHolder)
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is PlaylistAdapter.SongViewHolder) {
            val myViewHolder: PlaylistAdapter.SongViewHolder? = viewHolder as PlaylistAdapter.SongViewHolder?
            mAdapter.onRowClear(myViewHolder)
        }
    }

    interface ItemTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: PlaylistAdapter.SongViewHolder?)
        fun onRowClear(myViewHolder: PlaylistAdapter.SongViewHolder?)
    }
}

public interface StartDragListener {
    fun requestDrag(viewHolder: RecyclerView.ViewHolder)
}