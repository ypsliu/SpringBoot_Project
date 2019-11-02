package com.cxp.springboot2webflux;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;

@SpringBootTest
@RunWith(SpringRunner.class)
class Springboot2WebfluxApplicationTests {

	@Test
	void contextLoads() {

	}

	public static void main(String[] args) {
		Flux.just("hello",111).subscribe(System.out::println);
		Flux.fromArray(new Integer[]{11,13,15}).share().subscribe(System.out::println);
		Flux.empty().subscribe(System.out::println);
		Flux.range(1, 10).subscribe(System.out::println);
		Flux.interval(Duration.of(10, ChronoUnit.SECONDS)).subscribe(System.out::println);
		System.out.println("=========");
		Flux.range(1, 10)
				.timeout(Flux.never(), v -> Flux.never())
				.subscribe(System.out::println);
		System.out.println("=========");
//		Flux.interval(Duration.ofSeconds(2)).doOnNext(System.out::println).blockLast();

		Flux.generate(sink -> {
			sink.next("Hello");
			sink.complete();
		}).subscribe(System.out::println);


		final Random random = new Random();
		Flux.generate(ArrayList::new, (list, sink) -> {
			int value = random.nextInt(100);
			list.add(value);
			sink.next(value);
			if (list.size() == 10) {
				sink.complete();
			}
			return list;
		}).subscribe(System.out::println);

		Flux.create(sink -> {
			for (int i = 0; i < 10; i++) {
				sink.next(i);
			}
			sink.complete();
		}).subscribe(System.out::println);

	}
}