package com.stardust.ooxx;

import android.app.Fragment;
import android.graphics.Rect;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Util;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.stardust.app.FragmentPagerAdapterBuilder;
import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.widget.BubblePopupMenu;
import com.stardust.ooxx.widget.TitleInputAndTagSelectBottomSheetBuilder;
import com.stardust.ooxx.widget.UnderlineLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_translation)
public class TranslationFragment extends Fragment {

    private static final List<String> OOXX_OPTIONS = Arrays.asList("XXXX", "OXOX", "OOXX", "OOOX", "OOOO");
    private static final String LOG_TAG = TranslationFragment.class.getSimpleName();

    @ViewById(R.id.view_pager)
    ViewPager mViewPager;

    @ViewById(R.id.translate_from_underline)
    UnderlineLayout mTranslationFromUnderline;

    @ViewById(R.id.translate_to_underline)
    UnderlineLayout mTranslationToUnderline;

    @ViewById(R.id.bmb)
    BoomMenuButton mBoomMenuButton;

    @ViewById(R.id.text_translate_to)
    TextView mOOXX;

    @ViewById(R.id.text_translate_from)
    TextView mSourceFormat;

    private final TranslationService mTranslationService = TranslationService.getInstance();
    private BubblePopupMenu mShowingMenu;
    private List<String> mFormatOptions = mTranslationService.getFormatOptions();

    @AfterViews
    void setUpUI() {
        setUpViewPager();
        setUpBoomMenu();
        setOOXXInterval(mTranslationService.getOOXXInterval());
        setSourceFormat(mTranslationService.getSourceFormat());
    }

    private void setUpBoomMenu() {
        final TextInsideCircleButton.Builder firstButton = buildMenuButton(R.drawable.save, R.color.colorMenuFav, R.string.save);
        firstButton.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                save();
            }
        });
        mBoomMenuButton.addBuilder(firstButton);
        final TextInsideCircleButton.Builder secondButton = buildMenuButton(R.drawable.add, R.color.colorMenuConvert, R.string.add);
        secondButton.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                mTranslationService.clearSourceText();
                mTranslationService.clearCurrentPassage();
            }
        });
        mBoomMenuButton.addBuilder(secondButton);
        mBoomMenuButton.addBuilder(buildMenuButton(R.drawable.clear, R.color.colorMenuClear, R.string.clear)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        mTranslationService.clearSourceText();
                    }
                }));
    }

    private void save() {
        if (mTranslationService.getCurrentPassageId() != Passage.NO_ID) {
            mTranslationService.saveCurrentPassage()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<PutResult>() {
                        @Override
                        public void call(PutResult result) {
                            if (result.wasUpdated()) {
                                Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return;
        }
        insertNewPassage();
    }

    private void insertNewPassage() {
        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(new TitleInputAndTagSelectBottomSheetBuilder(getActivity())
                .passage(mTranslationService.getCurrentPassage())
                .saveCallback(new TitleInputAndTagSelectBottomSheetBuilder.OnSaveButtonClickCallback() {
                    @Override
                    public void onClick(String title, Set<Integer> selectedPositions) {
                        doInsertingNewPassage(title, TextUtils.join(" ", selectedPositions));
                        dialog.dismiss();
                    }
                })
                .build());
        dialog.show();
    }

    private void doInsertingNewPassage(String title, String tag) {
        mTranslationService.insertPassage(title, tag)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PutResult>() {
                    @Override
                    public void call(PutResult result) {
                        if (result.wasInserted()) {
                            Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private TextInsideCircleButton.Builder buildMenuButton(int resId, int colorRes, int textRes) {
        return new TextInsideCircleButton.Builder()
                .buttonRadius(Util.dp2px(32))
                .imageRect(new Rect(Util.dp2px(10), Util.dp2px(12), Util.dp2px(54), Util.dp2px(40)))
                .textRect(new Rect(Util.dp2px(10), Util.dp2px(42), Util.dp2px(54), Util.dp2px(56)))
                .pieceColorRes(colorRes)
                .normalColorRes(colorRes)
                .normalImageRes(resId)
                .normalTextRes(textRes);
    }

    private void setUpViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapterBuilder(getChildFragmentManager())
                .addFragment(new EditFragment_())
                .addFragment(new TranslationResultFragment_())
                .build());
        mTranslationFromUnderline.setUpWithViewPager(mViewPager, 0);
        mTranslationToUnderline.setUpWithViewPager(mViewPager, 1);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                mTranslationService.submitSourceText();
            }
        });
    }

    @Click(R.id.text_translate_from)
    void showSourceTextPage() {
        if (mViewPager.getCurrentItem() == 0) {
            showSourceLanguageDialog();
            return;
        }
        mViewPager.setCurrentItem(0);
    }

    @Click(R.id.text_translate_to)
    void showTranslationResultPage() {
        if (mViewPager.getCurrentItem() == 1) {
            showTargetLanguageDialog();
            return;
        }
        mViewPager.setCurrentItem(1);
    }

    @Click(R.id.icon_translate)
    void scrollToNextPage() {
        mViewPager.setCurrentItem(1 - mViewPager.getCurrentItem());
    }


    @LongClick(R.id.text_translate_from)
    @Click(R.id.icon_select_source_format)
    void showSourceLanguageDialog() {
        if (mShowingMenu != null) {
            mShowingMenu.dismiss();
            return;
        }
        mShowingMenu = buildPopupMenu(mFormatOptions);
        mShowingMenu.setOnItemClickListener(new BubblePopupMenu.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                setSourceFormat(position);
                mShowingMenu.dismiss();
            }
        });
        mShowingMenu.showAsDropDown(mTranslationFromUnderline, -mShowingMenu.getWidth(), 0);
    }

    private BubblePopupMenu buildPopupMenu(List<String> options) {
        BubblePopupMenu menu = new BubblePopupMenu(getActivity(), options);
        menu.setWidth(Util.dp2px(150));
        menu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mShowingMenu = null;
            }
        });
        return menu;
    }

    @LongClick(R.id.text_translate_to)
    @Click(R.id.icon_select_target_language)
    void showTargetLanguageDialog() {
        if (mShowingMenu != null) {
            mShowingMenu.dismiss();
            return;
        }
        mShowingMenu = buildPopupMenu(OOXX_OPTIONS);
        mShowingMenu.setOnItemClickListener(new BubblePopupMenu.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                setOOXXInterval(position);
                mShowingMenu.dismiss();
            }
        });
        mShowingMenu.showAsDropDown(mTranslationToUnderline, 0, 0);

    }

    private void setOOXXInterval(int position) {
        mTranslationService.setOOXXInterval(position);
        mOOXX.setText(OOXX_OPTIONS.get(position));
    }


    private void setSourceFormat(int position) {
        mTranslationService.setSourceFormat(position);
        mSourceFormat.setText(mFormatOptions.get(position));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
