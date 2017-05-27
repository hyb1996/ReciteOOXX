package com.stardust.ooxx;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.github.ybq.android.spinkit.SpinKitView;
import com.nightonke.boommenu.Util;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.widget.BubblePopupMenu;
import com.stardust.ooxx.widget.ResizeAnimation;
import com.stardust.ooxx.widget.TitleInputAndTagSelectBottomSheetBuilder;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_star_list)
public class StarListFragment extends Fragment {

    private static final String LOG_TAG = StarListFragment.class.getSimpleName();
    @ViewById(R.id.star_list)
    RecyclerView mPassageListRecyclerView;

    @ViewById(R.id.spin_kit)
    SpinKitView mSpinKitView;

    @ViewById(R.id.title_bar)
    ViewSwitcher mTitleBar;

    @ViewById(R.id.tool_bar)
    ViewSwitcher mToolbar;

    @ViewById(R.id.rename)
    View mRename;

    @ViewById(R.id.delete)
    View mDelete;

    @ViewById(R.id.search_box)
    EditText mSearchBox;

    @ViewById(R.id.title)
    TextView mTitle;

    private List<Passage> mPassages = new ArrayList<>();

    private boolean mMultiSelecting;
    private SortedSet<Integer> mSelectedPositions = new TreeSet<>(Collections.<Integer>reverseOrder());
    private BubblePopupMenu mShowingMenu;
    private int mTagIndex = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    public void setUpStarList() {
        mPassageListRecyclerView.setAdapter(new StarListAdapter());
        mPassageListRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .colorResId(R.color.star_list_divider_color)
                .size(1)
                .marginResId(R.dimen.star_list_divider_left_margin, R.dimen.star_list_divider_right_margin)
                .showLastDivider()
                .build());
        mSpinKitView.setVisibility(View.GONE);
        mPassageListRecyclerView.setItemAnimator(new SlideInLeftAnimator(new OvershootInterpolator()));
        mPassageListRecyclerView.getItemAnimator().setRemoveDuration(500);
        mPassageListRecyclerView.getItemAnimator().setAddDuration(500);
        fetchStarList();
    }

    private void fetchStarList() {
        fetchStarList(-1);
    }

    private void fetchStarList(int tagIndex) {
        showProcessBar();
        TranslationService.getInstance().getStarList(tagIndex)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Passage>>() {
                    @Override
                    public void call(List<Passage> passages) {
                        hideProcessBar();
                        mPassages.clear();
                        mPassages.addAll(passages);
                        mPassageListRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
    }


    private void searchPassages(String keywords) {
        showProcessBar();
        TranslationService.getInstance().search(keywords, mTagIndex)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Passage>>() {
                    @Override
                    public void call(List<Passage> passages) {
                        hideProcessBar();
                        mPassages.clear();
                        mPassages.addAll(passages);
                        mPassageListRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                });


    }

    private void showProcessBar() {
        if (mSpinKitView != null)
            mSpinKitView.setVisibility(View.VISIBLE);
    }

    private void hideProcessBar() {
        if (mSpinKitView != null) {
            mSpinKitView.setVisibility(View.GONE);
        }
    }


    private void multiSelect() {
        mMultiSelecting = true;
        mPassageListRecyclerView.getAdapter().notifyDataSetChanged();
        mTitleBar.showNext();
    }

    @Click(R.id.close)
    void exitMultiSelecting() {
        mMultiSelecting = false;
        mPassageListRecyclerView.getAdapter().notifyDataSetChanged();
        mTitleBar.showNext();
        mSelectedPositions.clear();
    }


    @Click(R.id.delete)
    void deleteSelectedPassages() {
        for (final int pos : mSelectedPositions) {
            TranslationService.getInstance().deletePassage(mPassages.get(pos))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<DeleteResult>() {
                        @Override
                        public void call(DeleteResult deleteResult) {

                        }
                    });
            mPassages.remove(pos);
            mPassageListRecyclerView.getAdapter().notifyItemRemoved(pos);
        }
        mPassageListRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                exitMultiSelecting();
            }
        }, 500);
    }

    @Click(R.id.rename)
    void rename() {
        if (mSelectedPositions.size() != 1) {
            return;
        }
        final Passage passage = mPassages.get(mSelectedPositions.first());
        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(new TitleInputAndTagSelectBottomSheetBuilder(getActivity())
                .passage(passage)
                .saveCallback(new TitleInputAndTagSelectBottomSheetBuilder.OnSaveButtonClickCallback() {
                    @Override
                    public void onClick(String title, Set<Integer> selectedPositions) {
                        updatePassage(passage, title, TextUtils.join(" ", selectedPositions));
                        dialog.dismiss();
                    }
                })
                .build());
        dialog.show();
        exitMultiSelecting();
    }

    @Click(R.id.select_all)
    void selectOrUnSelectAll() {
        if (mSelectedPositions.size() == mPassages.size()) {
            mSelectedPositions.clear();
        } else {
            mSelectedPositions.clear();
            for (int i = 0; i < mPassages.size(); i++) {
                mSelectedPositions.add(i);
            }
        }
        mPassageListRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void updatePassage(Passage passage, String name, String tag) {
        passage.title = name;
        passage.tag = tag;
        TranslationService.getInstance().updatePassage(passage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PutResult>() {
                    @Override
                    public void call(PutResult result) {
                    }
                });
    }

    @Click(R.id.search)
    void showSearchBox() {
        mToolbar.showNext();
        mSearchBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchBox.requestFocus();
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);
    }

    @Click(R.id.cancel_search)
    void cancelSearch() {
        mToolbar.showPrevious();
        fetchStarList();
    }

    @EditorAction(R.id.search_box)
    @Click(R.id.done)
    void search() {
        CharSequence input = mSearchBox.getText();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        searchPassages(input.toString());
    }

    @Subscribe
    public void onPassageInserted(TranslationService.PassageInsertedEvent event) {
        mPassages.add(event.passage);
        mPassageListRecyclerView.getAdapter().notifyItemInserted(mPassages.size() - 1);
    }

    @Click(R.id.title_star)
    void showTagListMenu() {
        if (mShowingMenu != null) {
            mShowingMenu.dismiss();
            return;
        }
        final List<String> options = new ArrayList<>();
        options.add(getString(R.string.fav));
        options.addAll(TranslationService.getInstance().getTags());
        mShowingMenu = new BubblePopupMenu(getActivity(), options);
        mShowingMenu.setWidth(Util.dp2px(150));
        mShowingMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mShowingMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mShowingMenu = null;
            }
        });
        mShowingMenu.setOnItemClickListener(new BubblePopupMenu.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                mTagIndex = position - 1;
                fetchStarList(mTagIndex);
                mShowingMenu.dismiss();
                mTitle.setText(options.get(position));
            }
        });
        mShowingMenu.showAsDropDown(mTitle, mTitle.getWidth() / 2 - mShowingMenu.getWidth() / 2, Util.dp2px(-8), Gravity.CENTER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private class PassageViewHolder extends RecyclerView.ViewHolder {

        TextView title, summary, datetime;
        CheckBox checkBox;
        View checkBoxContainer;

        PassageViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            summary = (TextView) itemView.findViewById(R.id.summary);
            datetime = (TextView) itemView.findViewById(R.id.datetime);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            checkBoxContainer = itemView.findViewById(R.id.checkbox_container);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMultiSelecting) {
                        checkBox.toggle();
                        return;
                    }
                    int pos = mPassageListRecyclerView.getChildAdapterPosition(itemView);
                    Passage passage = mPassages.get(pos);
                    TranslationService.getInstance().setCurrentPassage(passage);
                    EventBus.getDefault().post(passage);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!mMultiSelecting) {
                        int pos = mPassageListRecyclerView.getChildAdapterPosition(itemView);
                        mSelectedPositions.add(pos);
                        multiSelect();
                        return true;
                    }
                    return false;
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int pos = mPassageListRecyclerView.getChildAdapterPosition(itemView);
                    if (isChecked) {
                        mSelectedPositions.add(pos);
                    } else {
                        mSelectedPositions.remove(pos);
                    }
                    mRename.setEnabled(mSelectedPositions.size() == 1);
                    mDelete.setEnabled(!mSelectedPositions.isEmpty());
                }
            });
        }
    }

    private class StarListAdapter extends RecyclerView.Adapter<PassageViewHolder> {

        @Override
        public PassageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PassageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_star_list_passage, parent, false));
        }

        @Override
        public void onBindViewHolder(PassageViewHolder holder, int position) {
            Passage passage = mPassages.get(position);
            holder.summary.setText(passage.summary);
            holder.title.setText(passage.title);
            holder.datetime.setText(passage.time);
            ResizeAnimation animation = new ResizeAnimation(holder.checkBoxContainer, mMultiSelecting ? Util.dp2px(36) : 0);
            animation.setDuration(300);
            holder.checkBoxContainer.startAnimation(animation);
            holder.checkBox.setChecked(mSelectedPositions.contains(position));
        }

        @Override
        public int getItemCount() {
            return mPassages.size();
        }
    }


}
