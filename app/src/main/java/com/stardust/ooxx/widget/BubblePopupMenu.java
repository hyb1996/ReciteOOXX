package com.stardust.ooxx.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.stardust.ooxx.R;

import java.util.List;

/**
 * Created by Stardust on 2017/5/23.
 */

public class BubblePopupMenu extends PopupWindow {


    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    private RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;

    public BubblePopupMenu(Context context, List<String> options) {
        super(context);
        View view = View.inflate(context, R.layout.bubble_popup_menu, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setAdapter(new SimpleRecyclerViewAdapter<>(R.layout.bubble_popup_menu_item, options, new SimpleRecyclerViewAdapter.ViewHolderFactory<MenuItemViewHolder>() {
            @Override
            public MenuItemViewHolder create(View itemView) {
                return new MenuItemViewHolder(itemView);
            }
        }));
        setContentView(view);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(true);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private class MenuItemViewHolder extends BindableViewHolder<String> {

        private TextView mOption;

        public MenuItemViewHolder(View itemView) {
            super(itemView);
            mOption = (TextView) itemView.findViewById(R.id.option);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        int i = mRecyclerView.getChildAdapterPosition(v);
                        mOnItemClickListener.onClick(v, i);
                    }
                }
            });
        }

        @Override
        public void bind(String s, int position) {
            mOption.setText(s);
        }
    }

}
