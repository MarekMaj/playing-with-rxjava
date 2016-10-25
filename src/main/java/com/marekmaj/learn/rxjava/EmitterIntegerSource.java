package com.marekmaj.learn.rxjava;

import rx.AsyncEmitter;
import rx.Observable;
import rx.functions.Action1;


public class EmitterIntegerSource implements IntegerSource {

    // TODO
    @Override
    public Observable<Integer> observe(boolean startWithError) {
        return Observable.<Integer>fromEmitter(new Action1<AsyncEmitter<Integer>>() {
            @Override
            public void call(AsyncEmitter<Integer> integerAsyncEmitter) {
                //integerAsyncEmitter.
                //integerAsyncEmitter.setSubscription();
            }
        }, AsyncEmitter.BackpressureMode.BUFFER);
    }
}
