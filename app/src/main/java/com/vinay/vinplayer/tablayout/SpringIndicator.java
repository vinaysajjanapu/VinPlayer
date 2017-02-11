/*
 * Copyright 2015 chenupt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vinay.vinplayer.tablayout;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vinay.vinplayer.R;

import java.util.ArrayList;
import java.util.List;

public class SpringIndicator extends HorizontalScrollView {

    private static final int INDICATOR_ANIM_DURATION = 3000;

    private float acceleration = 0.5f;
    private float headMoveOffset = 0.6f;
    private float footMoveOffset = 1 - headMoveOffset;
    private float radiusMax;
    private float radiusMin;
    private float radiusOffset;

    private float textSize;
    private int textColorId;
    private int textBgResId;
    private int selectedTextColorId;
    private int indicatorColorId;
    private int indicatorColorsId;
    private int[] indicatorColorArray;

    private LinearLayout tabContainer;
    private SpringView springView;
    private ViewPager viewPager;

    private List<TextView> tabs;

    private ViewPager.OnPageChangeListener delegateListener;
    private TabClickListener tabClickListener;
    private ObjectAnimator indicatorColorAnim;

    private int titleOffset;
    private static final int TITLE_OFFSET_AUTO_CENTER = -1;
    private float startMargin;
    private float endMargin;
    private float defaultMargin;
    private FrameLayout mFrameLayout;

    public SpringIndicator(Context context) {
        this(context, null);
    }

    public SpringIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        textColorId = R.color.si_default_text_color;
        selectedTextColorId = R.color.si_default_text_color_selected;
        indicatorColorId = R.color.si_default_indicator_bg;
        textSize = getResources().getDimension(R.dimen.si_default_text_size);
        radiusMax = getResources().getDimension(R.dimen.si_default_radius_max);
        radiusMin = getResources().getDimension(R.dimen.si_default_radius_min);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpringIndicator);
        textColorId = a.getResourceId(R.styleable.SpringIndicator_siTextColor, textColorId);
        selectedTextColorId = a.getResourceId(R.styleable.SpringIndicator_siSelectedTextColor, selectedTextColorId);
        textSize = a.getDimension(R.styleable.SpringIndicator_siTextSize, textSize);
        textBgResId = a.getResourceId(R.styleable.SpringIndicator_siTextBg, 0);

        indicatorColorId = a.getResourceId(R.styleable.SpringIndicator_siIndicatorColor, indicatorColorId);
        indicatorColorsId = a.getResourceId(R.styleable.SpringIndicator_siIndicatorColors, 0);
        radiusMax = a.getDimension(R.styleable.SpringIndicator_siRadiusMax, radiusMax);
        radiusMin = a.getDimension(R.styleable.SpringIndicator_siRadiusMin, radiusMin);

        // initialized margins
        startMargin = a.getDimension(R.styleable.SpringIndicator_startMargin, startMargin);
        endMargin = a.getDimension(R.styleable.SpringIndicator_endMargin, endMargin);
        defaultMargin = a.getDimension(R.styleable.SpringIndicator_defaultMargin, defaultMargin);
        a.recycle();

        if (indicatorColorsId != 0) {
            indicatorColorArray = getResources().getIntArray(indicatorColorsId);
        }
        radiusOffset = radiusMax - radiusMin;
    }


    public void setViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
        initSpringView();
        setUpListener();
    }


    private void initSpringView() {
        addPointView();
        addTabContainerView();
        addTabItems();
    }

    private void addPointView() {
        mFrameLayout = new FrameLayout(getContext());
        mFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT));
        springView = new SpringView(getContext());
        springView.setIndicatorColor(getResources().getColor(indicatorColorId));
        mFrameLayout.addView(springView);
    }

    private void addTabContainerView() {
        tabContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        tabContainer.setLayoutParams(params);
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabContainer.setGravity(Gravity.CENTER);
        mFrameLayout.addView(tabContainer);
        addView(mFrameLayout);
        setHorizontalScrollBarEnabled(false);
    }

    private void addTabItems() {

        LinearLayout.LayoutParams layoutParamsLeft =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        LinearLayout.LayoutParams layoutParamsRight =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        layoutParamsRight.setMargins(((int) defaultMargin), /*((int) defaultMargin)*/0,
                ((int) endMargin), 0/*((int) defaultMargin)*/);

        layoutParamsLeft.setMargins(((int) startMargin), 0/*((int) defaultMargin)*/,
                ((int) defaultMargin), 0/*((int) defaultMargin)*/);

        tabContainer.setPadding((/*(int) startMargin)*/0), 0, 0, 0);

        tabs = new ArrayList<>();
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            TextView textView = new TextView(getContext());
            if (viewPager.getAdapter().getPageTitle(i) != null) {
                textView.setText(viewPager.getAdapter().getPageTitle(i));
            }
            textView.setGravity(Gravity.CENTER);
            textView.setSingleLine(true);
            textView.setTextColor(getResources().getColor(textColorId));
            if (textBgResId != 0) {
                textView.setBackgroundResource(textBgResId);
            }
            textView.setTag(i);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tabClickListener == null ||
                            tabClickListener.onTabClick((int) v.getTag())) {
                        viewPager.setCurrentItem((int) v.getTag());
                    }
                }
            });

