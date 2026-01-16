# 📚 Système de Gestion de Bibliothèque

Application Java de gestion de bibliothèque développée avec Hibernate, JPA et SQLite.

## 🚀 Installation et Configuration

### Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- Git

### Étapes d'installation

Après avoir cloné le repository, suivez ces étapes dans l'ordre :

#### 1. Cloner le repository

```bash
git clone https://github.com/AppSketchArchitect/java-library
cd java-library
```

#### 2. Initialiser la base de données SQLite

**⚠️ IMPORTANT : Cette étape doit être exécutée en premier !**

Exécutez la classe `SQLiteInitScript` pour créer et initialiser la base de données :

```bash
# Via votre IDE (IntelliJ IDEA, Eclipse, etc.)
# Exécutez la classe : fr.appsketch.Core.SQLiteInitScript
```

Ou via Maven :

```bash
mvn exec:java -Dexec.mainClass="fr.appsketch.Core.SQLiteInitScript"
```

Cette étape crée le fichier `test.db` avec le schéma de base de données nécessaire.

#### 3. Installer les dépendances Maven

```bash
mvn clean install
```

Cette commande :
- Télécharge toutes les dépendances
- Compile le projet
- Exécute les tests
- Crée le fichier JAR

## 🎯 Utilisation

### Lancer l'application

#### Via votre IDE

Exécutez la classe principale :
```
fr.appsketch.MyLibrary
```

#### Via Maven

```bash
mvn exec:java -Dexec.mainClass="fr.appsketch.MyLibrary"
```

#### Via le JAR généré

```bash
java -jar target/java-library-1.0-SNAPSHOT.jar
```

### Menu principal

L'application propose deux modules principaux :

1. **📚 Gestion des Livres**
   - Ajouter, modifier, supprimer des livres
   - Rechercher par titre, auteur, catégorie
   - Lister les livres disponibles/empruntés
   - Emprunter et rendre des livres
   - Importer/exporter des livres (format JSON)

2. **👥 Gestion des Utilisateurs**
   - Ajouter, modifier, supprimer des utilisateurs
   - Rechercher par email, nom, prénom
   - Lister tous les utilisateurs

## 🧪 Tests

### Exécuter les tests

```bash
mvn test
```

### Voir la couverture de code

Après l'exécution des tests, un rapport de couverture JaCoCo est généré automatiquement.

Pour consulter le rapport :

1. Ouvrez le fichier suivant dans votre navigateur :
   ```
   target/site/jacoco/index.html
   ```

2. Le rapport affiche :
   - Couverture globale du projet
   - Couverture par package
   - Couverture par classe
   - Lignes couvertes/non couvertes

### Types de tests

Le projet contient plusieurs catégories de tests :

- **Tests unitaires des Managers** : `BookManagerTest`, `UserManagerTest`, `EmpruntManagerTest`
- **Tests unitaires des Repositories** : `BookRepositoryTest`, `UserRepositoryTest`, `EmpruntRepositoryTest`
- **Tests unitaires des Displays** : `BookDisplayTest`, `UserDisplayTest`
- **Tests d'intégration** : `HibernateManagerTest`
- **Tests de la classe principale** : `MyLibraryTest`

## 📦 Structure du projet

```
java-library/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── fr/appsketch/
│   │   │       ├── Book/          # Gestion des livres
│   │   │       ├── User/          # Gestion des utilisateurs
│   │   │       ├── Emprunt/       # Gestion des emprunts
│   │   │       ├── Core/          # Configuration Hibernate
│   │   │       ├── Displays/      # Interface utilisateur
│   │   │       └── MyLibrary.java # Classe principale
│   │   └── resources/
│   │       └── META-INF/
│   │           └── persistence.xml
│   └── test/
│       ├── java/
│       │   └── fr/appsketch/      # Tests unitaires et d'intégration
│       └── resources/
│           └── META-INF/
│               └── persistence.xml
├── target/
│   └── site/
│       └── jacoco/
│           └── index.html         # Rapport de couverture
├── pom.xml
├── test.db                        # Base de données SQLite
└── README.md
```

## 🛠️ Technologies utilisées

- **Java 17**
- **Maven** - Gestion des dépendances et build
- **Hibernate 6.6.4** - ORM (Object-Relational Mapping)
- **SQLite 3.51.1** - Base de données
- **JUnit 5** - Framework de tests
- **Mockito** - Mocking pour les tests
- **JaCoCo** - Couverture de code
- **Gson** - Sérialisation/Désérialisation JSON

## 📝 Fonctionnalités principales

### Gestion des Livres
- CRUD complet (Create, Read, Update, Delete)
- Recherche multi-critères (titre, auteur, catégorie, ISBN)
- Gestion des emprunts
- Import/Export JSON

### Gestion des Utilisateurs
- CRUD complet
- Validation des emails (unicité)
- Recherche par nom, prénom, email

### Gestion des Emprunts
- Association Utilisateur ↔ Livre
- Suivi de l'état (EN_COURS, TERMINE)
- Historique des emprunts
- Vérification de disponibilité

## 🔧 Configuration

### Base de données

La configuration de la base de données se trouve dans `src/main/resources/META-INF/persistence.xml`.

Par défaut, l'application utilise SQLite avec le fichier `test.db` à la racine du projet.

### Persistence Unit

Deux unités de persistence sont configurées :
- `LibraryPU` - Pour l'application principale
- `TestLibraryPU` - Pour les tests

## 📊 Rapport de couverture

Pour générer un nouveau rapport de couverture :

```bash
mvn clean test jacoco:report
```

Le rapport sera disponible dans `target/site/jacoco/index.html`

## 🐛 Dépannage

### Erreur "No Persistence provider for EntityManager"

Assurez-vous d'avoir exécuté `SQLiteInitScript` avant de lancer l'application.

### Erreur "test.db not found"

Exécutez la classe `SQLiteInitScript` pour créer la base de données.

### Tests en échec

Vérifiez que :
1. La base de données est initialisée
2. Les dépendances Maven sont installées (`mvn clean install`)
3. Vous utilisez Java 17 ou supérieur

## 📄 Licence

Ce projet est développé à des fins éducatives.

## 👨‍💻 Auteur

Développé dans le cadre d'un projet de gestion de bibliothèque avec architecture en couches.
Par ROMOLI Enzo et FALC'HUN Victor.

---

**Note** : Pour toute question ou problème, consultez d'abord ce README et vérifiez que toutes les étapes d'installation ont été suivies correctement.
