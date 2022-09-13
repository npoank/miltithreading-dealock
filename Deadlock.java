package multithreading;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Deadlock {
    public static void main(String[] args) throws InterruptedException {
        Runner runner = new Runner();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                runner.mythread1();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                runner.mythread2();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        runner.finished();
    }
}

class Runner {
    private Account account1 = new Account();
    private Account account2 = new Account();

    private Lock lock1 = new ReentrantLock();
    private Lock lock2 = new ReentrantLock();

    private void takeLocks(Lock lock1, Lock lock2) {
        boolean lock1taken = false;
        boolean lock2taken = false;

        while (true) {
            try {
                lock1taken = lock1.tryLock();
                lock2taken = lock2.tryLock();
            } finally {
                if (lock1taken && lock2taken) {
                    return;
                }
                if (lock1taken) {
                    lock1.unlock();
                }
                if (lock2taken) {
                    lock2.unlock();
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void mythread1() {
        Random random = new Random();
        for (int i = 0; i < 10_000; i++) {
            takeLocks(lock1, lock2);
            try {
                Account.transfer(account1, account2, random.nextInt(100));
            } finally {
                lock1.unlock();
                lock2.unlock();
            }
        }
    }

    public void mythread2() {
        Random random = new Random();
        for (int i = 0; i < 10_000; i++) {
            takeLocks(lock2, lock1);
            try {
                Account.transfer(account2, account1, random.nextInt(100));
            } finally {
                lock1.unlock();
                lock2.unlock();
            }
        }
    }

    public void finished() {
        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());
        System.out.println("Total balance " + (account1.getBalance() + account2.getBalance()));

    }
}

class Account {
    private int balance = 10_000;

    public int getBalance() {
        return balance;
    }

    public void replenishment(int value) {
        balance += value;
    }

    public void withdraw(int value) {
        balance -= value;
    }

    public static void transfer(Account acc1, Account acc2, int value) {
        acc1.withdraw(value);
        acc2.replenishment(value);
    }
}