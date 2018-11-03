package sk.kotlin.sensebox.utils

import io.reactivex.FlowableOperator
import io.reactivex.functions.Function
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Created by Patrik Švrlo on 14.10.2018.
 */
class BufferUntil<U, D>(
        private val size: Int,
        private val predicate: Function<U, Boolean>,
        private val transformer: Function<List<U>, D>
) : FlowableOperator<List<D>, U> {

    override fun apply(observer: Subscriber<in List<D>>): Subscriber<in U> {
        return BufferUntilSubscriber(observer)
    }

    private var counter: Int = 0
    private val localBuffer = ArrayList<U>()
    private val transformBuffer = ArrayList<D>()

    private inner class BufferUntilSubscriber(private val observer: Subscriber<in List<D>>) : Subscriber<U> {

        override fun onSubscribe(s: Subscription?) {
            observer.onSubscribe(s)
        }

        override fun onNext(t: U) {
            counter++
            localBuffer.add(t)

            when {
                predicate.apply(t) -> {
                    observer.onNext(transformBuffer)
                    counter = 0
                    localBuffer.clear()
                    transformBuffer.clear()
                }
                counter == size -> {
                    transformBuffer.add(transformer.apply(localBuffer))
                    counter = 0
                    localBuffer.clear()
                }
            }
        }

        override fun onError(t: Throwable?) {
            counter = 0
            localBuffer.clear()
            transformBuffer.clear()
            observer.onError(t)
        }

        override fun onComplete() {
            val bufferCopy = ArrayList(transformBuffer)
            counter = 0
            transformBuffer.clear()
            localBuffer.clear()

            if (bufferCopy.isNotEmpty()) {
                observer.onNext(bufferCopy)
            }

            observer.onComplete()
        }

    }
}