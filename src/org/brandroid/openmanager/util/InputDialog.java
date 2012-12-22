
package org.brandroid.openmanager.util;

import org.brandroid.openmanager.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class InputDialog extends Builder {
    private View view;
    private EditText mEdit, mEdit2;

    public InputDialog(Context mContext) {
        super(mContext);
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.input_dialog_layout, null);
        mEdit = (EditText)view.findViewById(R.id.dialog_input);
        mEdit2 = (EditText)view.findViewById(R.id.dialog_input_top);
        setViewVisible(false, R.id.dialog_message, R.id.dialog_message_top, R.id.dialog_input_top);
        super.setView(view);
    }

    public void setViewVisible(boolean visible, int... ids) {
        for (int id : ids)
            if (view.findViewById(id) != null)
                view.findViewById(id).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public InputDialog setTitle(int resId) {
        super.setTitle(resId);
        return this;
    }

    public InputDialog setIcon(int resId) {
        super.setIcon(resId);
        return this;
    }

    public String getInputTopText() {
        return mEdit2.getText().toString();
    }

    public String getInputText() {
        return mEdit.getText().toString();
    }

    public InputDialog setDefaultText(String s) {
        mEdit.setText(s);
        return this;
    }

    public InputDialog setDefaultTop(CharSequence s) {
        mEdit2.setVisibility(View.VISIBLE);
        mEdit2.setText(s);
        return this;
    }

    public InputDialog setDefaultTop(CharSequence s, boolean enabled) {
        mEdit2.setEnabled(enabled);
        return setDefaultTop(s);
    }

    public InputDialog setMessageTop(String s) {
        ((TextView)view.findViewById(R.id.dialog_message_top)).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.dialog_message_top)).setText(s);
        mEdit2.setVisibility(View.VISIBLE);
        return this;
    }

    public InputDialog setMessageTop(int resId) {
        ((TextView)view.findViewById(R.id.dialog_message_top)).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.dialog_message_top)).setText(resId);
        mEdit2.setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public InputDialog setMessage(CharSequence message) {
        ((TextView)view.findViewById(R.id.dialog_message)).setText(message);
        ((TextView)view.findViewById(R.id.dialog_message)).setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public InputDialog setMessage(int messageId) {
        ((TextView)view.findViewById(R.id.dialog_message)).setText(messageId);
        ((TextView)view.findViewById(R.id.dialog_message)).setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public InputDialog setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    @Override
    public InputDialog setTitle(CharSequence title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public InputDialog setNegativeButton(int text, OnClickListener listener) {
        super.setNegativeButton(text, listener);
        return this;
    }

    @Override
    public InputDialog setPositiveButton(int text, OnClickListener listener) {
        super.setPositiveButton(text, listener);
        return this;
    }

    @Override
    public InputDialog setOnCancelListener(OnCancelListener onCancelListener) {
        super.setOnCancelListener(onCancelListener);
        return this;
    }

    @Override
    public InputDialog setNeutralButton(int text, OnClickListener listener) {
        super.setNeutralButton(text, listener);
        return this;
    }

    @Override
    public InputDialog setIcon(Drawable icon) {
        super.setIcon(icon);
        return this;
    }

    public View getView() {
        return view;
    }

    @Override
    public AlertDialog create() {
        return super.create();
    }

    @Override
    public AlertDialog show() {
        AlertDialog ret = super.show();
        if (mEdit2.getVisibility() == View.VISIBLE)
            mEdit2.requestFocus();
        return ret;
    }

}
