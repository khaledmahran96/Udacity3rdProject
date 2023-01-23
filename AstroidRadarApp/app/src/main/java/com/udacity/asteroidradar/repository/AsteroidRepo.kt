package com.udacity.asteroidradar.repository

import android.os.Build
import android.util.Log
import android.view.animation.Transformation
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidApiServer
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsteroidRepo(private val database: AsteroidDatabase) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val startDate =LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val endDate = startDate.plusDays(7)

    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()){
        it.asDomainModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val asteroidsToday: LiveData<List<Asteroid>> = Transformations
        .map(database.asteroidDao.getAsteroidsDay(startDate.format(DateTimeFormatter.ISO_DATE))){
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val asteroidsWeek: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroidsDate(
        startDate.format(DateTimeFormatter.ISO_DATE),
        endDate.format(DateTimeFormatter.ISO_DATE))){
        it.asDomainModel()
    }

    suspend fun refreshAsteroids(){
        withContext(Dispatchers.IO){
            try {
                val listOfAsteroids = AsteroidApi.retrofitService.getAsteroids(API_KEY)
                val asteroids = parseAsteroidsJsonResult(JSONObject(listOfAsteroids))
                database.asteroidDao.insertAll(*asteroids.asDatabaseModel())
            }catch (e: Exception){
                Log.e("Data" , e.printStackTrace().toString())
            }
        }
    }
}