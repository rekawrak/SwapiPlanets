Выполнил: Вражкин Роман Евгеньевич (Б9123-09.03.03пикд(1))
Вариант: SWAPI-PLANETS-MOD_E22_DETAIL_BY_ID_ONLY

Endpoints API
Base URL: https://swapi.dev/api/
List: GET /planets/?page=1
Detail: GET /planets/{id}/

Стек и архитектура:
- Kotlin + Jetpack Compose + Coroutines
- Сеть: Retrofit + OkHttp (logging) + Moshi
- DI: Hilt
- Навигация: Jetpack Navigation (Navigation-Compose)

Слои:
- data: DTO (PlanetDto, PlanetListResponseDto), SwapiApi, PlanetRepositoryImpl
- domain: UI-модель Planet, интерфейс PlanetRepository
- ui: экраны списка и деталей (PlanetListScreen, PlanetDetailScreen), навигация (AppNavHost), общие компоненты (LoadingView, ErrorView, VariantCodeBanner), кнопка retry

Экран списка показывает первую страницу планет, экран деталей открывается по клику и делает отдельный запрос по id

Модификатор MOD_E22_DETAIL_BY_ID_ONLY
- Навигация в экран деталей идёт только через маршрут planet_detail/{planetId}
- В PlanetDetailViewModel planetId берётся из SavedStateHandle, объект планеты не передаётся через аргументы
- Детали всегда загружаются отдельным запросом getPlanetDetail(id) в репозитории

Скриншоты

![Главный экран](<main.png>)
![Карточка планеты](<planet-info.png>)
