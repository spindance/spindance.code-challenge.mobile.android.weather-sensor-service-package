 //
 // SpinDance
 // Weather Sensor Service
 //
 // Copyright © 2022 SpinDance. All rights reserved.
 //

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Timer
import kotlin.concurrent.*
import kotlin.random.Random
import java.time.LocalDateTime

private class Constants {
    companion object {
        val READING_INTERVAL: UInt = 1U
        val TEMPERATURE_RANGE: ClosedRange<Double> = -40.0..40.0
        val HUMIDITY_RANGE: ClosedRange<Double> = 0.0..100.0
        val PRESSURE_RANGE: ClosedRange<Double> = 95.0..105.0
    }
}

class MockWeatherSensorReader: WeatherSensorReaderType {
     
    private var timer: Timer? = null

    private var waveLocation: Int = 1

    override var readingInterval: UInt = Constants.READING_INTERVAL
        
    private val flow: MutableSharedFlow<WeatherSensorReading> = MutableSharedFlow<WeatherSensorReading>(replay = 1)
    override var sensorReaderFlow: SharedFlow<WeatherSensorReading> = flow.asSharedFlow()
    
    override fun set(readingInterval: UInt) {
        if (readingInterval != this.readingInterval && readingInterval >= 1U && readingInterval <= UInt.MAX_VALUE) { 
            this.readingInterval = readingInterval 
            if (timer != null) {
                startSensorReadings()
            }
        }
    }

    override fun startSensorReadings() {
        stopSensorReadings()

        // Report the first reading immediately, then start the timer
        reportSensorReadings()

        timer = fixedRateTimer(name="SensorReadingsTimer", daemon = false, initialDelay = 0L, period = (readingInterval * 1000U).toLong()){
            reportSensorReadings()
        }
     }

    override fun stopSensorReadings() {
        timer?.cancel()
        timer = null
     }

     private fun reportSensorReadings() {
         flow.tryEmit(
             WeatherSensorReading(
                GenerateTemperatureReadings((waveLocation/100.0),Constants.TEMPERATURE_RANGE),
                GenerateTemperatureReadings((waveLocation/100.0),Constants.HUMIDITY_RANGE),
                GenerateTemperatureReadings((waveLocation/100.0),Constants.PRESSURE_RANGE),
                LocalDateTime.now()
             )
         )
         if ( waveLocation === 100) {
             waveLocation = 1
            }
        waveLocation++
     }
 }
