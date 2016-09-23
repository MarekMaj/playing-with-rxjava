package com.marekmaj.learn.rxjava;

import rx.Observable;


interface IntegerSource {
    default Observable<Integer> observeWithError() { return observeWithError(false); }

    Observable<Integer> observeWithError(boolean startWithError);


    class InjectedException extends RuntimeException {}
}
