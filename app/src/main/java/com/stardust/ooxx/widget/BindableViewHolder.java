package com.stardust.ooxx.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Stardust on 2017/5/24.
 */

public abstract class BindableViewHolder<M> extends RecyclerView.ViewHolder {
    public BindableViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(M m, int position);
}
