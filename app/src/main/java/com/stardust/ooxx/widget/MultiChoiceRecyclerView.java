package com.stardust.ooxx.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.stardust.ooxx.R;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Stardust on 2017/5/26.
 */

public class MultiChoiceRecyclerView extends RecyclerView {

    private Set<Integer> mSelectedPositions = new TreeSet<>();
    private SimpleRecyclerViewAdapter<String, ViewHolder> mAdapter;

    public MultiChoiceRecyclerView(Context context) {
        super(context);
        init();
    }

    public MultiChoiceRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiChoiceRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setChoices(List<String> choices, List<Integer> selectedPositions) {
        mSelectedPositions.clear();
        mSelectedPositions.addAll(selectedPositions);
        mAdapter = new SimpleRecyclerViewAdapter<>(R.layout.item_single_choice, choices, new SimpleRecyclerViewAdapter.ViewHolderFactory<ViewHolder>() {
            @Override
            public ViewHolder create(View itemView) {
                return new ViewHolder(itemView);
            }
        });
        setAdapter(mAdapter);
    }

    public void addChoice(String item, boolean checked) {
        if (checked) {
            mSelectedPositions.add(mAdapter.getItemCount());
        }
        mAdapter.add(item);
    }

    public Set<Integer> getSelectedPositions() {
        return mSelectedPositions;
    }

    private class ViewHolder extends BindableViewHolder<String> {

        CheckBox mCheckBox;
        TextView mTextView;

        public ViewHolder(final View itemView) {
            super(itemView);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            mTextView = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox.toggle();
                }
            });
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int pos = getChildAdapterPosition(itemView);
                    if (isChecked)
                        mSelectedPositions.add(pos);
                    else
                        mSelectedPositions.remove(pos);
                }
            });
        }

        @Override
        public void bind(String s, int position) {
            mCheckBox.setChecked(mSelectedPositions.contains(position));
            mTextView.setText(s);
        }

    }


}
