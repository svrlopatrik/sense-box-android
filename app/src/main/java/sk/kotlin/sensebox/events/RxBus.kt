package sk.kotlin.sensebox.events

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
object RxBus {

    private val publishProcessor = PublishProcessor.create<BaseEvent>()

    fun <E : BaseEvent> post(event: E) {
        publishProcessor.onNext(event)
    }

    fun <E : BaseEvent> ofType(clazz: Class<E>): Flowable<E> = publishProcessor.ofType(clazz)
}