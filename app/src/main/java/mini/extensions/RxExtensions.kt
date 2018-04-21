package mini.extensions

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.mapNotNull(crossinline fn: (T) -> U?): Flowable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Flowable.empty()
        else Flowable.just(mapped)
    }
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.mapNotNull(crossinline fn: (T) -> U?): Observable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Observable.empty()
        else Observable.just(mapped)
    }
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Flowable<T>.select(crossinline fn: (T) -> U?): Flowable<U> =
    mapNotNull(fn).distinctUntilChanged()

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Observable<T>.select(crossinline fn: (T) -> U?): Observable<U> =
    mapNotNull(fn).distinctUntilChanged()

/**
 * Take the first element that matches the filter function.
 */
inline fun <T> Observable<T>.filterOne(crossinline fn: (T) -> Boolean): Maybe<T> {
    return filter { fn(it) }.take(1).singleElement()
}

/**
 * Take the first element that matches the filter function.
 */
inline fun <T> Flowable<T>.filterOne(crossinline fn: (T) -> Boolean): Maybe<T> {
    return filter { fn(it) }.take(1).singleElement()
}