
package com.xiaomakj.bdvoice.recognition;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 识别对话框提示语列表。使用bdspeech_suggestion_item布局文件，支持{@link #setTextColor(int)}设置字体颜色
 *
 * @author yangliang02
 */
class TipsAdapter extends ArrayAdapter<String> {
    private int mTextColor;

    private static String ITEM_FORMAT = "%1$d.\"%2$s\"";

    private TipsAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TipsAdapter(Context context) {
        this(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;

        if (convertView == null) {
            view = View.inflate(getContext(), getContext().getResources()
                    .getIdentifier("bdspeech_suggestion_item", "layout", getContext().getPackageName()), null);
        } else {
            view = convertView;
        }

        try {
            if (view instanceof TextView) {
                // If no custom field is assigned, assume the whole resource is
                // a TextView
                text = (TextView) view;
            } else {
                // Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewWithTag("textView");
            }
            text.setTextColor(mTextColor);
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        text.setText(String.format(ITEM_FORMAT, position + 1, getItem(position)));
        return view;
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }
}
