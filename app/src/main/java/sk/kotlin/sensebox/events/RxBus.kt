package sk.kotlin.sensebox.events

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
object RxBus {

    private val publishSubject = PublishSubject.create<BaseEvent>()

    fun <E : BaseEvent> post(event: E) {
        publishSubject.onNext(event)
    }

    fun <E : BaseEvent> ofType(clazz: Class<E>): Observable<E> = publishSubject.ofType(clazz)
}