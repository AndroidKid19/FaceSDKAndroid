package com.baidu.idl.face.main.attribute.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.baidu.idl.main.facesdk.attrbutelibrary.R;

/**
 * author : shangrong
 * date : 2019/6/10 10:12 PM
 * description :
 */
public class RoundCheckBox extends AppCompatCheckBox {
    public RoundCheckBox(Context context) {
        this(context, null);
    }

    public RoundCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radioButtonStyle);
    }

    public RoundCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
