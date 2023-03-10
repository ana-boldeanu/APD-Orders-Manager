// Boldeanu Ana-Maria
// APD - Tema 2 - Manager de comenzi

=============================== Descrierea solutiei ===============================

    Pentru implementarea procesatorului de comenzi, am folosit modelul Replicated
Workers cu 2 pooluri:
  * ordersPool - pentru threadurile de tip OrderTask;
  * productsPool - pentru threadurile de tip ProductTask.

    In sursa principala (Tema2.java), creez cei 2 ExecutorService si <nrThreads>
threaduri de tip Order. In plus, creez cate un Writer pentru fiecare fisier de
output (orders_out.txt si order_products_out.txt), si ii pasez threadurilor create.

    Fiecare thread de tip OrderTask:
    - are acces la cele 2 pooluri, la numarul de threaduri active din fiecare pool
 (AtomicInteger inQueue) si la cei 2 Writeri.
    - are un id unic (int orderIndex), care coincide cu indexul liniei pe care o va
 procesa, din fisierul orders.txt. Practic, ajung sa se creeze atatea threaduri
 Order cate linii sunt in fisier.
    - citeste fisierul orders.txt pana cand ajunge la linia asignata, de unde isi
 extrage numele comenzii (String name) si numarul de produse din ea (int nrProducts).
    - (doar daca nrProducts != 0) creeaza un semafor, pe care il initializeaza cu 
 <1 - nrProducts> permise. Semaforul este folosit pentru a "notifica" angajatul care
 se ocupa de comanda ca toate produsele din ea au fost livrate. Practic, fiecare
 thread OrderTask va astepta ca <nrProducts> threaduri ProductTask sa faca
 semaphore.release().
    - (doar daca nrProducts != 0) creeaza un numar de threaduri de tip ProductTask
 egal cu numarul de produse din comanda respectiva. Practic, fiecare thread
 ProductTask va cauta al i-lea produs care apartine comenzii.
    - cand toate threadurile Product pentru comanda respectiva si-au terminat
 executia, rescrie linia in fisierul de output, adaugand textul "shipped" la final.
    - adauga in ordersPool un nou thread OrderTask, care se va ocupa de linia cu
 indexul <orderIndex + nrThreads>.
    - daca indexul noului OrderTask depaseste numarul de linii din fisier, acesta
 pur si simplu isi va incheia executia, decrementand contorul ordersInQueue.

    Fiecare thread de tip ProductTask:
    - are acces la productsPool si contorul productsInQueue, la obiectul de tip
 orderTask care l-a creat, la semaforul acestuia si la Writerul aferent.
    - are un id unic (int productIndex), care coincide cu indexul produsului care
 trebuie  livrat din comanda.
    - citeste fisierul order_products.txt, cautand  produsele care fac parte din
 comanda sa. Incrementeaza un contor (int foundProducts) pentru fiecare produs
 gasit, avand scopul de a gasi al i-lea produs (cel care ii este asignat), pentru
 care scrie "shipped" in fisierul de out.
    - face semaphore.release() la final, decrementand contorul productsInQueue.

===================================================================================
