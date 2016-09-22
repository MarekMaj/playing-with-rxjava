package com.marekmaj.rxjava;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleObservableTest {

    @Test
    public void shouldReturnImmediateResult() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = Observable.just(1, 2 , 3)
                .map(x -> x*x)
                .filter(x -> x > 4)
                .subscribe(buffer::add);

        assertThat(subscription.isUnsubscribed()).isTrue();
        assertThat(buffer).isEqualTo(ImmutableList.of(9));
    }
}
