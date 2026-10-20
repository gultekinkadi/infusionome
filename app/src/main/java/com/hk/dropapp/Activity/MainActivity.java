package com.hk.dropapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hk.dropapp.Dialog.SelectLanguageDialog;
import com.hk.dropapp.Enums.Language;
import com.hk.dropapp.Helpers.SharedHelper;
import com.hk.dropapp.Public.Calculate;
import com.hk.dropapp.R;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String[] drops = new String[]{"10","15","20","30","60"};
    private EditText siviHacmiEdit,ilacHacmiEdit,ilacMiktariEdit,kiloEdit,
    dozEdit;
    private Spinner dropSpinner;
    private Button calcButton;
    private Activity mActivity;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindIDs();
    }



    private void bindIDs() {
        mActivity = this;
        siviHacmiEdit = findViewById(R.id.main_sivi_hacmi);
        ilacHacmiEdit = findViewById(R.id.main_ilac_hacmi);
        ilacMiktariEdit = findViewById(R.id.main_ilac_miktari);
        kiloEdit = findViewById(R.id.main_kilo);
        dozEdit = findViewById(R.id.main_doz);
        dropSpinner = findViewById(R.id.main_drop);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1,drops);
        dropSpinner.setAdapter(arrayAdapter);
        dropSpinner.setSelection(2,true);
        calcButton = findViewById(R.id.main_calculate);

        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputs();
            }
        });

        toolbar = findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.main_menu);



        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.change_volume){
                    SharedHelper.setSound(mActivity,!SharedHelper.getSound(mActivity));
                    doMenuState();
                    String message;
                    if(SharedHelper.getSound(mActivity)){
                        message = getString(R.string.ses_acildi);
                    }else{
                        message = getString(R.string.ses_kapatildi);
                    }
                    Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
                }
                else if(item.getItemId() == R.id.change_vibration){
                    SharedHelper.setVibration(mActivity,!SharedHelper.getVibration(mActivity));
                    doMenuState();
                    String message;
                    if(SharedHelper.getVibration(mActivity)){
                        message = getString(R.string.titresim_acildi);
                    }else{
                        message = getString(R.string.titresim_kapandi);
                    }
                    Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
                }
                else if(item.getItemId() == R.id.change_language){
                    SelectLanguageDialog sld = new SelectLanguageDialog(mActivity, new SelectLanguageDialog.SelecLanguageDialogListener() {
                        @Override
                        public void onClick(Language language) {
                            SharedHelper.setLang(mActivity,language.toString());
                            setLocale(language.toString());
                        }
                    });
                    sld.show();

                }
                else if(item.getItemId() == R.id.menu_info){
                    String message = getString(R.string.info_message);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("Info");
                    builder.setMessage(message);
                    builder.show();
                }
                return false;
            }
        });
        doMenuState();
    }

    private void setLocale(String language){
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = new Locale(language.toLowerCase());
        resources.updateConfiguration(configuration,metrics);
        onConfigurationChanged(configuration);
        Intent intent = new Intent(mActivity,MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }
    private void doMenuState(){
        MenuItem soundItem = toolbar.getMenu().getItem(0);
        MenuItem vibItem = toolbar.getMenu().getItem(1);
        if(SharedHelper.getSound(mActivity)){
            soundItem.setIcon(R.drawable.ic_baseline_volume_off_24);
            soundItem.setTitle(getString(R.string.sesi_kapat));
        }else{
            soundItem.setIcon(R.drawable.ic_baseline_volume_up_24);
            soundItem.setTitle(getString(R.string.sesi_ac));
        }
        if(SharedHelper.getVibration(mActivity)){
           vibItem.setTitle(getString(R.string.titresimi_kapat));
        }else{
            vibItem.setTitle(getString(R.string.titresimi_ac));
        }
    }
    private void checkInputs() {
        String siviHacmiString = siviHacmiEdit.getText().toString().trim();
        String ilacHacmiString = ilacHacmiEdit.getText().toString().trim();
        String ilacMiktariString = ilacMiktariEdit.getText().toString().trim();
        String kiloString = kiloEdit.getText().toString().trim();
        String dozString = dozEdit.getText().toString().trim();
        String dropString = dropSpinner.getSelectedItem().toString().trim();

        if(!siviHacmiString.isEmpty()){
            if(!ilacHacmiString.isEmpty()){
                if(!ilacMiktariString.isEmpty()){
                    if(!kiloString.isEmpty()){
                        if(!dozString.isEmpty()){
                            if(!dropString.isEmpty()){
                                checkIsDouble(siviHacmiString,ilacHacmiString,ilacMiktariString,kiloString,dozString,dropString);
                            }else{
                                Toast.makeText(mActivity, getString(R.string.istenen_doz_giriniz), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(mActivity, getString(R.string.istenen_doz_giriniz), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(mActivity, getString(R.string.kilo_giriniz), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(mActivity, getString(R.string.ilac_miktari_giriniz), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(mActivity, getString(R.string.ilac_hacmi_giriniz), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(mActivity, getString(R.string.sivi_hacmi_giriniz), Toast.LENGTH_SHORT).show();
        }
    }



    private void checkIsDouble(String siviHacmiString, String ilacHacmiString, String ilacMiktariString, String kiloString, String dozString, String dropString) {
        try {
            Double siviHacmiD = Double.parseDouble(siviHacmiString);
            Double ilacHacmiD = Double.parseDouble(ilacHacmiString);
            Double ilacMiktariD = Double.parseDouble(ilacMiktariString);
            Double kiloD = Double.parseDouble(kiloString);
            Double dozD = Double.parseDouble(dozString);
            Double dropD = Double.parseDouble(dropString);
            calculate(siviHacmiD,ilacHacmiD,ilacMiktariD,kiloD,dozD,dropD);
        }catch (NumberFormatException e){
            Toast.makeText(mActivity, getString(R.string.gecersiz_deger_girisi), Toast.LENGTH_LONG).show();
        }
    }

    private void calculate(Double siviHacmiD, Double ilacHacmiD, Double ilacMiktariD, Double kiloD, Double dozD, Double dropD) {
        Map<String,String> results = Calculate.calculateSonuc(siviHacmiD,ilacHacmiD,ilacMiktariD,kiloD,dozD,dropD);
        Intent intent = new Intent(mActivity,CalculateActivity.class);
        intent.putExtra("sonuc",results.get("sonuc"));
        intent.putExtra("aralik",results.get("aralik"));
        startActivity(intent);
    }





}