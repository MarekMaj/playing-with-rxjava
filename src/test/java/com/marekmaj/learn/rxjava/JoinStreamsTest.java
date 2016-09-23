package com.marekmaj.learn.rxjava;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.marekmaj.learn.rxjava.SampleData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class JoinStreamsTest {

    private final List<String> all = ImmutableList.of("1a", "2a", "3a", "4a", "1b", "2b", "3b", "4b", "1c", "2c", "3c", "4c", "1d", "2d", "3d", "4d");
    @Test
    public void shouldJoinAllElementsUsingIndefiniteTimeWindows() {
        List<String> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.join(slowChars, i -> Observable.never(), c -> Observable.never(), stringFun)
                .subscribe(buffer::add);

        await().atMost(4, TimeUnit.SECONDS).until(subscription::isUnsubscribed);

        assertThat(buffer).containsExactlyElementsOf(all);
    }

    @Test
    public void shouldJoinOnlyFirstElementFromRightStream() throws Exception {
        List<String> buffer = Lists.newArrayList();
        // 500ms observable, every item is valid for 500ms
        Observable<?> duration = Observable.interval(500, TimeUnit.MILLISECONDS);

        Subscription subscription = fastInts.join(slowChars, i -> duration, c -> Observable.never(), stringFun)
                .subscribe(buffer::add);

        await().atMost(1, TimeUnit.SECONDS).until(subscription::isUnsubscribed);

        assertThat(buffer).containsExactly("1a", "2a", "3a", "4a");
    }

    @Test
    public void shouldJoinLastTwoElementsAsWindowIncreased() throws Exception {
        List<String> buffer = Lists.newArrayList();
        Observable<?> duration = Observable.interval(850, TimeUnit.MILLISECONDS);

        Subscription subscription = fastInts.join(slowChars, i -> duration, c -> Observable.never(), stringFun)
                .subscribe(buffer::add);

        await().atMost(2, TimeUnit.SECONDS).until(subscription::isUnsubscribed);

        assertThat(buffer).containsExactly("1a", "2a", "3a", "4a", "3b", "4b");
    }

    @Test
    public void shouldWorkWhenDurationIsJustDelayedValue() throws Exception {
        List<String> buffer = Lists.newArrayList();
        Observable<?> duration = Observable.just(1).delay(850, TimeUnit.MILLISECONDS);

        Subscription subscription = fastInts.join(slowChars, i -> duration, c -> Observable.never(), stringFun)
                .subscribe(buffer::add);

        await().atMost(2, TimeUnit.SECONDS).until(subscription::isUnsubscribed);

        assertThat(buffer).containsExactly("1a", "2a", "3a", "4a", "3b", "4b");
    }

    @Test
    public void shouldEmitAllWhenLeftWindowIsIndefiniteAsValuesFromRightComeAfterLeftIsFinished() throws Exception {
        List<String> buffer = Lists.newArrayList();
        Observable<?> duration = Observable.just(1).delay(100, TimeUnit.MILLISECONDS);

        Subscription subscription = fastInts.join(slowChars.delay(300, TimeUnit.MILLISECONDS), i -> Observable.never(), c -> duration, stringFun)
                .subscribe(buffer::add);

        await().atMost(4, TimeUnit.SECONDS).until(subscription::isUnsubscribed);

        assertThat(buffer).containsExactlyElementsOf(all);
    }
}
