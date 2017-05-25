package com.stardust.ooxx;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.Passage;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_star_list)
public class StarListFragment extends Fragment {

    private static final String LOG_TAG = StarListFragment.class.getSimpleName();
    @ViewById(R.id.star_list)
    RecyclerView mStarList;

    @ViewById(R.id.spin_kit)
    SpinKitView mSpinKitView;

    private List<Passage> mPassages = Collections.emptyList();

    @AfterViews
    public void setUpStarList() {
        mStarList.setAdapter(new StarListAdapter());
        mStarList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .colorResId(R.color.star_list_divider_color)
                .size(1)
                .marginResId(R.dimen.star_list_divider_left_margin, R.dimen.star_list_divider_right_margin)
                .showLastDivider()
                .build());
        mSpinKitView.setVisibility(View.GONE);
        fetchStarList();
    }

    private void fetchStarList() {
        TranslationService.getInstance().getStarList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Passage>>() {
                    @Override
                    public void call(List<Passage> passages) {
                        hideProcessBar();
                        mPassages = passages;
                        mStarList.getAdapter().notifyDataSetChanged();
                    }
                });
    }

    private void hideProcessBar() {
        mSpinKitView.setVisibility(View.GONE);
    }


    private class PassageViewHolder extends RecyclerView.ViewHolder {

        TextView title, summary, datetime;

        PassageViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            summary = (TextView) itemView.findViewById(R.id.summary);
            datetime = (TextView) itemView.findViewById(R.id.datetime);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = mStarList.getChildAdapterPosition(itemView);
                    Passage passage = mPassages.get(pos);
                    TranslationService.getInstance().setCurrentPassage(passage);
                    EventBus.getDefault().post(passage);
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
        }

        @Override
        public int getItemCount() {
            return mPassages.size();
        }
    }


}
