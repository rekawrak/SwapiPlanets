# Итоговый проект

ФИО: Вражкин Роман Евгеньевич  
Группа: Б9123-09.03.03пикд(1)

API: [SWAPI](https://swapi.dev/)

Клиент SWAPI, развитый до offline-first с персональными данными пользователя

---

## Что добавлено в финальной работе

- экран **Settings** (TTL кэша, фоновое обновление, локальные профили);
- экран **Recent** (история просмотренных планет);
- экран **Notes** + редактор заметок на деталке;
- **Planet Collections**, пользовательские коллекции;
- **Pinned planets**, до 5 закреплённых планет сверху списка;
- **Read/Unread**, статус изученности при открытии деталки;
- **Рейтинг 1-5**, локальная оценка планет;
- **offline-first кэш** планет в Room;
- **избранное с TTL**, stale-метками и фоновой синхронизацией через WorkManager;
- **локальные профили** (до 5) с раздельными данными и удалением профиля.

---

## Новые пользовательские данные

### Room


| Сущность                     | Таблица                   | Назначение                        |
| ---------------------------- | ------------------------- | --------------------------------- |
| `UserProfileEntity`          | `user_profiles`           | Локальные профили                 |
| `CachedPlanetEntity`         | `cached_planets`          | Кэш планет для offline            |
| `PlanetVisitEntity`          | `planet_visits`           | История просмотров (по profileId) |
| `PlanetNoteEntity`           | `planet_notes`            | Заметки к планетам                |
| `FavouritePlanetEntity`      | `favourite_planets`       | Избранное + `lastSyncedAtMs`      |
| `PlanetCollectionEntity`     | `planet_collections`      | Коллекции                         |
| `PlanetCollectionItemEntity` | `planet_collection_items` | Планеты в коллекции (FK cascade)  |
| `PlanetUserStateEntity`      | `planet_user_states`      | Read, pin, rating                 |


**Связи:** все персональные данные привязаны к `profileId`; коллекции связаны с планетами через `PlanetCollectionItemEntity`; активный профиль в DataStore (только id, не сами данные).

### DataStore (только настройки)


| Ключ                         | Назначение                    |
| ---------------------------- | ----------------------------- |
| `list_only_favourites`       | Фильтр только избранное       |
| `sort_names_desc`            | Сортировка AZ / ZA            |
| `cache_ttl_hours`            | TTL избранного (6/12/24/48 ч) |
| `background_refresh_enabled` | Фоновое обновление            |
| `active_profile_id`          | ID активного профиля          |


---

## Новые законченные сценарии

### 1. Offline-first каталог

**Слои:** `PlanetListViewModel` → `OfflineFirstPlanetRepository` → `PlanetCacheDao` → UI

1. Сначала читается кэш из Room, UI показывает данные с баннером offline
2. Затем сетевое обновление; при успехе кэш перезаписывается
3. Без сети список и деталка работают из кэша

### 2. Избранное с TTL и WorkManager (существенный)

**Слои:** `FavouritesViewModel` - `RoomFavouritesRepository` - `FavouritesSyncRepositoryImpl` - `FavouritesSyncWorker`

1. Добавление в избранное - `scheduleImmediateSync()`
2. TTL в Settings - stale-метка на экране избранного
3. Periodic WorkManager (6 ч, сеть) обновляет устаревшие записи
4. Избранное доступно офлайн из кэша

### 3. Planet Collections (новая сущность + связь)

**Слои:** `CollectionsViewModel` / `CollectionDetailViewModel` - `RoomPlanetCollectionRepository` - `PlanetCollectionDao`

1. Пользователь создаёт коллекции
2. Добавляет планеты с деталки через FilterChip
3. Данные хранятся в Room, разделены по profileId

### Дополнительные сценарии

- **Заметки:** деталка - Room - метка на списке + экран Notes
- **Recent:** деталка - `PlanetVisitEntity` - экран Recent (офлайн)
- **Профили:** создание/переключение/удаление, лимит 5, каскадное удаление данных
- **Pin / Read / Rating:** `PlanetUserStateEntity` влияет на порядок и отображение списка

---

## Offline-first архитектура

```
UI (List / Detail / Favourites / Notes / Recent / Collections)
        ↓
ViewModel (StateFlow, combine, debounce)
        ↓
Repository
   ├── OfflineFirstPlanetRepository (сеть + кэш)
   ├── RoomFavouritesRepository / RoomPlanetNotesRepository / …
   └── RoomUserProfileRepository
        ↓
Room DB + DataStore (настройки)
        ↑
Retrofit (SWAPI) - обновление, не единственный источник UI
```

**Принципы:** UI читает локальные данные первым; сеть - для обновления; при наличии кэша приложение остаётся полезным без интернета.

---

## WorkManager


| Компонент                      | Назначение                                                    |
| ------------------------------ | ------------------------------------------------------------- |
| `FavouritesSyncWorker`         | Hilt Worker, вызывает `runBackgroundSync()`                   |
| `FavouritesSyncWorkScheduler`  | Periodic (6 ч) + one-time задачи                              |
| `FavouritesSyncRepositoryImpl` | TTL-логика: какие id stale, обновление кэша, `lastSyncedAtMs` |
| `SettingsScreen`               | Вкл/выкл фонового обновления                                  |


**Триггеры:** старт приложения (periodic), добавление в избранное (immediate), изменение настройки в Settings.

---

## Тесты


| Файл                                   | Что проверяет                                |
| -------------------------------------- | -------------------------------------------- |
| `OfflineFirstPlanetRepositoryTest`     | кэш при падении сети, save в cache           |
| `RoomPlanetCacheRepositoryTest`        | TTL `isStale`, round-trip                    |
| `FavouritesSyncRepositoryImplTest`     | sync только stale, skip при выкл. фоне       |
| `RoomPlanetUserStateRepositoryTest`    | лимит 5 pin, read, rating                    |
| `InMemoryFavouritesRepositoryFlowTest` | Turbine на toggle                            |
| `PlanetListViewModelTest`              | loading/content/error, offline, pinned-first |
| `PlanetListViewModelFlowTest`          | поиск, onlyFavourites, pin feedback          |
| `PlanetDetailViewModelTest`            | favourite, visit, markAsRead                 |
| `NotesViewModelTest`                   | удаление заметки                             |
| `RoomPlanetCollectionRepositoryTest`   | CRUD операций с коллекциями и их содержимым  |
| `RoomPlanetNotesRepositoryTest`        | сохранение, удаление и получение заметок     |
| `RoomVisitHistoryRepositoryTest`       | запись визитов и получение истории           |


