package com.marekmaj.learn.rxjava;


import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.marekmaj.learn.rxjava.SampleData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CombiningStreamsTest {

    @Test
    public void shouldZipValuesAsTheyComeInSlowerStream() throws Exception {
        List<String> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.zipWith(slowChars, stringFun)
                .subscribe(buffer::add);

        await().atLeast(1, TimeUnit.SECONDS).until(() -> buffer.contains("2b"));

        assertThat(buffer).containsExactly("1a", "2b");
    }

    @Test
    public void shouldMergeNotWaitingForSlowerStream() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.mergeWith(slowInts)
                .subscribe(buffer::add);

        await().atLeast(1, TimeUnit.SECONDS).until(() -> buffer.contains(-2));

        assertThat(buffer).contains(1, 4,-1,-2);
        assertThat(buffer).doesNotContain(-3, -4);
    }

    @Test
    public void shouldGetFirstResultFromMergedStreams() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = Observable.merge(fastInts, slowInts)
                .first()
                .subscribe(buffer::add);

        await().atMost(1, TimeUnit.SECONDS).until(() -> buffer.contains(1));
        assertThat(subscription.isUnsubscribed()).isTrue();
        assertThat(buffer).contains(1);
    }

    @Test
    public void shouldConcatPreservingOrder() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.concatWith(slowInts)
                .subscribe(buffer::add);

        await().atLeast(1, TimeUnit.SECONDS).until(() -> buffer.contains(-2));

        assertThat(buffer).containsExactly(1,2, 3, 4, -1, -2);
    }

    @Test
    public void shouldFlatMapWithoutPreservingAnyOrder() {
        List<String> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.flatMap(i -> slowChars.map(c -> String.format("%d%c", i, c)))
                .subscribe(buffer::add);

        await().atLeast(1, TimeUnit.SECONDS).until(() -> buffer.contains("1b"));

        // flatMap nie zachowuje kolejnosci flatennowania dlatego contains!
        assertThat(buffer).contains("1a", "2a", "3a", "4a", "1b");
    }

    @Test
    public void shouldConcatMapPreservingOrder() {
        List<String> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.concatMap(i -> slowChars.map(c -> String.format("%d%c", i, c)))
                .subscribe(buffer::add);

        await().atLeast(3, TimeUnit.SECONDS).until(() -> buffer.contains("2a"));

        assertThat(buffer).containsExactly("1a", "1b", "1c", "1d", "2a");
    }

    @Test
    public void shouldCombineLatest() {
        List<String> buffer = Lists.newArrayList();

        Subscription subscription = Observable.combineLatest(fastInts, slowChars, stringFun)
                .subscribe(buffer::add);

        await().atLeast(1, TimeUnit.SECONDS).until(() -> buffer.contains("4b"));

        assertThat(buffer).containsExactly("1a", "2a", "3a", "4a", "4b");
    }

    @Test
    public void shouldWaitForSourceStreamToFinishWhenConcat() {
        List<Integer> buffer = Lists.newArrayList();

        Subscription subscription = fastInts.concatWith(slowInts)
                .subscribe(buffer::add);

        await().atMost(1, TimeUnit.SECONDS).until(() -> buffer.contains(-1));

        assertThat(buffer).containsExactly(1,2,3,4,-1);
    }

}
