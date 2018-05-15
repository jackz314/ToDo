

package com.jackz314.todo;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * Adapter that exposes data from a {@link Cursor Cursor} to a {@link RecyclerView RecyclerView} widget.
 * <p>
 * The Cursor must include a column named "_id" or this class will not work.
 */
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private boolean mDataValid;
    private Cursor mCursor;
    private int mRowIDColumn;

    /**
     * Constructor.
     *
     * @param c The cursor from which to get the data.
     */

    public CursorRecyclerAdapter(final Cursor c) {
        mCursor = c;
        mDataValid = c != null;
        mRowIDColumn = mDataValid ? c.getColumnIndexOrThrow("_id") : -1;
        setHasStableIds(true);
        setHasStableIds(true);

        setHasStableIds(true);

        setHasStableIds(true);

        setHasStableIds(true);

    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("This should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, mCursor);
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param holder Existing view, returned earlier by newView
     * @param cursor The cursor from which to get the data. The cursor is already
     *               moved to the correct position.
     */
    public abstract void onBindViewHolder(VH holder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(final int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        } else {
            return RecyclerView.NO_ID;
        }
    }

    public String getItemContent(final int position, String columnName) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getString(mCursor.getColumnIndex(columnName));
        } else {
            return null;
        }
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(final Cursor cursor) {
        final Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there was a not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    private Cursor swapCursor(final Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor oldCursor = mCursor;

        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }

        // notify the observers about the new cursor or about the lack of a data set
        notifyDataSetChanged();

        return oldCursor;
    }
}
