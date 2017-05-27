package com.stardust.ooxx;

import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.widget.SimpleTextWatcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_edit)
public class EditFragment extends Fragment {

    @ViewById(R.id.edit)
    EditText mSourceText;
    private Subscription mSourceTextSubscription;

    private TextWatcher mTextWatcher = new SimpleTextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            TranslationService.getInstance().setSourceTextWithoutPublish(s.toString());
        }
    };

    @AfterViews
    void setUp() {
        mSourceTextSubscription = TranslationService.getInstance().getSourceTextPublish()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (mSourceText == null) {
                            return;
                        }
                        mSourceText.setText(s);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSourceText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSourceText.removeTextChangedListener(mTextWatcher);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSourceTextSubscription != null)
            mSourceTextSubscription.unsubscribe();
    }
}
