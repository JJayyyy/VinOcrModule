package com.dede.vin_component.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

/**
 * @author hsh
 * @since 2017/6/19 019 上午 09:43.
 * 车架号输入框，需要{@link VinEditText#addTextChangedListener(VinTextWatcher)}才会格式化车架号，格式 xxxx xxxx xxxx xxxxx
 */
public class VinEditText extends AppCompatEditText {

    public static final String TAG = "VinEditText";

    public VinEditText(Context context) {
        super(context);
        init();
    }

    public VinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VinEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMaxLength(20);//最大长度为20
        setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    /**
     * 设置最大长度
     *
     * @param length 最大输入长度，默认为20
     * @author hsh
     * @since 2017/6/19 019 下午 01:58
     */
    public void setMaxLength(int length) {
        if (length < 0)
            return;
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
    }

    /**
     * 获取Vin码，不包含空格
     *
     * @return vinStr
     * @author hsh
     * @since 2017/6/19 019 下午 01:59
     */
    public String getVin() {
        return getText().toString().replace(" ", "");
    }

    public void addTextChangedListener(VinTextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        if (watcher != null && !(watcher instanceof VinTextWatcher)) {
            Log.w(TAG, "addTextChangedListener: " + watcher);
        }
    }

    /**
     * 格式化Vin码的TextWatcher
     */
    public static class VinTextWatcher implements TextWatcher {

        private TextWatcher watcher;
        private VinEditText editText;
        private boolean isDelete;//是否是删除

        public VinTextWatcher(VinEditText editText, TextWatcher watcher) {
            this.watcher = watcher;
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isDelete = after == 0;//改变后的内容长度为0时是删除
            if (watcher != null)
                watcher.beforeTextChanged(s, start, count, after);
        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (watcher != null)
                watcher.onTextChanged(s, start, before, count);
        }

        private boolean isChange = true;

        @Override
        public void afterTextChanged(Editable s) {
            if (isChange) {
                int index = editText.getSelectionStart();
                boolean isSystemCopyPaste = index == 17 && !s.toString().contains(" ");//gengqiquan 2017/6/20 是否通过系统粘贴事件粘贴。
                String text = s.toString().replace(" ", "").toUpperCase();
                char[] chars = text.toCharArray();
                text = "";
                for (int i = 0; i < chars.length; i++) {
                    text += ((i > 0 && i < 16 && i % 4 == 0) ? " " : "") + chars[i];
                }
                if (isDelete) {
                    index -= ((index % 5 == 0 && index > 0) ? 1 : 0);
                } else {
                    index += ((index % 5 == 0 && index > 0 && index < 16) ? 1 : 0);
                }
                isChange = false;
                editText.setText(text);
                try {
                    editText.setSelection(isSystemCopyPaste ? text.length() : index);//设置光标位置
                } catch (IndexOutOfBoundsException ignored) {
                }
                isChange = true;
                if (watcher != null && editText != null)
                    watcher.afterTextChanged(Editable.Factory.getInstance().newEditable(editText.getVin()));
            }

        }
    }
}
