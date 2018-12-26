# Ringoid Android App
A charming dating service.

##### Clean Architecture that honors SOLID principles.

## Data

*Cloud* - performs network requests.

*Database* - stores data locally to Database.

*SharedPrefsManager* - stores data locally to Shared Preferences.

*InMemoryCache* - stores data locally in memory.

*Repository* - delivers correct data and makes sure that request for data has been performed.

* get data (fecth from *Cloud*, store to *Database*)
* notify data loaded / error has occurred
* handle synchoronous requests
* maintain *Queue* for async requests
* filter *Cloud* response with *Queue* and keep *Database* up-to-date
* schedule repeatable requests
* support exponential backoff for retry
* map results from *Dbo* and *Entity* to Domain-layer models
* decide when to fetch *Cloud* if data in *Database* has staled or empty

## Domain

*UseCase* - performs command to get data in separate thread and deliver it to thread.

* get input parameters and transform it
* perform data request to *Repository*
* do job in separate thread A
* deliver results to another thread B
* no lifecycle aware
* can be (un)subscribed and pass data / error upstream
* cancel job or ignore results upon unsubscribe
* neither reusable, nor bindable
* perform in thread pool with load balancing

## View

*ViewModel* - business logic for views.

* lifecycle aware, but outlives destroyed-then-recreated view
* receives requests from view
* is a subscriber to *UseCase*
* restore scroll and page positions, view state
* track time of app's restart thresholds and perform fresh start of related view accordingly
* indirectly show / update data on view
* show errors (statuses) on view
* hold *LiveData* (or similar), change it as data received and push that change to view, which is subscribed to that *LiveData*
* have all necessary *UseCase*s injected upon construction
* keep state of view using *LiveData*
* *onCleared()* - store view state (scroll, page positions, ...) to persistent storage

*View* - visual data presentation.

* subscriber to state *LiveData* (IDLE, LOADING, ERROR, PAGING, ...)
* on view state change (scroll, page, text typing, ...) - setting values to *ViewModel*
* ask *ViewModel* for data
* push user actions as *ActionObject*s and / or *Trigger*s
* observe changes on *LiveData* fields in *ViewModel*

## ActionObject, Trigger

*ActionObject* - object representing user's action: VIEW, OPEN_CHAT, LIKE, UNLIKE, BLOCK, REPORT, MESSAGE
*ActionObjectStorage* - holds *ActionObject*s to commit them later.

* hold internal queue of *ActionObject*s
* track creation time of last enqueued *ActionObject*
* track number of enqueued *ActionObject*s of each type
* handle triggers: pop queue and commit all
* handle errors and exponential backoff for retry, as well as recovery logic and discarding new *ActionObject*s upon failure
* trigger itself by: time elapsed since last *ActionObject* of some type, count of *ActionObject*s of some type has reached, size of queue has reached, internal clock alarm
* persist queue and restore it on app's restart

User's gesture happens on *View* -> passed to *ViewModel* -> *ActionObject* is created of some type.

If type is VIEW or similar - track that *ActionObject* until new *ActionObject* of the same type created.

Then enqueue that *ActionObject* to *ActionObjectStorage*.

Enqueue immediately for the other *ActionObject*'s types.

--------------
User's gesture happens on *View* -> passed to *ViewModel* -> *Trigger* is fired, notifying **ActionObjectStorage*.

## Navigation

*Navigator* - open screens.

* responsible for opening screens, including external screens, and pass parameters to it
* plug-in architecture: low-level interface is referenced by any screen on upper modules
* implementation resides on top-level module, which is aware of all screens

*Navigator* injects *ViewModel*.

*View* is willing to open new screen. It passes some params and *TAG* to *ViewModel*.

*ViewModel* calls *navigate(TAG, params)* on *Navigator*.

*FragNavController* - keep track an restore screens' history.

* hold cache of navigation history, persiste on app's restart
* when opened, each new screen *TAG* is recorded to history cache
* by persisted history queue (cache) - restore screen's sequence by *TAG*s
* on each restored screen *ViewModel* is then responsible for restoring data on view and view state
