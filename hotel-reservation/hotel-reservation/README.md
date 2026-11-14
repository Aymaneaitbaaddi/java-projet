# Hotel Reservation — Projet final POO Java (M1)

Application console de gestion des réservations d'un hôtel : recherche de chambres
disponibles, calcul du prix d'un séjour, réservation et annulation.

## 1. Objectif de l'application

L'hôtel possède trois types de chambres (simple, double, suite), chacune avec sa propre
règle de prix. L'application applique les règles métier d'un vrai hôtel :

- une chambre ne peut pas être réservée deux fois sur des dates qui se chevauchent ;
- le nombre de personnes ne peut pas dépasser la capacité de la chambre ;
- la date de départ doit être après la date d'arrivée (au moins une nuit) ;
- le prix dépend du type de chambre, du nombre de personnes, du petit-déjeuner
  et de la durée du séjour (remise long séjour) ;
- seule une réservation confirmée peut être annulée, et une réservation annulée libère la chambre.

## 2. Comment lancer l'application

Prérequis : **JDK 17 ou plus** et **Maven** (IntelliJ inclut Maven).

**Dans IntelliJ** : ouvrir le dossier du projet (File → Open → choisir le dossier contenant
`pom.xml`), attendre le chargement Maven, puis lancer la méthode `main` de
`com.hotel.Application` (flèche verte ▶).

**En ligne de commande** :

```bash
mvn compile exec:java
```

Par défaut, les chambres sont chargées depuis `src/main/resources/rooms.csv`.
Pour utiliser un autre fichier, passer son chemin en argument :

```bash
mvn compile exec:java -Dexec.args="chemin/vers/autre-fichier.csv"
```

## 3. Fonctionnalités

Menu console :

1. Lister toutes les chambres
2. Rechercher les chambres disponibles (dates + nombre de personnes)
3. Réserver une chambre (le prix est affiché et doit être confirmé avant la réservation)
4. Annuler une réservation
5. Afficher les réservations d'un client (par email)
6. Afficher toutes les réservations et le chiffre d'affaires prévu

### Règles de prix

| Type | Prix du séjour |
|---|---|
| `SingleRoom` | prix/nuit × nuits + petit-déjeuner en option (12 €/personne/nuit) |
| `DoubleRoom` | prix/nuit × nuits + 15 €/nuit si 2 personnes + petit-déjeuner en option |
| `Suite` | (prix/nuit + 30 € de frais de service) × nuits, petit-déjeuner **inclus** (jamais facturé) |

Remise long séjour : **-10 % à partir de 7 nuits**, appliquée au prix total.

## 4. Organisation du code

```
src/main/java/com/hotel/
├── Application.java          point d'entrée : assemble les objets et lance le menu
├── model/                    les entités du domaine
│   ├── Room (abstraite), SingleRoom, DoubleRoom, Suite, RoomType (enum)
│   ├── Customer
│   └── Reservation, ReservationStatus (enum)
├── pricing/                  les politiques de remise
│   ├── DiscountPolicy (interface)
│   └── NoDiscount, LongStayDiscount
├── repository/               le stockage des données
│   ├── RoomRepository, ReservationRepository (interfaces)
│   └── InMemoryRoomRepository, InMemoryReservationRepository
├── loader/                   le chargement du fichier
│   ├── RoomCsvLoader
│   └── RoomFactory
├── service/                  la logique métier
│   ├── ReservationService
│   └── PricingService
├── exception/                les exceptions personnalisées
└── ui/
    └── ConsoleMenu           interaction avec l'utilisateur
src/main/resources/rooms.csv  données initiales
src/test/java/com/hotel/      tests JUnit 5
```

### Classes principales

