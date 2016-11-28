package com.jacko1972.stockhawk.touch_helper;

/**
 * Created by sam_chordas on 10/6/15.
 * credit to Paul Burke (ipaulpro)
 * Interface for enabling swiping to delete
 */
@SuppressWarnings("EmptyMethod")
public interface ItemTouchHelperViewHolder {
    void onItemSelected();

    void onItemClear();
}
