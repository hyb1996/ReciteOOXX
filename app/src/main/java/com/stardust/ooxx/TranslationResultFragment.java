package com.stardust.ooxx;

import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.stardust.ooxx.data.TranslationService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_translation_result)
public class TranslationResultFragment extends Fragment {

    @ViewById(R.id.result)
    TextView mTranslationResult;

    private TranslationService mTranslationService = TranslationService.getInstance();

    private Subscription mTranslationResultSubscription;

    @AfterViews
    void setUp() {
        mTranslationResult.setMovementMethod(new ScrollingMovementMethod());
        mTranslationResult.setText(mTranslationService.getTranslation());
        mTranslationResultSubscription = mTranslationService.getTranslationPublish()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (mTranslationResult == null)
                            return;
                        mTranslationResult.setText(s);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTranslationResultSubscription.unsubscribe();
    }
}