| Classe | Rôle |
|---|---|
| `Room` | classe abstraite commune à toutes les chambres : numéro, prix, capacité, `calculatePrice()` abstraite |
| `Reservation` | une réservation : client, chambre, dates, statut ; sait calculer ses nuits et détecter un chevauchement |
| `ReservationService` | règles métier : disponibilité, réservation, annulation, devis, chiffre d'affaires |
| `PricingService` | prix final = prix de la chambre (polymorphisme) + politique de remise |
| `RoomCsvLoader` | lit et valide le fichier CSV, crée les chambres via `RoomFactory` |
| `ConsoleMenu` | lit les saisies, affiche les résultats et les messages d'erreur |

## 5. Où sont utilisés les concepts du cours

### Héritage
`SingleRoom`, `DoubleRoom` et `Suite` **étendent** `Room` (relation IS-A : une suite *est une*
chambre). Le numéro, le prix, la capacité, la validation et le calcul du petit-déjeuner sont
écrits une seule fois dans `Room`.

### Polymorphisme
- `PricingService.calculateTotal()` appelle `room.calculatePrice(...)` sur une variable de type
  `Room` : c'est la méthode de l'objet réel (simple, double ou suite) qui s'exécute.
- `ReservationService` manipule une `List<Room>` sans jamais tester le type concret
  (aucun `instanceof`).
- `PricingService` utilise une `DiscountPolicy` sans savoir laquelle.

### Classe abstraite et interface
- **`Room` est une classe abstraite** car toutes les chambres partagent un état (numéro, prix,
  capacité), un constructeur avec validation et du code commun, mais une « chambre générique »
  n'existe pas : chaque type doit définir sa règle de prix.
- **`DiscountPolicy` est une interface** car une remise est un comportement interchangeable,
  sans état commun. On peut ajouter une nouvelle remise sans modifier le code existant
  (pattern **Strategy**).
- **`RoomRepository` et `ReservationRepository` sont des interfaces** pour que les services ne
  dépendent pas de la façon dont les données sont stockées.

### Exceptions personnalisées

| Exception | Type | Quand |
|---|---|---|
| `RoomNotAvailableException` | checked | la chambre est déjà réservée sur une partie de la période |
| `RoomNotFoundException` | checked | le numéro de chambre n'existe pas |
| `ReservationNotFoundException` | checked | l'identifiant de réservation n'existe pas |
| `DataLoadingException` | checked | fichier introuvable, ligne mal formée, type inconnu, numéro en double |
| `InvalidReservationException` | unchecked | dates invalides, capacité dépassée, double annulation |

