package com.marekmaj.learn.rxjava;

import rx.Observable;


interface IntegerSource {
    default Observable<Integer> observe() { return observe(false); }

    Observable<Integer> observe(boolean startWithError);


    class InjectedException extends RuntimeException {}
}
