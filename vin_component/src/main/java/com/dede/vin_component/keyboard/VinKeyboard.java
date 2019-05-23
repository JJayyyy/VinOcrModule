package com.dede.vin_component.keyboard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;


import com.dede.vin_component.R;

import org.jetbrains.anko.DimensionsKt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class VinKeyboard {
    private ViewGroup mParent;
    private EditText mVinEditView;
    private Activity mActivity;
    private FrameLayout mContentView;
    private onSureClickListener mOnSureClickListener;
    private SoftInputChangeListener listener;
    private boolean isShow = false;

    private ScrollView scrollView;

    public VinKeyboard(Activity activity, EditText vin) {
        ViewParent parent = vin.getParent();
        while (parent != null && !(parent instanceof ScrollView)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            scrollView = (ScrollView) parent;
        }

        mVinEditView = vin;
        mActivity = activity;
        mParent = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.layout_vin_keyboard, null);
        mParent.findViewById(R.id.tv_kb_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSureClickListener == null || !mOnSureClickListener.onClick(mVinEditView)) {
                    hideKeyboard();
                }
            }
        });
        setSystemKeyboardEnable(false);

        mVinEditView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK && isShow) {
                    hideKeyboard();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mVinEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });
        Keyboard keyboard = new Keyboard(mActivity, R.xml.vin_keyboard);
        final KeyboardView keyboardView = (KeyboardView) mParent.findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setEnabled(true);
        OnKeyboardActionListener listener = new OnKeyboardActionListener() {
            @Override
            public void swipeUp() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onPress(int primaryCode) {
                if (primaryCode == 81 || primaryCode == 73 || primaryCode == 79 || primaryCode == -5 || primaryCode == -10)
                    keyboardView.setPreviewEnabled(false);
                else
                    keyboardView.setPreviewEnabled(true);
            }

            //一些特殊操作按键的codes是固定的比如完成、回退等
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                Editable editable = mVinEditView.getText();
                int start = mVinEditView.getSelectionStart();
                switch (primaryCode) {
                    case Keyboard.KEYCODE_DELETE:
                        if (editable != null && editable.length() > 0) {
                            if (start > 0) {
                                editable.delete(start - 1, start);
                            }
                        }
                        break;
                    case -10:
                        //handle paste
                        ClipboardManager myClipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = myClipboard.getPrimaryClip();
                        if (clip != null && clip.getItemCount() > 0)
                            editable.insert(start, clip.getItemAt(0).coerceToText(mActivity));
                        break;
                    case 73:
                    case 79:
                    case 81:
                        //handle paste
                        break;
                    default:
                        editable.insert(start, Character.toString((char) primaryCode));

                }
            }
        };
        keyboardView.setOnKeyboardActionListener(listener);
        ArrayList<Keyboard.Key> list = (ArrayList<Keyboard.Key>) keyboardView.getKeyboard().getKeys();
        for (Keyboard.Key key : list) {
            if (key.codes[0] == 81 || key.codes[0] == 73 || key.codes[0] == 79) {
                key.onPressed();
            }
        }
        mContentView = (FrameLayout) mActivity.findViewById(android.R.id.content);
        mChildOfContent = mContentView.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (newFocus == null)
                    return;
                if (newFocus.equals(mVinEditView) && !isShow) {
                    showKeyboard();
                } else if (!newFocus.equals(mVinEditView) && isShow) {
                    hideKeyboard();
                }
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        frameLayoutParamsHeight = frameLayoutParams.height;
    }

    /**
     * 设置软键盘状态监听
     *
     * @param listener
     */
    public void setOnSoftInputChangeListener(SoftInputChangeListener listener) {
        this.listener = listener;
    }

    public void showKeyboard() {
        if (isShow) {
            return;
        }
        hideSystemKeyboard(mActivity, mVinEditView);
        if (listener != null) listener.change(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DimensionsKt.dip(mActivity, 256));
        layoutParams.gravity = Gravity.BOTTOM;
        ViewGroup parent = (ViewGroup) mParent.getParent();
        if (parent != null) {
            parent.removeView(mParent);
        }
        mContentView.addView(mParent, layoutParams);
        isShow = true;

        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.anim_show_keyboard);
        animation.setAnimationListener(new AnimationListenerImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                moveEditShow(mActivity);

                if (scrollView == null) return;
                int[] l = new int[2];
                mVinEditView.getLocationInWindow(l);
                int bottom = l[1] + mVinEditView.getHeight();// 输入框底部y坐标值
                Rect rect = new Rect();
                mVinEditView.getWindowVisibleDisplayFrame(rect);
                int height = rect.height();// window height
                int c = mParent.getHeight() + bottom - height;
                if (c > 0) {// 需要滚动
                    scrollView.scrollBy(0, c);
                }
            }
        });
        mParent.startAnimation(animation);
    }

    /**
     * 设置系统键盘是否弹出
     *
     * @param enable true 弹出系统键盘，false 不弹出
     * @author hsh
     * @time 2017/6/1 001 下午 04:09
     * @version 1.7.6
     */
    private void setSystemKeyboardEnable(boolean enable) {
        try {
            Method setShowSoftInputOnFocus = mVinEditView.getClass().getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(mVinEditView, enable);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回true表示调用者拦截事件自己处理
     *
     * @author gengqiquan
     * @date 2017/6/1 下午2:39
     */
    public void setOnSureClicklistener(onSureClickListener sureClicklistener) {
        mOnSureClickListener = sureClicklistener;
    }

    public void hideKeyboard() {
        if (!isShow) {
            return;
        }
        if (listener != null) listener.change(false);
        moveEditHide(mActivity);

        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.anim_hide_keyboard);
        animation.setAnimationListener(new AnimationListenerImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mContentView.removeView(mParent);
            }
        });
        mParent.startAnimation(animation);
        isShow = false;
    }

    private View mChildOfContent;
    private FrameLayout.LayoutParams frameLayoutParams;
    private int frameLayoutParamsHeight;

    private void moveEditShow(Context activity) {
        int usableHeightNow = computeUsableHeight(activity);
        frameLayoutParams.height = usableHeightNow - DimensionsKt.dip(activity, 256);
        mChildOfContent.requestLayout();
    }

    private void moveEditHide(Context activity) {
        frameLayoutParams.height = frameLayoutParamsHeight;// 恢复原来的高度
        mChildOfContent.requestLayout();
    }

    private int computeUsableHeight(Context activity) {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.height() /*+ ScreenUtils.getStatusHeight(activity)*/);
    }

    /**
     * 隐藏系统键盘
     *
     * @param v
     */
    private void hideSystemKeyboard(Context context, View v) {
        InputMethodManager inputManger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManger.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * 返回true表示调用者拦截事件自己处理
     *
     * @author gengqiquan
     * @date 2017/6/1 下午2:39
     */
    public interface onSureClickListener {
        boolean onClick(EditText v);
    }


    private static class AnimationListenerImpl implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
