package com.br.song.example.rxreferences

import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * This is a class for Observable and Observer basic Rx operations
 */
class RxBasic {

    private val myList = listOf("Galaxy", "iPhone", "Pixel")

    /**
     * Create a observable with one single emitter item
     */
    @Test
    fun callJust() {
        Observable.just("Hello World Rx").subscribe { value -> print(value) }
    }

    /**
     * Create a observable with a array that can emitter items
     */
    @Test
    fun callFrom() {

        Observable.fromArray(myList).subscribe { print(it) }
    }

    /**
     * A variant of callFrom()
     */
    @Test
    fun callFromIterable() {
        Observable.fromIterable(myList).subscribe(
            { print(it)}, //onNext method has been called
            {error -> print("onError() method has been called: $error")}, //OnError
            { print("onComplete method has been called")} //OnComplete
        )
    }

    /**
     * Create a new custom Observable
     */
    private fun createCustomObservable(newList: List<String>) =
        Observable.create<String> { emitter ->
            newList.forEach { item ->
                if (item.isEmpty()) {
                    emitter.onError(Exception("List with empty value"))
                } else {
                    emitter.onNext(item)
                }
            }
            emitter.onComplete()
        }

    @Test
    fun testCreateCustomObservable() {
        createCustomObservable(myList).subscribe { print(it) }
    }

    @Test
    fun testCreateWithError() {
        val newList = listOf("Hello", "", "World")

        createCustomObservable(newList).subscribe(
            { print(it)}, //onNext()
            { print("Error!")} //onError()
        )
    }

    /**
     * Some examples of Observable "life cycle"
     */
    @Test
    fun observableLifeCycle() {
        Observable.just("Hello Observable life cycle\n")
            .doOnSubscribe { print("Subscribed!\n") }
            .doOnNext { value -> print("emitted: $value \n") }
            .doOnError { error -> print("error: $error \n") }
            .doOnComplete { print("complete\n") }
            .subscribe { print("subscribe\n") }
    }

    /**
     * creates a Observable with a configurable time interval
     */
    @Test
    fun callForInterval() {
        Observable.intervalRange(
            10L, //start
            5L, //count
            0L, //initial delay
            1L, // period
            TimeUnit.SECONDS).subscribe { print("emitted on $it second") }
    }

    /**********************************Emitter Types***********************************************/

    /**
     * The same as Observable but with BackPressure strategy concept
     */
    private fun creatFlowable(value: String) =
        Flowable.create<String> ({ emitter ->
                if (value.isEmpty()) {
                    emitter.onError(Exception("Flowable fail"))
                } else {
                    emitter.onNext("Hello Flowable World")
                }
            emitter.onComplete()

        }, BackpressureStrategy.BUFFER)

    @Test
    fun testCreateFlowable() {
        creatFlowable("hello").subscribe { print(it)}
    }

    @Test
    fun testCreateFlowableWithError() {
        val newValue = ""

        creatFlowable(newValue).subscribe(
            { print(it)}, //onNext()
            { print("Error!")} //OnError()
        )
    }

    /**
     * Maybe is when you want only a single optional value.
     * If value exist onSuccess() is called, otherwise onComplete() is called
     * in error case onError() is called
     */
    @Suppress("unused")
    fun exampleCreateMaybe() =
        Maybe.create<String> { emitter ->
            emitter.onSuccess("Hello Maybe World")
            emitter.onComplete()
            emitter.onError(Exception("Maybe fail"))
        }

    /**
     * Only for a single required value is returned.
     * If value exist onSuccess() is called, otherwise onError() is called
     */
    @Suppress("unused")
    fun exampleCreateSingle() =
        Single.create<String> { emitter ->
            emitter.onSuccess("Hello Single World")
            emitter.onError(Exception("Single fail"))
        }

    /**
     * Completable wont emit data, this emitter is used only for ensure that your operation is
     * completed. A common use for it is for HTTP calls when requestCode 204 (no content) is returned,
     * but the request was successful
     */
    @Suppress("unused")
    fun exampleCreateCompletable() =
        Completable.create { emitter ->
            emitter.onComplete()
            emitter.onError(Exception("Completable fail"))
        }

    /***************************************Schedulers*********************************************/

    /**
     * Schedulers define what thread observable can emit data
     * and what thread observers will consume this data
     * Here some Schedulers:
     *  - Main thread (AndroidSchedulers.mainThread())
     *  - Computation (Schedulers.computation())
     *  - I/O (Schedulers.io())
     *  - New Thread (Schedulers.newThread())
     *
     *  On this example the operation will be performed on I/O
     *  and the result will be posted on the I/O
     *
     *  OBS: io() and computation() can handle the thread pool automatically
     */
    @Test
    fun callForObservableWithScheduler() {
        Observable.create<String> { emitter ->
            emitter.onNext("hello schedule")
            emitter.onComplete()
            emitter.onError(Exception("Scheduler fail")) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    /******************************************Transformers****************************************/

    /**
     * To avoid to call x times subscribeOn() and observeOn(), we can create a ObservableTransformer
     * and use inside of compose()
     */
    private fun <T> createAsyncObservable(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
        }
    }

    @Test
    fun callForAsyncObservable() {
        Observable.just("hello", "world", "observable")
            .compose(createAsyncObservable())
            .subscribe { word -> print(word) }
    }

    /*****************************************Disposable*******************************************/

    /**
     * When you create a "subscription" for a specific Observable on instance of Disposable will be
     * returned, this can allow you to deallocate memory from device.
     * If you are handle with a lot of "subscriptions" you can call for CompositeDisposable.
     * At the end to ensure the will be no memory leak in your Android app, just call for clear()
     */
    @Test
    fun callForDisposable() {
        //handle dispose returned object
        val disposable = Observable.just("Hello").subscribe { println(it) }
        disposable.dispose()

        //call for dispose directly
        Observable.just("World").subscribe { println(it) }.dispose()

        val firstObservable = Observable.just("Bye Bye").subscribe { println(it) }
        val secondObservable = Observable.just("Planet").subscribe { println(it) }

        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(firstObservable)
        compositeDisposable.add(secondObservable)
        compositeDisposable.clear()
    }

    /*************************************Publish Object*******************************************/

    /**
     * Publish Subject is a subclass of Subject it works like Observable and Observer at same time
     * (can emit and receive data).
     *
     * Example in MainActivity
     */

    /************************************Connectable Observable************************************/

    /**
     * By default when a Observer register to a Observable some data will be emitted, it is possible
     * to send the same data through other Observables using ConnectableObservable that was given to
     * us with publish() call
     */
    @Test
    fun callForConnectableObservable() {
        val observable = Observable.just("Event")
            .map { data -> println("Too expensive to be handle $data")
                data }
            .publish() //here receives ConnectableObservable() object
            .autoConnect(2)

        observable.subscribe { data -> println("Subscribe one got: $data") }
        observable.subscribe { data -> println("Subscribe two got: $data") }
    }
}