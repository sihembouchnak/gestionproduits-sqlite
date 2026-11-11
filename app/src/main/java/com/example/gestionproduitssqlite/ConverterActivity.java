package com.example.gestionproduitssqlite;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ConverterActivity extends AppCompatActivity {

    RadioButton rbDinarTodollar;
    RadioButton rbDollarToDinar;
    EditText editTxtAmount;
    Button btnConvert;
    TextView txtResult;
    Button btnOnlineConversion;
    ImageButton btnBack;
    Button btnHeaderReturn;
    Button btnReturnMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        // Boutons de retour vers l'interface principale
        btnBack = findViewById(R.id.btnBack);
        btnHeaderReturn = findViewById(R.id.btnHeaderReturn);
        btnReturnMain = findViewById(R.id.btnReturnMain);

        View.OnClickListener returnListener = v -> finish();

        if (btnBack != null) btnBack.setOnClickListener(returnListener);
        if (btnHeaderReturn != null) btnHeaderReturn.setOnClickListener(returnListener);
        if (btnReturnMain != null) btnReturnMain.setOnClickListener(returnListener);

        // Récupération des références de vue selon les attributs demandés
        rbDinarTodollar = (RadioButton) findViewById(R.id.radioButtonTnDToUsD);
        rbDollarToDinar = (RadioButton) findViewById(R.id.radioButtonUsDToTnD);
        editTxtAmount = (EditText) findViewById(R.id.editTextAmountToConvert);
        btnConvert = (Button) findViewById(R.id.buttonConvert);
        txtResult = (TextView) findViewById(R.id.textViewResult);
        btnOnlineConversion = (Button) findViewById(R.id.buttonOnlineConversion);

        // Écouteur du bouton CONVERTIR
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTxtAmount.getText().length() == 0) {
                    showAlert("Erreur", "veuillez saisir un montant à convertir");
                } else {
                    double res = 0;
                    String amountStr = editTxtAmount.getText().toString().replace(',', '.');
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (rbDinarTodollar.isChecked()) {
                            res = amount / 3.1;
                            txtResult.setText(String.format(Locale.getDefault(), "Résultat: %.2f $", res));
                        } else if (rbDollarToDinar.isChecked()) {
                            res = amount * 3.1;
                            txtResult.setText(String.format(Locale.getDefault(), "Résultat: %.2f TND", res));
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Veuillez sélectionner un sens de conversion",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "veuillez saisir un montant à convertir");
                    }
                }
            }
        });

        // Intent implicite vers le lien XE.com TND -> DZD
        btnOnlineConversion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.xe.com/fr/currencyconverter/convert/?Amount=1&From=TND&To=DZD");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    // Message d'alerte selon les consignes TP7
    void showAlert(String titreAlerte, String messageAlerte) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(titreAlerte);
        alertDialogBuilder
                .setMessage(messageAlerte)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
