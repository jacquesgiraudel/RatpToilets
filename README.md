Strategy

I used the standard MVVM strategy using compose / view model / repository / retrofit. As the api logic is simple, I have used LiveData rather than Flows.

TODO

 - Add navigation

ListToiletsScreen

 - add preview wrapper to show the different state of the composables

 ListToiletsViewModel

 - a better equals for the Result class based on the content of the 2 lists (do not trigger a recomposition if the elements of the 2 lists are the same)
 - remove the geolocation data provider from the view model, consider it as another data source provider

 ToiletsRepository

 - add dao in constructor parameter to ease testing / hilt
 - find a clean way to remove the background execution instruction from the repo to the dao, not the repo responsibility