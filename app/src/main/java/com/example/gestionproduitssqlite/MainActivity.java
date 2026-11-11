package com.example.gestionproduitssqlite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewProduits;
    private Button btnAjouterProduit;
    private Button btnOuvrirConvertisseur;
    private TextView txtHeaderCount;
    private ProductDatabaseHelper dbHelper;
    private ArrayAdapter<Product> adapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues et de la base de données
        listViewProduits = findViewById(R.id.listViewProduits);
        btnAjouterProduit = findViewById(R.id.btnAjouterProduit);
        btnOuvrirConvertisseur = findViewById(R.id.btnOuvrirConvertisseur);
        txtHeaderCount = findViewById(R.id.txtHeaderCount);
        dbHelper = new ProductDatabaseHelper(this);

        // Charger et afficher la liste des produits au démarrage
        refreshProductList();

        // 1. Accès direct sans menu à 3 points : Ajouter Produit
        btnAjouterProduit.setOnClickListener(v -> showProductDialog(null));

        // 2. Accès direct sans menu à 3 points : Ouvrir Convertisseur TP7
        btnOuvrirConvertisseur.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConverterActivity.class);
            startActivity(intent);
        });

        // 3. Clic sur un produit : Boîte de dialogue pour Modifier ou Supprimer
        listViewProduits.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);
            showOptionsDialog(selectedProduct);
        });
    }

    // Méthode pour rafraîchir l'affichage des cartes de produits dans la ListView
    private void refreshProductList() {
        productList = dbHelper.getAllProducts();

        if (txtHeaderCount != null) {
            txtHeaderCount.setText("Produits en stock (" + productList.size() + ")");
        }

        adapter = new ArrayAdapter<Product>(this, R.layout.item_product, productList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = MainActivity.this.getLayoutInflater().inflate(R.layout.item_product, parent, false);
                }

                TextView txtName = view.findViewById(R.id.txtProductName);
                TextView txtDescription = view.findViewById(R.id.txtProductDescription);
                TextView txtPrice = view.findViewById(R.id.txtProductPrice);

                Product p = productList.get(position);
                txtName.setText(p.getName());

                if (p.getDescription() != null && !p.getDescription().trim().isEmpty()) {
                    txtDescription.setText(p.getDescription());
                    txtDescription.setVisibility(View.VISIBLE);
                } else {
                    txtDescription.setVisibility(View.GONE);
                }

                txtPrice.setText(p.getPrice() + " €");

                return view;
            }
        };
        listViewProduits.setAdapter(adapter);
    }

    // Formulaire d'ajout ou de modification d'un produit
    private void showProductDialog(Product productToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product, null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtDescription = dialogView.findViewById(R.id.edtDescription);
        EditText edtPrice = dialogView.findViewById(R.id.edtPrice);

        boolean isEdit = (productToEdit != null);
        if (isEdit) {
            builder.setTitle("Modifier le produit");
            edtName.setText(productToEdit.getName());
            edtDescription.setText(productToEdit.getDescription());
            edtPrice.setText(String.valueOf(productToEdit.getPrice()));
        } else {
            builder.setTitle("Ajouter un nouveau produit");
        }

        builder.setPositiveButton(isEdit ? "Enregistrer" : "Ajouter", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();

            if (!name.isEmpty() && !priceStr.isEmpty()) {
                try {
                    double price = Double.parseDouble(priceStr.replace(',', '.'));

                    if (isEdit) {
                        productToEdit.setName(name);
                        productToEdit.setDescription(description);
                        productToEdit.setPrice(price);
                        dbHelper.updateProduct(productToEdit);
                        Toast.makeText(this, "Produit mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Product newProduct = new Product(0, name, description, price);
                        dbHelper.addProduct(newProduct);
                        Toast.makeText(this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                    }
                    refreshProductList();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Prix invalide, veuillez saisir un nombre valide", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Veuillez remplir au moins le nom et le prix", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Boîte de dialogue au clic sur un produit : propose "Modifier" ou "Supprimer"
    private void showOptionsDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(product.getName());
        builder.setMessage("Voulez-vous modifier ou supprimer ce produit ?");

        builder.setPositiveButton("Modifier", (dialog, which) -> showProductDialog(product));

        builder.setNegativeButton("Supprimer", (dialog, which) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation de suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer \"" + product.getName() + "\" ?")
                    .setPositiveButton("Oui, Supprimer", (confirmDialog, confirmWhich) -> {
                        dbHelper.deleteProduct(product.getId());
                        refreshProductList();
                        Toast.makeText(this, "Produit supprimé", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        builder.setNeutralButton("Annuler", null);
        builder.show();
    }
}
