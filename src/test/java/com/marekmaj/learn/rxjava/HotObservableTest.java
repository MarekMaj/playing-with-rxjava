package com.marekmaj.learn.rxjava;


import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Subscription;
import rx.observables.ConnectableObservable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.marekmaj.learn.rxjava.SampleData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class HotObservableTest {

    @Test
    public void shouldPublishToAllSubscribersAfterConnect() {
        List<Integer> buffer1 = Lists.newArrayList();
        List<Integer> buffer2 = Lists.newArrayList();
        ConnectableObservable<Integer> published = fastInts.publish();

        Subscription subscription1 = published.subscribe(buffer1::add);

        Subscription subscription2 = published.subscribe(buffer2::add);

        Subscription connection = published.connect();

        await().atMost(1, TimeUnit.SECONDS).until(connection::isUnsubscribed);

        assertThat(subscription1.isUnsubscribed()).isTrue();
        assertThat(subscription2.isUnsubscribed()).isTrue();
        assertThat(buffer1).containsExactly(1,2,3,4);
        assertThat(buffer2).containsExactly(1,2,3,4);
    }

    @Test
    public void shouldObserveOnlyElementsAfterSubscribeAndConnect() {
        List<Integer> buffer1 = Lists.newArrayList();
        List<Integer> buffer2 = Lists.newArrayList();
        ConnectableObservable<Integer> published = fastInts.publish();

        Subscription subscription1 = published.subscribe(buffer1::add);

        Subscription connection = published.connect();

        Subscription subscription2 = published.subscribe(buffer2::add);

        await().atMost(1, TimeUnit.SECONDS).until(connection::isUnsubscribed);

        assertThat(subscription1.isUnsubscribed()).isTrue();
        assertThat(subscription2.isUnsubscribed()).isTrue();
        assertThat(buffer1).containsExactly(1,2,3,4);
        assertThat(buffer2).containsExactly(2,3,4);
    }

    @Test
    public void shouldReplayAllElements() throws Exception {
        List<Integer> buffer1 = Lists.newArrayList();
        List<Integer> buffer2 = Lists.newArrayList();
        ConnectableObservable<Integer> published = fastInts.replay(); // unbounded buffer by default

        Subscription connection = published.connect();

        Subscription subscription1 = published.subscribe(buffer1::add);

        await().atMost(1, TimeUnit.SECONDS).until(subscription1::isUnsubscribed);
        assertThat(buffer1).containsExactly(1,2,3,4);
        assertThat(connection.isUnsubscribed()).isTrue();

        Subscription subscription2 = published.subscribe(buffer2::add);

        await().atMost(1, TimeUnit.SECONDS).until(subscription2::isUnsubscribed);
        assertThat(buffer2).containsExactly(1,2,3,4);
    }

    @Test
    public void shouldReplayOnlyNewElements() {
        List<Integer> buffer1 = Lists.newArrayList();
        List<Integer> buffer2 = Lists.newArrayList();
        ConnectableObservable<Integer> published = fastInts.replay(200, TimeUnit.MILLISECONDS);

        Subscription connection = published.connect();

        Subscription subscription1 = published.subscribe(buffer1::add);

        await().atMost(1, TimeUnit.SECONDS).until(subscription1::isUnsubscribed);
        assertThat(buffer1).containsExactly(1,2,3,4);
        assertThat(connection.isUnsubscribed()).isTrue();

        Subscription subscription2 = published.subscribe(buffer2::add);

        await().atMost(2, TimeUnit.SECONDS).until(subscription2::isUnsubscribed);
        assertThat(buffer2).contains(4);
        assertThat(buffer2).doesNotContain(1);
    }
}
