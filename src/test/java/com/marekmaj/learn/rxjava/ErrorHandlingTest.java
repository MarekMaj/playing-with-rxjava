package com.marekmaj.learn.rxjava;


import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ErrorHandlingTest {


    @Test(expected = OnErrorNotImplementedException.class)
    public void shouldPropoagateExceptionDownstreamWhenNotHandled() {
        Observable<Integer> observable = Observable.<Integer>create(subscriber -> { throw new RuntimeException("exc");})
                .map(x -> x*x);
        // .delay(1, TimeUnit.SECONDS)

        observable.subscribe();
    }

    @Test
    public void shouldNotPropoagateExceptionDownstreamAndTerminate() {
        AtomicBoolean errorHandled = new AtomicBoolean(false);

        Subscription subscription = Observable.<Integer>create(subscriber -> { throw new RuntimeException("exc");})
                .map(x -> x*x)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        throw new RuntimeException("not expected");
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorHandled.set(true);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        throw new RuntimeException("not expected");
                    }
                });

        assertThat(errorHandled.get()).isTrue();
    }

    @Test
    public void shouldNotRecoverAndTerminateAfterError() {
        AtomicBoolean valueObserved = new AtomicBoolean(false);
        AtomicBoolean errorObserved = new AtomicBoolean(false);

        Subscription subscription = CreateMethodIntegerSource.newInstance().observeWithError(true)
                .subscribe(
                        i -> {valueObserved.set(true);},
                        error -> { errorObserved.set(true);});

        await().atMost(2, TimeUnit.SECONDS).until(subscription::isUnsubscribed);
        assertThat(errorObserved.get()).isTrue();
        assertThat(valueObserved.get()).isFalse();
    }

    @Ignore
    public void shouldRecoverFromFailure() {
        AtomicBoolean valueObserved = new AtomicBoolean(false);
        AtomicBoolean errorObserved = new AtomicBoolean(false);

        Subscription subscription = CreateMethodIntegerSource.newInstance().observeWithError(true)
                .onExceptionResumeNext(Observable.empty())
                .subscribe(
                        i -> {valueObserved.set(true);},
                        error -> { errorObserved.set(true);});

        await().atMost(3, TimeUnit.SECONDS).untilTrue(valueObserved);
        assertThat(errorObserved.get()).isFalse();
        assertThat(subscription.isUnsubscribed()).isFalse();
    }
}
