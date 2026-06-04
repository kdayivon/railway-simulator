# Objectifs du projet

## Objectif technique

L'objectif technique de ce projet est la réalisation d'un simulateur de
réseau ferroviaire modulable déterministe. Ce projet se veut être une
vitrine technique stricte des paradigmes de la programmation orientée
objet, de la concurrence en Java, et de l'architecture réactive.
L'aspect du design n'est ici présent que pour illustrer et

Il s'agit d'une simulation multi-threadée, conçue sans l'aide de
frameworks externes et s'appuyant uniquement sur du Java pur (Java 25).
Les limitations et règles de conception imposées garantissent une
stabilité et préviennent tout risque de blocage. Le projet utilise aussi
notamment la séparation des ressources métiers, ce qui rend les
fonctionnalités très modulables.

1.  **Concurrence et Threads** : Utilisation des Virtual Threads pour la
    gestion des trains.

2.  **Horloge Physique** : L'utilisation de `System.nanoTime()` sert aux
    calculs de cinématique ($d = v \cdot \Delta t$) de manière continue
    dans le thread de chaque train, évitant ainsi les sauts de physique
    liés à d'autres méthodes d'horloge.

3.  **Topologie du Réseau** : L'architecture privilégie les segments par
    rapport aux nœuds. Les aiguillages sont gérés comme des segments de
    voies partageant.

4.  **Séparation des Responsabilités** : Principe de conception visant à
    segmenter un programme informatique en plusieurs parties, afin que
    chacune d'entre elles isole et gère un aspect précis de la
    problématique générale

## Objectif pédagogique

L'apport pédagogique principal réside dans la maîtrise de la concurrence
avancée et de la sécurité des threads (thread-safety) en Java. Le projet
pousse à utiliser des mécanismes modernes et sûrs tels que les
`CompletableFuture`, les `Observers`/`Listeners`, les `Interfaces` et
les `Semaphore`. Il forme également à la conception d'architectures
découplées où les composants communiquent de manière asynchrone sans
créer de goulets d'étranglement.

# Développement technique

La conception du diagramme de classes reprenant le domaine de ce projet
fut une étape clé à la réussite de celle-ci.


\
L'architecture de ce simulateur s'articule autour de plusieurs concepts
clés et d'un protocole de transfert rigoureux.

## Le Protocole de Transfert

Pour éviter les collisions et les attentes actives, le déplacement des
trains est régi par les étapes suivantes :

- **Pré-enregistrement** : Sur la voie principale, le train appelle
  `StationMaster`. Le chef de gare ajoute un `CompletableFuture` à sa
  file et incrémente un sémaphore. Le \"Future\" est renvoyé au train.

- **Mise en attente (Staging)** : Arrivé sur un segment de type
  `STAGING`, le train appelle `future.join()`, ce qui bloque de manière
  optimale son Virtual Thread.

- **Allocation de Route** : Le `StationMaster` consomme un permis du
  sémaphore. Il parcourt la file et dès qu'une route est libre, il
  acquiert de manière atomique tous les sémaphores des segments, met à
  jour les états sur `RESERVED`, retire le \"Future\" et appelle bloque
  cette route pour le train.

- **Mouvement et Libération** : Le train se réveille et avance. Lorsque
  l'arrière du train quitte un segment, un événement est émis pour les
  `Listeners`. Le `StationMaster` libère alors le sémaphore
  correspondant.

## Conception Graphique et Rendu

Le rendu visuel est effectué via JavaFx. L'esthétique respecte un
système de grille stricte (routage octilinéaire) où les lignes ne
peuvent être dessinées qu'à 0°, 45°, 90° ou 135°, inspiré du design de
schéma de métros popularisé par Henry Beck.


- Les voies parallèles maintiennent un espacement constant.

- Les gares sont des nœuds représentés par des rectangles sur un Z-index
  supérieur, où les lignes passent sans se courber.

- Les aiguillages (ou switchs) sont des petits cercles solides placés
  exactement à l'intersection mathématique des segments. Ces switchs,
  bien qu'ils héritent de `Node`, ne se comporte pas comme des
  `Stations` (gares) car les trains ne peuvent s'arrêter à ces endroits
  là.


## Algorithme de Parcours : Dijkstra

L'idée est de maintenir la distance de la source donnée vers tous les
sommets à l'aide d'un tableau `dist`. Le tableau des distances est
initialisé à l'infini pour tous les sommets, et à 0 pour la source
donnée. Nous maintenons également deux ensembles :Un ensemble contient
les sommets inclus dans l'arbre des plus courts chemins.L'autre ensemble
comprend les sommets qui ne sont pas encore inclus dans l'arbre des plus
courts chemins.À chaque étape de l'algorithme, on cherche un sommet qui
se trouve dans l'autre ensemble (l'ensemble des sommets non encore
inclus) et qui possède une distance minimale par rapport à la source.
Une fois qu'un sommet est choisi, nous mettons à jour la distance de ses
sommets adjacents si nous obtenons un chemin plus court en passant par
lui.

# Gestion du projet

## Conception

Ce projet s'accompagne d'une liste de bonnes pratiques d'architecture à
internaliser et respecter, tout en forçant l'apprentissage de nouveaux
outils très intéressants intégrés à Java depuis quelques mise à jour
LTS:

1.  **Utilisation de Record** pour éviter la verbosité lors de la
    création de classes immuables (pas de méthode set())

2.  **Utilisation de Semaphore** pour éviter les blocages, en donnant un
    nombre limité de \"permis\" d'accès à une même ressource par les
    `Threads`. Cela a été très utile pour bloquer les trains lorsque des
    `Segments` sont utilisés.

3.  **Interdiction des frameworks externes** pour rester concentré sur
    le cœur de la technologie Java.

La séparation des outils métiers fait que l'on pourrait changer ce
programme en un réseau routier par exemple, où les trains sont des bus
et les rails sont des autoroutes.

## Bonnes et mauvaises pratiques

1.  Travailler sous de telles contraintes force à privilégier la
    consistance et la planification sur l'improvisation. Toute
    modification nécessitait une justification poussée pour ne pas
    outrepasser les limites architecturales imposées.

2.  Utilisation de l'IA pour les commentaires javadoc de
    classes/méthodes diverses, cela sert lorsque l'on revient sur le
    projet après un certain temps. Elle a été aussi utilisée pour aider
    à la conception des méthodes `findShortestPath()` et `draw()`.

# Bilan du projet

En conclusion, la réalisation de ce simulateur déterministe démontre
l'efficacité des `Virtual Threads` de Java 25 couplés à une architecture
réactive bien pensée. L'utilisation systématique de mécanismes atomiques
(`CompletableFuture`, `Semaphore`) a permis de créer une simulation
fluide et robuste, dénuée de blocages. Ce projet, bien que purement
pédagogique et non destiné à être distribué, constitue une base solide
sur les problématiques de gestion de flux et de concurrence. Il demeure
cependant, plusieurs points qui prêtent à une amélioration future.
Notamment, l'instanciation manuel des stations, switches, segments et
trains, qui pourrait en elle-même constituer un programme à part
entière.
