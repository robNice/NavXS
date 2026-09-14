# Informativa sulla privacy di NavXS

Ultimo aggiornamento: 14 settembre 2026

## 1. Informazioni generali

NavXS è un'applicazione Android che può visualizzare un overlay di navigazione con pulsanti fissi per le applicazioni selezionate.

Lo sviluppatore di questa applicazione raccoglie, memorizza o elabora dati personali solo nella misura tecnicamente necessaria per il funzionamento dell'applicazione.

## 2. Elaborazione locale sul dispositivo

NavXS elabora generalmente le informazioni localmente sul dispositivo.

Ciò include in particolare:

- il nome del pacchetto dell'applicazione attualmente in primo piano
- la selezione delle applicazioni per cui l'overlay deve essere attivato
- le impostazioni dell'overlay definite dall'utente, come posizioni, colori, dimensioni, trasparenza e temi
- lo stato di attivazione del servizio di accessibilità
- l'URI di un'immagine che l'utente seleziona facoltativamente come sfondo dell'editor di posizionamento, insieme all'opacità scelta per essa

## 3. Finalità dell'elaborazione

Queste informazioni vengono elaborate esclusivamente per:

- rilevare se l'overlay deve essere mostrato per l'applicazione attualmente aperta
- mostrare o nascondere l'overlay
- fornire le azioni di navigazione configurate
- salvare e ripristinare localmente le impostazioni selezionate dall'utente

## 4. Servizio di accessibilità

NavXS utilizza l'API del servizio di accessibilità di Android (AccessibilityService). NavXS non è uno strumento di accessibilità per persone con disabilità; il servizio è necessario per la funzione principale dell'app, l'overlay di navigazione.

### 4.1 Consenso

Prima di richiedere il servizio di accessibilità, NavXS mostra un'informativa che spiega come viene utilizzato il servizio. L'utente sceglie tra due pulsanti: «Rifiuta» e «Accetta». Solo dopo aver toccato «Accetta», NavXS apre le impostazioni di accessibilità di Android, dove l'utente deve attivare il servizio manualmente. «Rifiuta» chiude l'app senza attivare nulla. Uscire dall'informativa in qualsiasi altro modo non viene considerato un consenso. Il servizio può essere disattivato in qualsiasi momento nelle impostazioni di accessibilità di Android.

### 4.2 Per cosa viene utilizzato il servizio

Il servizio viene utilizzato esclusivamente per:

- rilevare il nome del pacchetto dell'app attualmente in primo piano
- decidere se l'overlay deve essere mostrato per quell'app
- mostrare i pulsanti di navigazione fluttuanti sopra le app selezionate
- eseguire le azioni di navigazione Indietro, Home e App recenti quando l'utente tocca il pulsante corrispondente

### 4.3 Cosa non fa il servizio

Tecnicamente il servizio di accessibilità ha accesso alle informazioni sulle finestre dell'app in primo piano. NavXS utilizza questo accesso esclusivamente per determinare il nome del pacchetto di quell'app. NavXS non legge, memorizza né trasmette testi digitati, password, contenuti dei messaggi o altri contenuti dello schermo. NavXS non esegue azioni per conto dell'utente, ad eccezione delle azioni di navigazione sopra indicate, e solo quando l'utente tocca un pulsante.

Questo servizio non viene utilizzato per scopi pubblicitari, di tracciamento o di analisi, e i dati non vengono né venduti né condivisi.

## 5. Nessuna trasmissione a server esterni

Attualmente, NavXS non trasmette le applicazioni selezionate, i nomi dei pacchetti delle applicazioni in primo piano né le impostazioni dell'overlay a server esterni o altri servizi esterni.

L'elaborazione avviene localmente sul dispositivo.

## 6. Archiviazione dei dati

Le impostazioni e le selezioni di applicazioni memorizzate da NavXS vengono salvate localmente sul dispositivo.

Questi dati rimangono sul dispositivo finché non vengono modificati dall'utente, rimossi cancellando i dati dell'applicazione, o eliminati disinstallando l'applicazione.

Se l'utente seleziona uno screenshot come sfondo dell'editor di posizionamento, NavXS memorizza solo l'URI di quell'immagine insieme al diritto di lettura concesso dal sistema. Il file immagine stesso non viene copiato, non viene salvato in una directory dell'app, non viene trasmesso al servizio di accessibilità e non viene inviato. Reimpostando lo sfondo, l'URI memorizzato viene eliminato e il diritto di lettura viene nuovamente rilasciato.

## 7. Autorizzazioni

NavXS richiede in particolare l'autorizzazione di accessibilità esplicitamente attivata dall'utente affinché l'applicazione possa fornire la sua funzionalità di overlay.

Inoltre, vengono utilizzati solo gli accessi tecnicamente necessari per il funzionamento dell'applicazione.

## 8. Terze parti

Attualmente, NavXS non utilizza servizi di terze parti, strumenti di analisi né pubblicità per elaborare i dati descritti sopra.

## 9. Modifiche a questa informativa sulla privacy

Questa informativa sulla privacy può essere aggiornata se necessario per riflettere le modifiche all'applicazione o i requisiti legali.

## 10. Contatto

Per qualsiasi domanda sulla privacy, è possibile contattare lo sviluppatore.

E-mail: navxs@robnice.de
