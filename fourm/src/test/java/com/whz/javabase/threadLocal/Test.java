package com.whz.javabase.threadLocal;

public class Test {

	public static void main(String[] args) {
		try {
			for (int i = 0; i < 3; i++) {
				System.out.println("在Main线程中取值=" + Tools.tl.get());
				Thread.sleep(100);
			}
			Thread.sleep(5000);
			ThreadA a = new ThreadA();
			a.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@org.junit.Test
	public void testThreadLocal() throws InterruptedException {

		for (int i = 0; i < 3; i++) {
			System.out.println("在Main线程中取值=" + Tools.tl.get());
			Thread.sleep(100);
		}
		Thread.sleep(5000);
		ThreadA a = new ThreadA();
		a.start();
	}

}