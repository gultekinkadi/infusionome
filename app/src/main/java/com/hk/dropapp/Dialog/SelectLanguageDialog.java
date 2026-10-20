package com.hk.dropapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.hk.dropapp.Enums.Language;
import com.hk.dropapp.R;

public class SelectLanguageDialog extends Dialog {
    private final ImageView tr,usa;
    private final SelecLanguageDialogListener listener;
    public SelectLanguageDialog(@NonNull Context context,SelecLanguageDialogListener listener) {
        super(context);
        setContentView(R.layout.dialog_select_language);
        tr = findViewById(R.id.dialog_sl_tr);
        usa = findViewById(R.id.dialog_sl_usa);
        this.listener = listener;
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(Language.TR);
                dismiss();
            }
        });
        usa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(Language.EN);
                dismiss();
            }
        });
    }


    public interface SelecLanguageDialogListener<T>{
        void onClick(Language language);
    }
}
