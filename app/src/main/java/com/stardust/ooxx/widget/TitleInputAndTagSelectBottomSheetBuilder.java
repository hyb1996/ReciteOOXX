package com.stardust.ooxx.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stardust.ooxx.R;
import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.Passage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Stardust on 2017/5/26.
 */

public class TitleInputAndTagSelectBottomSheetBuilder {


    public interface OnSaveButtonClickCallback {

        void onClick(String title, Set<Integer> selectedPositions);
    }

    private Context mContext;
    private View mView;
    private MultiChoiceRecyclerView mTagView;
    private List<String> mTags;
    private List<Integer> mSelectedTagIndices = new ArrayList<>();
    private EditText mTitle;
    private OnSaveButtonClickCallback mOnSaveButtonClickCallback;

    public TitleInputAndTagSelectBottomSheetBuilder(Context context) {
        mContext = context;
        mView = View.inflate(context, R.layout.bottom_sheet_name_input_and_tag_select, null);
        mTagView = (MultiChoiceRecyclerView) mView.findViewById(R.id.tag_list);
        mTitle = (EditText) mView.findViewById(R.id.title);
        mTags = TranslationService.getInstance().getTags();
        mView.findViewById(R.id.add_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.new_tag)
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (TextUtils.isEmpty(input))
                                    return;
                                if (TranslationService.getInstance().addTag(input.toString())) {
                                    mTagView.addChoice(input.toString(), false);
                                }
                            }
                        })
                        .show();
            }
        });
        mView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSaveButtonClickCallback == null)
                    return;
                mOnSaveButtonClickCallback.onClick(mTitle.getText().toString(), mTagView.getSelectedPositions());
            }
        });
    }

    public TitleInputAndTagSelectBottomSheetBuilder passage(Passage passage) {
        if (passage == null || passage.tag == null)
            return this;
        mTitle.setText(passage.title);
        String[] tagIndices = passage.tag.split(" ");
        for (String index : tagIndices) {
            try {
                mSelectedTagIndices.add(Integer.parseInt(index));
            } catch (NumberFormatException ignored) {

            }
        }
        return this;
    }

    public TitleInputAndTagSelectBottomSheetBuilder saveCallback(OnSaveButtonClickCallback clickCallback) {
        mOnSaveButtonClickCallback = clickCallback;
        return this;
    }

    public View build() {
        mTagView.setChoices(mTags, mSelectedTagIndices);
        return mView;
    }
}
