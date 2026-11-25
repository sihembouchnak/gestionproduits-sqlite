package com.example.gestionproduitssqlite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView listViewProduits;
    private Button btnAjouterProduit;
    private Button btnOuvrirConvertisseur;
    private TextView txtHeaderCount;
    private ProductDatabaseHelper dbHelper;
    private ArrayAdapter<Product> adapter;
    private List<Product> productList = new ArrayList<>();

    // Instance de Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation de Firestore
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues et de la base de données locale SQLite
        listViewProduits = findViewById(R.id.listViewProduits);
        btnAjouterProduit = findViewById(R.id.btnAjouterProduit);
        btnOuvrirConvertisseur = findViewById(R.id.btnOuvrirConvertisseur);
        txtHeaderCount = findViewById(R.id.txtHeaderCount);
        dbHelper = new ProductDatabaseHelper(this);

        // Configuration de l'adaptateur ListView
        setupAdapter();

        // Charger et écouter les produits depuis Firestore en temps réel
        getProductsFromFirestore();

        // 1. Accès direct sans menu à 3 points : Ajouter Produit
        btnAjouterProduit.setOnClickListener(v -> showProductDialog(null));

        // 2. Accès direct sans menu à 3 points : Ouvrir Convertisseur TP7
        btnOuvrirConvertisseur.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConverterActivity.class);
            startActivity(intent);
        });

        // 3. Clic sur un produit : Boîte de dialogue pour Modifier ou Supprimer
        listViewProduits.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < productList.size()) {
                Product selectedProduct = productList.get(position);
                showOptionsDialog(selectedProduct);
            }
        });
    }

    private void setupAdapter() {
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

    // =========================================================================
    // Étape 2: OPÉRATIONS CRUD FIRESTORE POUR LES PRODUITS
    // =========================================================================

    // 1. Créer un document dans Firestore (Create)
    private void addProductToFirestore(String name, String description, double price) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("description", description);
        productMap.put("price", price);

        db.collection("products")
                .add(productMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Document ajouté avec succès", Toast.LENGTH_SHORT).show();
                    // Synchronisation locale SQLite
                    Product newProduct = new Product(0, name, description, price);
                    dbHelper.addProduct(newProduct);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur d'ajout : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 2. Lire des documents depuis Firestore (Read) en temps réel
    private void getProductsFromFirestore() {
        db.collection("products")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Erreur de lecture", e);
                        // Fallback sur SQLite en cas d'erreur / hors ligne
                        loadFromSQLite();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        productList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String docId = document.getId();
                            String name = document.getString("name");
                            String description = document.getString("description");
                            Double priceDouble = document.getDouble("price");
                            double price = (priceDouble != null) ? priceDouble : 0.0;

                            Product product = new Product(docId, name != null ? name : "", description, price);
                            productList.add(product);
                        }

                        if (txtHeaderCount != null) {
                            txtHeaderCount.setText("Produits en stock (" + productList.size() + ")");
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Fallback SQLite si besoin
    private void loadFromSQLite() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        if (txtHeaderCount != null) {
            txtHeaderCount.setText("Produits en stock (" + productList.size() + ")");
        }
        adapter.notifyDataSetChanged();
    }

    // 3. Mettre à jour un document dans Firestore (Update)
    private void updateProductInFirestore(Product product, String newName, String newDesc, double newPrice) {
        if (product.getDocumentId() != null) {
            DocumentReference personRef = db.collection("products").document(product.getDocumentId());
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("description", newDesc);
            updates.put("price", newPrice);

            personRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Document mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        if (product.getId() > 0) {
                            product.setName(newName);
                            product.setDescription(newDesc);
                            product.setPrice(newPrice);
                            dbHelper.updateProduct(product);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur de mise à jour : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si le produit n'a pas de documentId Firestore (issu de SQLite uniquement)
            product.setName(newName);
            product.setDescription(newDesc);
            product.setPrice(newPrice);
            dbHelper.updateProduct(product);
            loadFromSQLite();
            Toast.makeText(this, "Produit mis à jour dans SQLite", Toast.LENGTH_SHORT).show();
        }
    }

    // 4. Supprimer un document dans Firestore (Delete)
    private void deleteProductFromFirestore(Product product) {
        if (product.getDocumentId() != null) {
            DocumentReference personRef = db.collection("products").document(product.getDocumentId());
            personRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Document supprimé avec succès", Toast.LENGTH_SHORT).show();
                        if (product.getId() > 0) {
                            dbHelper.deleteProduct(product.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur de suppression : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si issu de SQLite uniquement
            dbHelper.deleteProduct(product.getId());
            loadFromSQLite();
            Toast.makeText(this, "Document supprimé de SQLite", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================================
    // Exemples d'implémentation modèle fournis dans la notice (Person / People)
    // =========================================================================

    private void addPerson() {
        Map<String, Object> person = new HashMap<>();
        person.put("firstName", "John");
        person.put("lastName", "Doe");
        person.put("age", 30);

        db.collection("people")
                .add(person)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Document ajouté avec succès", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur d'ajout : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getPeople() {
        db.collection("people")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        Long ageLong = document.getLong("age");
                        int age = (ageLong != null) ? ageLong.intValue() : 0;
                        Log.d("Firestore", "Nom: " + firstName + " " + lastName + ", Âge: " + age);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erreur de lecture", e);
                });
    }

    private void updatePerson(String documentId) {
        DocumentReference personRef = db.collection("people").document(documentId);
        personRef.update("age", 31)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Document mis à jour avec succès", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de mise à jour : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletePerson(String documentId) {
        DocumentReference personRef = db.collection("people").document(documentId);
        personRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Document supprimé avec succès", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de suppression : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // =========================================================================
    // DIALOGUES D'INTERACTION UTILISATEUR
    // =========================================================================

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
                        updateProductInFirestore(productToEdit, name, description, price);
                    } else {
                        addProductToFirestore(name, description, price);
                    }
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
                        deleteProductFromFirestore(product);
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        builder.setNeutralButton("Annuler", null);
        builder.show();
    }
}
