package com.hk.dropapp.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.WindowCompat;

import com.hk.dropapp.BuildConfig;
import com.hk.dropapp.Dialog.SelectLanguageDialog;
import com.hk.dropapp.Helpers.EdgeInsets;
import com.hk.dropapp.Helpers.SharedHelper;
import com.hk.dropapp.Public.Calculate;
import com.hk.dropapp.R;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_DROPS_PER_MIN = "dropsPerMin";
    public static final String EXTRA_SECONDS_PER_DROP = "secondsPerDrop";
    public static final String EXTRA_ML_PER_HOUR = "mlPerHour";
    public static final String EXTRA_CONCENTRATION = "concentration";
    public static final String EXTRA_CONC_IS_UNITS = "concIsUnits";

    private static final String[] DROPS = {"10", "15", "20", "30", "60"};
    private static final int DEFAULT_DROP_INDEX = 2; // 20 damla/ml

    private EditText siviHacmiEdit;
    private EditText ilacHacmiEdit;
    private EditText ilacMiktariEdit;
    private EditText kiloEdit;
    private EditText dozEdit;
    private Spinner dropSpinner;
    private Spinner amountUnitSpinner;
    private Spinner doseUnitSpinner;
    private View kiloRow;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        EdgeInsets.apply(findViewById(R.id.main_root), findViewById(R.id.main_appbar));
        bindIDs();
    }

    private void bindIDs() {
        siviHacmiEdit = findViewById(R.id.main_sivi_hacmi);
        ilacHacmiEdit = findViewById(R.id.main_ilac_hacmi);
        ilacMiktariEdit = findViewById(R.id.main_ilac_miktari);
        kiloEdit = findViewById(R.id.main_kilo);
        dozEdit = findViewById(R.id.main_doz);
        kiloRow = findViewById(R.id.main_kilo_row);

        dropSpinner = findViewById(R.id.main_drop);
        dropSpinner.setAdapter(makeAdapter(DROPS));
        dropSpinner.setSelection(clamp(SharedHelper.getInt(this, SharedHelper.DROP_INDEX, DEFAULT_DROP_INDEX), DROPS.length), false);

        amountUnitSpinner = findViewById(R.id.main_ilac_birim);
        amountUnitSpinner.setAdapter(makeAdapter(getResources().getStringArray(R.array.amount_units)));
        amountUnitSpinner.setSelection(clamp(SharedHelper.getInt(this, SharedHelper.AMOUNT_UNIT,
                Calculate.AmountUnit.MG.ordinal()), Calculate.AmountUnit.values().length), false);

        doseUnitSpinner = findViewById(R.id.main_doz_birim);
        doseUnitSpinner.setAdapter(makeAdapter(getResources().getStringArray(R.array.dose_units)));
        doseUnitSpinner.setSelection(clamp(SharedHelper.getInt(this, SharedHelper.DOSE_UNIT,
                Calculate.DoseUnit.MCG_KG_MIN.ordinal()), Calculate.DoseUnit.values().length), false);
        doseUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateKiloVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        updateKiloVisibility();

        Button calcButton = findViewById(R.id.main_calculate);
        calcButton.setOnClickListener(v -> checkInputs());

        toolbar = findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(this::onMenuItem);
        doMenuState();
    }

    private ArrayAdapter<String> makeAdapter(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private static int clamp(int index, int size) {
        return (index >= 0 && index < size) ? index : 0;
    }

    private Calculate.DoseUnit selectedDoseUnit() {
        return Calculate.DoseUnit.values()[doseUnitSpinner.getSelectedItemPosition()];
    }

    private Calculate.AmountUnit selectedAmountUnit() {
        return Calculate.AmountUnit.values()[amountUnitSpinner.getSelectedItemPosition()];
    }

    private void updateKiloVisibility() {
        kiloRow.setVisibility(selectedDoseUnit().perKg ? View.VISIBLE : View.GONE);
    }

    private boolean onMenuItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.change_volume) {
            boolean on = !SharedHelper.getSound(this);
            SharedHelper.setSound(this, on);
            doMenuState();
            toast(on ? R.string.ses_acildi : R.string.ses_kapatildi);
        } else if (id == R.id.change_vibration) {
            boolean on = !SharedHelper.getVibration(this);
            SharedHelper.setVibration(this, on);
            doMenuState();
            toast(on ? R.string.titresim_acildi : R.string.titresim_kapandi);
        } else if (id == R.id.change_language) {
            new SelectLanguageDialog(this, language -> {
                String tag = language.toString().toLowerCase();
                SharedHelper.setLang(this, tag);
                // Seçim AppCompat tarafından saklanır ve uygulama yeniden açıldığında da geçerli olur.
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
            }).show();
        } else if (id == R.id.menu_info) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.info)
                    .setMessage(getString(R.string.info_message, BuildConfig.VERSION_NAME))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
        return true;
    }

    private void doMenuState() {
        MenuItem soundItem = toolbar.getMenu().findItem(R.id.change_volume);
        MenuItem vibItem = toolbar.getMenu().findItem(R.id.change_vibration);
        // Simge mevcut durumu gösterir; başlık dokununca ne olacağını söyler.
        if (SharedHelper.getSound(this)) {
            soundItem.setIcon(R.drawable.ic_baseline_volume_up_24);
            soundItem.setTitle(R.string.sesi_kapat);
        } else {
            soundItem.setIcon(R.drawable.ic_baseline_volume_off_24);
            soundItem.setTitle(R.string.sesi_ac);
        }
        if (SharedHelper.getVibration(this)) {
            vibItem.setIcon(R.drawable.ic_baseline_vibration_24);
            vibItem.setTitle(R.string.titresimi_kapat);
        } else {
            vibItem.setIcon(R.drawable.ic_baseline_mobile_off_24);
            vibItem.setTitle(R.string.titresimi_ac);
        }
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void checkInputs() {
        Calculate.DoseUnit doseUnit = selectedDoseUnit();
        Calculate.AmountUnit amountUnit = selectedAmountUnit();

        if (isEmpty(siviHacmiEdit)) { fail(siviHacmiEdit, R.string.sivi_hacmi_giriniz); return; }
        if (isEmpty(ilacMiktariEdit)) { fail(ilacMiktariEdit, R.string.ilac_miktari_giriniz); return; }
        if (isEmpty(dozEdit)) { fail(dozEdit, R.string.istenen_doz_giriniz); return; }
        if (doseUnit.perKg && isEmpty(kiloEdit)) { fail(kiloEdit, R.string.kilo_giriniz); return; }

        double sivi = Calculate.parse(siviHacmiEdit.getText().toString());
        // İlaç hacmi boş bırakılırsa 0 kabul edilir (örn. toz ilaç).
        double ilacHacmi = isEmpty(ilacHacmiEdit) ? 0 : Calculate.parse(ilacHacmiEdit.getText().toString());
        double miktar = Calculate.parse(ilacMiktariEdit.getText().toString());
        double doz = Calculate.parse(dozEdit.getText().toString());
        double kilo = doseUnit.perKg ? Calculate.parse(kiloEdit.getText().toString()) : 0;

        if (Double.isNaN(sivi) || Double.isNaN(ilacHacmi) || Double.isNaN(miktar)
                || Double.isNaN(doz) || Double.isNaN(kilo)) {
            Toast.makeText(this, R.string.gecersiz_deger_girisi, Toast.LENGTH_LONG).show();
            return;
        }

        double drop = Double.parseDouble(DROPS[dropSpinner.getSelectedItemPosition()]);
        Calculate.Result r = Calculate.calculate(sivi, ilacHacmi, miktar, amountUnit, kilo, doz, doseUnit, drop);

        if (r.error == Calculate.Error.UNIT_MISMATCH) {
            Toast.makeText(this, R.string.birim_uyumsuz, Toast.LENGTH_LONG).show();
            return;
        }
        if (!r.isOk()) {
            Toast.makeText(this, R.string.sifir_deger, Toast.LENGTH_LONG).show();
            return;
        }

        SharedHelper.setInt(this, SharedHelper.DROP_INDEX, dropSpinner.getSelectedItemPosition());
        SharedHelper.setInt(this, SharedHelper.AMOUNT_UNIT, amountUnitSpinner.getSelectedItemPosition());
        SharedHelper.setInt(this, SharedHelper.DOSE_UNIT, doseUnitSpinner.getSelectedItemPosition());

        Intent intent = new Intent(this, CalculateActivity.class);
        intent.putExtra(EXTRA_DROPS_PER_MIN, r.dropsPerMin);
        intent.putExtra(EXTRA_SECONDS_PER_DROP, r.secondsPerDrop);
        intent.putExtra(EXTRA_ML_PER_HOUR, r.mlPerHour);
        intent.putExtra(EXTRA_CONCENTRATION, r.concentrationPerMl);
        intent.putExtra(EXTRA_CONC_IS_UNITS, r.concentrationIsUnits);
        startActivity(intent);
    }

    private static boolean isEmpty(EditText e) {
        return e.getText().toString().trim().isEmpty();
    }

    private void fail(EditText field, int msgRes) {
        field.requestFocus();
        toast(msgRes);
    }
}
