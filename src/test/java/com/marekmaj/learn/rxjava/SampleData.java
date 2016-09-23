package com.marekmaj.learn.rxjava;

import rx.Observable;
import rx.functions.Func2;

import java.util.concurrent.TimeUnit;

public final class SampleData {


    private SampleData() {
    }

    // observables are immutable and reusable
    static final Observable<Long> fast = Observable.interval(0, 100, TimeUnit.MILLISECONDS);
    static final Observable<Long> slow = Observable.interval(0, 1, TimeUnit.SECONDS);
    static final Observable<Integer> ints = Observable.just(1,2,3,4);
    static final Observable<Character> chars = Observable.just('a', 'b', 'c', 'd');
    static final Observable<Integer> fastInts = ints.zipWith(fast, (x, y) -> x);
    static final Observable<Integer> slowInts = ints.map(i -> -i).zipWith(slow, (x, y) -> x);
    static final Observable<Character> slowChars =  chars.zipWith(slow, (x, y) -> x);


    static Func2<Integer, Character, String> stringFun = (i, c) -> String.format("%d%c", i, c);
}
