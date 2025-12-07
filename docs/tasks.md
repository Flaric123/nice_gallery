# Задачи для Артема

### Общее

Все по старой схеме: создаешь форк, там пишеш свой код, кидаешь мне PR.

### 1. Реализовать FragmentSettings (папка fragments)

Тебе нужно получить список доступных хранилищ из тестового файлового менеджера:

```java
IManagerOfFiles managerOfFiles = new ManagerOfFiles_Test1(getContext());
List<ModelStorage> storages = managerOfFiles.getAllStorages();
```

И для каждого хранилища дать пользователю выбрать от 0 до n путей к папкам, в которых приложение будет искать медиафайлы, или поставить галочку полного сканирования. Типо того:

```txt
Внутренее хранилище                 (сканировать все: нет)
 - /internal_storage/DCIM/Images
 - /imternal_storage/DCIM/Movies
SD карта                            (сканировать все: нет)
 - /internal_storage/DCIM/Pictures
USB накопитель                      (сканировать все: да)
```

Пусть пользователь просто вводит пути к папкам как строку. Получившийся список тебе надо перенести на модель `ModelScanList` (папка models) и сохранить его в тестовых настройках:

```java
IManagerOfSettings managerOfSettings = new ManagerOfSettngs_Test1(getContext());
managerOfSettings.saveScanList(yourScanList);
```

Используй именно тестовые реализации `ManagerOfFiles_Test1`, `ManagerOfSettngs_Test1` и интерфейсы `IManagerOfFiles`, `IManagerOfSettings`, чтобы потом можно было легко заменить их на настоящие, в твоем коде.

### 2. Реализовать ActivityFilters (папка activities)

Тебе нужно реализовать интерфейс для заполнения модели `ModelFilters` (папка models), заполнить модельку и сохранить ее в настройках:

```java
IManagerOfSettings managerOfSettings = new ManagerOfSettngs_Test1(getContext());
managerOfSettings.saveFilters(yourFilters);
```

Так же нужно предусмотреть кнопку "Применить" для сохранения и кнопку "Сбросить", при нажатии просто передаешь `managerOfSettings.saveFilters(null)`.

### 3. Начинай писать ПЗ (папка activities)

Главы:
- (1) Актуальность и значимость.
- (2) Тех. задание. Возьми из описания приложения в папке проекта, распиши подробнее свои компоненты.
- (5) Детальное проектирование. Опиши здесь модели из папки models, и нарисуй схему переходов по активити и фрагментам.
- (6) Реализация. Напиши что-нибудб про Android и Android Studio.
- (7) Тестирование. Протестируй свой компоненты.

Пока не оформляй особо, главное - информация, схемы, скрины.
