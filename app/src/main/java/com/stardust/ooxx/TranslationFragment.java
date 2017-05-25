package com.stardust.ooxx;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Util;
import com.stardust.ooxx.app.FragmentPagerAdapterBuilder;
import com.stardust.ooxx.data.Translation;
import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.widget.BubblePopupMenu;
import com.stardust.ooxx.widget.UnderlineLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Stardust on 2017/5/23.
 */
@EFragment(R.layout.fragment_translation)
public class TranslationFragment extends Fragment {

    private static final List<String> OOXX_OPTIONS = Arrays.asList("XXXX", "OXOX", "OOXX", "OOOX", "OOOO");
    private static final int[] FORMAT_OPTIONS = {R.string.cn, R.string.en, R.string.word};

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

    private List<String> mFormatOptions = new ArrayList<>();


    @AfterViews
    void setUpUI() {
        setUpViewPager();
        setUpBoomMenu();
        for (int i : FORMAT_OPTIONS) {
            mFormatOptions.add(getString(i));
        }
        setOOXXInterval(TranslationService.getInstance().getOOXXInterval());
        setSourceFormat(TranslationService.getInstance().getSourceFormat());
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
        final TextInsideCircleButton.Builder secondButton = buildMenuButton(R.drawable.convert, R.color.colorMenuConvert, R.string.translate);
        secondButton.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                if (mViewPager.getCurrentItem() == 0) {
                    showTranslationResultPage();
                    secondButton.normalImageRes(R.drawable.ic_border_color_white_48dp);
                    secondButton.normalTextRes(R.string.edit);
                } else {
                    showSourceTextPage();
                    secondButton.normalImageRes(R.drawable.convert);
                    secondButton.normalTextRes(R.string.translate);
                }
            }
        });
        mBoomMenuButton.addBuilder(secondButton);
        mBoomMenuButton.addBuilder(buildMenuButton(R.drawable.clear, R.color.colorMenuClear, R.string.clear)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        TranslationService.getInstance().clearSourceText();
                    }
                }));
    }

    private void save() {
        if (TranslationService.getInstance().getCurrentPassageId() != Passage.NO_ID) {
            TranslationService.getInstance().saveCurrentPassage();
            return;
        }

        new MaterialDialog.Builder(getActivity())
                .title(R.string.input_title)
                .input("", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        TranslationService.getInstance().insertPassage(input.toString(), "");
                    }
                })
                .show();
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
                TranslationService.getInstance().submitSourceText();
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
        final BubblePopupMenu menu = new BubblePopupMenu(getActivity(), mFormatOptions);
        menu.setWidth(Util.dp2px(150));
        menu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menu.setOnItemClickListener(new BubblePopupMenu.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                setSourceFormat(position);
                menu.dismiss();
            }
        });
        menu.showAsDropDown(mTranslationFromUnderline, -menu.getWidth(), 0);
    }

    @LongClick(R.id.text_translate_to)
    @Click(R.id.icon_select_target_language)
    void showTargetLanguageDialog() {
        final BubblePopupMenu menu = new BubblePopupMenu(getActivity(), OOXX_OPTIONS);
        menu.setWidth(Util.dp2px(150));
        menu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menu.setOnItemClickListener(new BubblePopupMenu.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                setOOXXInterval(position);
                menu.dismiss();
            }
        });
        menu.showAsDropDown(mTranslationToUnderline, 0, 0);

    }

    private void setOOXXInterval(int position) {
        TranslationService.getInstance().setOOXXInterval(position);
        mOOXX.setText(OOXX_OPTIONS.get(position));
    }


    private void setSourceFormat(int position) {
        TranslationService.getInstance().setSourceFormat(position);
        mSourceFormat.setText(mFormatOptions.get(position));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
