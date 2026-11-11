# 📱 Application Android : Gestion de Produits SQLite & Convertisseur de Monnaie (TP7)

Une application Android native développée en **Java** et **XML** sous **Android Studio**, combinant la gestion de base de données locale SQLite (CRUD) et un convertisseur de devises interactif avec navigation par Intent implicite.

---

## 🌟 Fonctionnalités Principales

### 📦 1. Gestionnaire de Produits (SQLite - CRUD)
- **Ajout de Produit** : Formulaire dans un `AlertDialog` personnalisé pour saisir le nom, la description et le prix d'un produit avec validation des données (conversion intelligente du point/virgule).
- **Affichage dynamique** : Liste des produits sous forme de cartes Material Design (`MaterialCardView`) avec badges de prix verts et avatars d'icônes.
- **Modification & Suppression** : Lors du clic sur n'importe quel produit, une boîte de dialogue s'affiche pour :
  - **Modifier** le produit (formulaire pré-rempli).
  - **Supprimer** le produit de la base de données SQLite avec confirmation.
- **Persistance des données** : Utilisation de `SQLiteOpenHelper` (`ProductDatabaseHelper`) pour sauvegarder les données localement dans `products.db`.

### 💱 2. Convertisseur de Monnaie (TP7)
- **Conversion locale (TND ↔ USD)** :
  - **TND ➔ USD** (Dinar vers Dollar) : `Résultat = Montant / 3.1`
  - **USD ➔ TND** (Dollar vers Dinar) : `Résultat = Montant * 3.1`
- **Gestion des Erreurs (`showAlert`)** :
  - Affichage d'un `AlertDialog` d'avertissement lorsque la zone de saisie du montant est vide ou invalide.
- **Intent Implicite (Conversion TND ➔ DZD en ligne)** :
  - Bouton ouvrant directement le navigateur Web vers la page exacte de XE.com :
    `https://www.xe.com/fr/currencyconverter/convert/?Amount=1&From=TND&To=DZD`

---

## 🎨 Design & Ergonomie (Material 3)

- **Palette de couleurs moderne** :
  - Couleur Principale : Bleu Roi (`#2563EB`)
  - Couleur Secondaire : Vert Émeraude (`#059669`)
  - Arrière-plan : Gris Slate Clair (`#F1F5F9`)
- **Navigation fluide sans menu 3 points** :
  - Remplacement du menu d'options à 3 points par des boutons d'accès direct sur l'écran d'accueil (`Ajouter Produit` et `Convertisseur`).
  - Bouton de retour **`←`** personnalisé dans le convertisseur.

---

## 📂 Structure du Projet

```
GestionProduitsSQLite/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/gestionproduitssqlite/
│   │   ├── MainActivity.java            # Écran principal (Liste des produits & Actions)
│   │   ├── ConverterActivity.java       # Écran du Convertisseur TP7 & Intent Implicite
│   │   ├── Product.java                 # Modèle de données Produit
│   │   └── ProductDatabaseHelper.java   # Gestionnaire SQLite (CRUD)
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml        # Interface de la liste des produits
│       │   ├── activity_converter.xml   # Interface du convertisseur de devises
│       │   ├── dialog_product.xml       # Formulaire d'ajout / modification
│       │   └── item_product.xml         # Carte personnalisée de produit
│       ├── drawable/
│       │   ├── bg_circle_icon.xml       # Fond circulaire d'icône
│       │   ├── bg_price_badge.xml       # Badge de prix vert
│       │   └── bg_edittext.xml          # Bordure des champs texte
│       └── values/
│           ├── colors.xml               # Palette de couleurs du projet
│           ├── strings.xml              # Ressources textuelles
│           └── themes.xml               # Thème Material 3 (NoActionBar)
└── README.md
```

---

## 💻 Configuration & Installation

1. Clonez ou ouvrez le projet dans **Android Studio**.
2. Assurez-vous que le SDK Android (API 24 minimum) est configuré.
3. Lancez la synchronisation Gradle (`Sync Project with Gradle Files`).
4. Exécutez l'application sur un émulateur Android ou un appareil réel (`Run 'app'`).

---

## 📝 Auteur & Contexte
Projet réalisé dans le cadre des travaux pratiques (TP7) de développement d'applications mobiles Android.
