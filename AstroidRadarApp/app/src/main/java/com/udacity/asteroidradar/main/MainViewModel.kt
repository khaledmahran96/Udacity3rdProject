package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.FilterMenuAsteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidRepo = AsteroidRepo(database)

    private val _pictureOfTheDay = MutableLiveData<PictureOfDay>()
    val pictureOfTheDay: LiveData<PictureOfDay>
        get() = _pictureOfTheDay

    private val _navToDetail = MutableLiveData<Asteroid?>()
    val navToDetail: LiveData<Asteroid?>
        get() = _navToDetail

    init {
        viewModelScope.launch {
            asteroidRepo.refreshAsteroids()
            refreshPicOfTheDay()
        }
    }

    private val _filter = MutableLiveData(FilterMenuAsteroid.ALL)

    @RequiresApi(Build.VERSION_CODES.O)
    val listAsteroid =Transformations.switchMap(_filter){
        when (it){
            FilterMenuAsteroid.WEEK -> asteroidRepo.asteroidsWeek
            FilterMenuAsteroid.DAY -> asteroidRepo.asteroidsToday
            else -> asteroidRepo.asteroids
        }
    }


    fun onAsteroidCLicked(asteroid: Asteroid){
        _navToDetail.value = asteroid
    }

    fun onAsteroidNavigated(){
        _navToDetail.value = null
    }

    fun onFilterSelect(filter: FilterMenuAsteroid){
        _filter.postValue(filter)
    }

    private suspend fun refreshPicOfTheDay(){
        withContext(Dispatchers.IO){
            try {
                _pictureOfTheDay.postValue(AsteroidApi.retrofitService.getPictureOfTheDay(API_KEY))
            }catch (e: Exception){
                Log.e("pictureOfTheDay" , e.printStackTrace().toString())
            }
        }
    }

}