//            TO DO SET MARGINS
            if (i == viewPager.getAdapter().getCount() - 1) {
                textView.setLayoutParams(layoutParamsRight);
            } else if (i == 0) {
                textView.setLayoutParams(layoutParamsLeft);
            } else {
                textView.setLayoutParams(layoutParamsLeft);
            }
            tabs.add(textView);
            tabContainer.addView(textView);
        }
    }

    /**
     * Set current point position.
     */
    private void createPoints() {
        View view = tabs.get(viewPager.getCurrentItem());
        springView.getHeadPoint().setX(view.getX() + view.getWidth() / 2);
        springView.getHeadPoint().setY(view.getY() + view.getHeight() / 2);
        springView.getFootPoint().setX(view.getX() + view.getWidth() / 2);
        springView.getFootPoint().setY(view.getY() + view.getHeight() / 2);
        springView.animCreate();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            createPoints();
            setSelectedTextColor(viewPager.getCurrentItem());
        }
    }


    private void setUpListener() {
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setSelectedTextColor(position);
                if (delegateListener != null) {
                    delegateListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < tabs.size() - 1) {
                    // radius
                    float radiusOffsetHead = 0.5f;
                    if (positionOffset < radiusOffsetHead) {
                        springView.getHeadPoint().setRadius(radiusMin);
                    } else {
                        springView.getHeadPoint().setRadius(((positionOffset - radiusOffsetHead) / (1 - radiusOffsetHead) * radiusOffset + radiusMin));
                    }
                    float radiusOffsetFoot = 0.5f;
                    if (positionOffset < radiusOffsetFoot) {
                        springView.getFootPoint().setRadius((1 - positionOffset / radiusOffsetFoot) * radiusOffset + radiusMin);
                    } else {
                        springView.getFootPoint().setRadius(radiusMin);
                    }

                    // x
                    float headX = 1f;
                    if (positionOffset < headMoveOffset) {
                        float positionOffsetTemp = positionOffset / headMoveOffset;
                        headX = (float) ((Math.atan(positionOffsetTemp * acceleration * 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
                    }
                    springView.getHeadPoint().setX(getTabX(position) - headX * getPositionDistance(position));
                    float footX = 0f;
                    if (positionOffset > footMoveOffset) {
                        float positionOffsetTemp = (positionOffset - footMoveOffset) / (1 - footMoveOffset);
                        footX = (float) ((Math.atan(positionOffsetTemp * acceleration * 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
                    }
                    springView.getFootPoint().setX(getTabX(position) - footX * getPositionDistance(position));

                    // reset radius
                    if (positionOffset == 0) {
                        springView.getHeadPoint().setRadius(radiusMax);
                        springView.getFootPoint().setRadius(radiusMax);
                    }
                } else {
                    springView.getHeadPoint().setX(getTabX(position));
                    springView.getFootPoint().setX(getTabX(position));
                    springView.getHeadPoint().setRadius(radiusMax);
                    springView.getFootPoint().setRadius(radiusMax);
                }

                // set indicator colors
                // https://github.com/TaurusXi/GuideBackgroundColorAnimation
                if (indicatorColorsId != 0) {
                    float length = (position + positionOffset) / viewPager.getAdapter().getCount();
                    int progress = (int) (length * INDICATOR_ANIM_DURATION);
                    seek(progress);
                }

                springView.postInvalidate();
                if (delegateListener != null) {
                    delegateListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
                scrollToTab(position, positionOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (delegateListener != null) {
                    delegateListener.onPageScrollStateChanged(state);
                }
            }
        });
    }


    private float getPositionDistance(int position) {
        float tarX = tabs.get(position + 1).getX();
        float oriX = tabs.get(position).getX();
        return oriX - tarX;
    }

    private float getTabX(int position) {
        return tabs.get(position).getX() + tabs.get(position).getWidth() / 2;
    }

    private void setSelectedTextColor(int position) {
        for (TextView tab : tabs) {
            tab.setTextColor(getResources().getColor(textColorId));
        }
        tabs.get(position).setTextColor(getResources().getColor(selectedTextColorId));
    }

    private void createIndicatorColorAnim() {
        indicatorColorAnim = ObjectAnimator.ofInt(springView, "indicatorColor", indicatorColorArray);
        indicatorColorAnim.setEvaluator(new ArgbEvaluator());
        indicatorColorAnim.setDuration(INDICATOR_ANIM_DURATION);
    }

    private void seek(long seekTime) {
        if (indicatorColorAnim == null) {
            createIndicatorColorAnim();
        }
        indicatorColorAnim.setCurrentPlayTime(seekTime);
    }

    public List<TextView> getTabs() {
        return tabs;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegateListener = listener;
    }

    public void setOnTabClickListener(TabClickListener listener) {
        this.tabClickListener = listener;
    }

    /*
    * add new params defaultMargin ,EndMargin and start margin in attrs.xml file
    * Change the parent layout to horizontal scroll view
    * add new variable below mentioned
    *
    * copy the addTabItems() ,addPointView(),addTabContainerView()
    *
    * */
    private void scrollToTab(int tabIndex, float positionOffset) {
        final int tabStripChildCount = tabContainer.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        final boolean isLayoutRtl = Utils.isLayoutRtl(this);
        View selectedTab = tabContainer.getChildAt(tabIndex);
        int widthPlusMargin = Utils.getWidth(selectedTab) + Utils.getMarginHorizontally(selectedTab);
        int extraOffset = (int) (positionOffset * widthPlusMargin);
        int x;
        if (titleOffset == TITLE_OFFSET_AUTO_CENTER) {

            if (0f < positionOffset && positionOffset < 1f) {
                View nextTab = tabContainer.getChildAt(tabIndex + 1);
                int selectHalfWidth = Utils.getWidth(selectedTab) / 2 + Utils.getMarginEnd(selectedTab);
                int nextHalfWidth = Utils.getWidth(nextTab) / 2 + Utils.getMarginStart(nextTab);
                extraOffset = Math.round(positionOffset * (selectHalfWidth + nextHalfWidth));
            }

            if (isLayoutRtl) {
                x = -Utils.getWidthWithMargin(selectedTab) / 2 + getWidth() / 2;
                x -= Utils.getPaddingStart(this);
            } else {
                x = Utils.getWidthWithMargin(selectedTab) / 2 - getWidth() / 2;
                x += Utils.getPaddingStart(this);
            }

        } else {

            if (isLayoutRtl) {
                x = (tabIndex > 0 || positionOffset > 0) ? titleOffset : 0;
            } else {
                x = (tabIndex > 0 || positionOffset > 0) ? -titleOffset : 0;
            }

        }

        int start = Utils.getStart(selectedTab);
        int startMargin = Utils.getMarginStart(selectedTab);
        if (isLayoutRtl) {
            x += start + startMargin - extraOffset - getWidth() + Utils.getPaddingHorizontally(this);
        } else {
            x += start - startMargin + extraOffset;
        }

        scrollTo(x, 0);
    }
}