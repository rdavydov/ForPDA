package forpdateam.ru.forpda.extensions

import io.reactivex.Observable

/**
 * Waits in background until [observable] emits first value.
 *
 * @result the first value emitted by [observable]
 */
//suspend fun <V> AsyncController.await(observable: Observable<V>): V = this.await {
//    observable.toBlocking().first()
//}