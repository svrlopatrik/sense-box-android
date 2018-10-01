package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.models.states.DetailActivityState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.ValueInterpreter
import timber.log.Timber
import javax.inject.Inject

class DetailActivityViewModel @Inject constructor(
        private val recordDao: RecordDao
) : BaseViewModel() {

    private val detailActivityState = SingleLiveEvent<DetailActivityState>()
    private val loadedRecords = MutableLiveData<List<Record>>() //note: must be of type MutableLiveData to provide data when observed

    override fun onViewCreated(savedInstanceState: Bundle?) {

    }

    fun loadRecords(file: File) {
        if (loadedRecords.value == null) {
            addDisposable(
                    recordDao.getAllByFile(file.id)
                            .flatMap { records ->
                                if (PreferencesManager.getByteValue(PreferencesManager.PreferenceKey.TEMPERATURE_UNIT) == Constants.UNIT_FLAG_TEMPERATURE_FAHRENHEIT) {
                                    records.forEach {
                                        it.temperature = ValueInterpreter.celsiusToFahrenheit(it.temperature)
                                    }
                                }
                                Single.fromCallable { records }
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        loadedRecords.value = it
                                        detailActivityState.value = DetailActivityState.Success
                                    },
                                    {
                                        Timber.e(it, "Error loading records.")
                                        detailActivityState.value = DetailActivityState.Error(it.message)
                                    }
                            )
            )
        }
    }


    fun getDetailActivityState(): LiveData<DetailActivityState> = detailActivityState
    fun getLoadedRecords(): LiveData<List<Record>> = loadedRecords
}