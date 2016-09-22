package com.marekmaj.rxjava;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SimpleObservableLearningTest {

    @Test
    public void shouldReturnImmediateResultWhenCreatedWithJust() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = Observable.just(1, 2, 3)
                .map(x -> x*x)
                .filter(x -> x > 4)
                .subscribe(buffer::add);

        assertThat(subscription.isUnsubscribed()).isTrue();
        assertThat(buffer).containsExactly(9);
    }

    @Test
    public void shouldEmitValuesWithIntervalAndUnsubscribeWhenDone() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .zipWith(Observable.just(1, 2, 3), (x, y) -> y)
                .map(x -> x*x)
                .filter(x -> x > 4)
                .subscribe(buffer::add);

        assertThat(subscription.isUnsubscribed()).isFalse();

        await().atMost(4, TimeUnit.SECONDS).until(subscription::isUnsubscribed);
        assertThat(buffer).containsExactly(9);
    }

    @Test
    public void shouldFlatMapKeepingOrder() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = Observable.just(1, 2, 3)
                .flatMap(x -> Observable.just(x, 2*x))
                .filter(x -> x > 2)
                .subscribe(buffer::add);

        assertThat(subscription.isUnsubscribed()).isTrue();
        assertThat(buffer).containsExactly(4, 3, 6);
    }

}