Exceptions standard utilisées : `IllegalArgumentException` (valeurs invalides dans les
constructeurs), `IllegalStateException`, `IOException`, `NumberFormatException`,
`DateTimeParseException` (saisie de date invalide, redemandée à l'utilisateur).

Les exceptions métier sont lancées dans les services et **attrapées dans `ConsoleMenu`**,
où l'on peut afficher un message utile à l'utilisateur.

### Chargement des données
Au démarrage, `Application` appelle `RoomCsvLoader`, qui lit `rooms.csv` :

```
number,type,pricePerNight,capacity
101,SINGLE,60.0,1
202,SUITE,180.0,4
```

Chaque ligne est découpée, convertie (`Integer.parseInt`, `Double.parseDouble`,
`RoomType.valueOf`), puis transformée en objet par `RoomFactory`. Une ligne invalide provoque
une `DataLoadingException` indiquant le numéro de la ligne. Si le fichier ne peut pas être
chargé, l'application affiche la raison et s'arrête.

### Collections et fonctionnalités Java
- `Map<Integer, Room>` (`TreeMap`) : accès rapide à une chambre par son numéro, triées.
- `Map<String, Reservation>` (`LinkedHashMap`) : réservations dans l'ordre de création.
- `Set<Integer>` : détection des numéros de chambre en double dans le CSV.
- `Optional` : `findByNumber()` et `findById()` ne renvoient jamais `null`.
- Streams et lambdas : recherche des chambres disponibles, filtrage par client, calcul du chiffre d'affaires.
- `enum` : `RoomType`, `ReservationStatus`.
- `LocalDate` et `ChronoUnit` : dates de séjour et nombre de nuits.

## 6. Principes SOLID

- **S — Responsabilité unique** : chaque classe a un seul rôle. `RoomCsvLoader` lit le fichier,
  les repositories stockent, `PricingService` calcule les prix, `ReservationService` applique les
  règles de réservation, `ConsoleMenu` gère l'affichage, `Application` assemble les objets.
  Il n'y a pas de « God class ».
- **O — Ouvert/fermé** : pour ajouter une remise (fidélité, saison…), on crée une nouvelle classe
  qui implémente `DiscountPolicy`, sans modifier `PricingService`. Pour un nouveau type de
  chambre, on crée une sous-classe de `Room` : le calcul du prix et la recherche fonctionnent
  sans modification (seuls l'enum `RoomType` et la fabrique doivent connaître le nouveau type).
- **L — Substitution de Liskov** : toute sous-classe de `Room` peut être utilisée partout où une
  `Room` est attendue. Aucune ne refuse une opération du parent ni ne lance d'exception inattendue.
- **I — Ségrégation des interfaces** : les interfaces sont petites et ciblées
  (`DiscountPolicy` a deux méthodes, chaque repository ne contient que ce dont les services ont besoin).
- **D — Inversion des dépendances** : `ReservationService` dépend des interfaces `RoomRepository`
  et `ReservationRepository`, et `PricingService` de l'interface `DiscountPolicy`. Les
  implémentations concrètes sont choisies et injectées par le constructeur dans `Application`.

## 7. Choix techniques

1. **`Room` abstraite plutôt qu'interface** : les chambres partagent un état et une validation
   (numéro, prix, capacité). Une classe abstraite évite de dupliquer ce code dans chaque type,
   tout en obligeant chaque sous-classe à définir son prix.
2. **Remises avec le pattern Strategy** : la politique de remise est injectée dans
   `PricingService`. Changer de règle commerciale ne demande de modifier qu'une ligne dans
   `Application`, et chaque remise se teste séparément.
3. **Le jour de départ n'est pas une nuit** : deux séjours se chevauchent si chacun commence
   avant la fin de l'autre (comparaison stricte). Ainsi, un client peut arriver le jour où le
   précédent part, comme dans un vrai hôtel.
4. **Exceptions checked pour les situations métier attendues, unchecked pour les demandes
   invalides** : une chambre occupée est une situation normale que l'appelant doit gérer
   (proposer d'autres dates) ; des dates incohérentes sont une erreur de saisie rejetée
   immédiatement (fail fast).
5. **Le devis vérifie les mêmes règles que la réservation** : le prix n'est jamais affiché pour
   un séjour qui serait refusé (chambre occupée, trop de personnes).

## 8. Lancer les tests

**Dans IntelliJ** : clic droit sur `src/test/java` → *Run 'All Tests'*.

**En ligne de commande** :

```bash
mvn test
```

Les tests (JUnit 5) couvrent la logique métier :

| Classe de test | Ce qui est testé |
|---|---|
| `RoomPricingTest` | prix de chaque type de chambre, petit-déjeuner, polymorphisme, validation |
| `ReservationTest` | nombre de nuits, chevauchement des dates (cas limites), annulation |
| `CustomerTest` | égalité par email (`equals`/`hashCode`), validation du nom et de l'email |
| `LongStayDiscountTest` | remise avant, à partir de et après 7 nuits, paramètres invalides |
| `RoomCsvLoaderTest` | fichier valide, type inconnu, nombre invalide, colonne manquante, doublon, fichier absent |
| `ReservationServiceTest` | réservation, conflit de dates, arrivée le jour du départ, capacité, annulation, devis, chiffre d'affaires |

Chaque comportement est testé dans les trois situations : cas normal, cas limite et cas d'erreur.
