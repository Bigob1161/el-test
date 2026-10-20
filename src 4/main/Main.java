package main;

import logic.RepairQueue;
import models.MaintenanceRequest;
import models.RepairRequest;
import models.ReplacementRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static RepairQueue<RepairRequest> queue;
    private static int n;
    private static int variantA;
    private static int variantB;
    private static int variantC;
    private static int completedStage = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        n = 0;
        while (n == 0) {
            System.out.print("Введите начальный номер N (1-36): ");
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                variantA = StageAccess.initial(value);
                n = value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        System.out.println("Назначен вариант задания 1: А" + variantA);

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Выполнить задание 1 и вывести назначенный вариант задания 2");
            System.out.println("2. Выполнить задание 2 и вывести назначенный вариант задания 3");
            System.out.println("3. Выполнить задание 3 и вывести отчёт");
            System.out.println("4. Показать очередь, статусы и остатки времени и деталей");
            System.out.println("5. Выход из программы");
            System.out.print("Выберите пункт: ");
            String line = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число от 1 до 5.");
                continue;
            }
            switch (choice) {
                case 1:
                    stageOne();
                    break;
                case 2:
                    stageTwo();
                    break;
                case 3:
                    stageThree();
                    break;
                case 4:
                    showQueue();
                    break;
                case 5:
                    running = false;
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Ошибка: введите число от 1 до 5.");
            }
        }
        scanner.close();
    }

    private static List<RepairRequest> createRequests() {
        List<RepairRequest> list = new ArrayList<>();
        list.add(new MaintenanceRequest(201, 21, 3, 5));
        list.add(new MaintenanceRequest(203, 22, 4, 5));
        list.add(new MaintenanceRequest(205, 23, 2, 2));
        list.add(new MaintenanceRequest(208, 24, 5, 4));
        list.add(new MaintenanceRequest(210, 25, 4, 2));
        list.add(new ReplacementRequest(202, 21, 2, 4, 5));
        list.add(new ReplacementRequest(204, 22, 1, 3, 5));
        list.add(new ReplacementRequest(206, 23, 3, 4, 0));
        list.add(new ReplacementRequest(207, 24, 1, 1, 0));
        list.add(new ReplacementRequest(209, 22, 1, 4, 0));
        return list.stream()
                .sorted(Comparator.comparingInt(RepairRequest::getId))
                .collect(Collectors.toList());
    }

    private static void stageOne() {
        if (completedStage >= 1) {
            System.out.println("Этап 1 уже выполнен.");
            return;
        }
        if (variantA != 2) {
            System.out.println("Реализован только вариант А2.");
            return;
        }
        Predicate<RepairRequest> selection = r -> r.getUnits() >= 2 && r.getRequiredMinutes() <= 30;
        RepairQueue<RepairRequest> newQueue = new RepairQueue<>(60, 2);
        for (RepairRequest request : createRequests()) {
            if (selection.test(request)) {
                newQueue.add(request);
            }
        }
        List<Integer> ids = newQueue.getAnalyticsStream()
                .map(RepairRequest::getId)
                .sorted()
                .collect(Collectors.toList());
        int minutes = newQueue.getAnalyticsStream().mapToInt(RepairRequest::getRequiredMinutes).sum();
        int parts = newQueue.getAnalyticsStream().mapToInt(RepairRequest::getRequiredParts).sum();
        System.out.println("ID добавленных заявок: " + ids);
        System.out.println("Сумма потребностей во времени: " + minutes);
        System.out.println("Сумма потребностей в деталях: " + parts);
        try {
            variantB = StageAccess.second(n, ids, minutes, parts);
            queue = newQueue;
            completedStage = 1;
            System.out.println("Назначен вариант задания 2: Б" + variantB);
        } catch (IllegalStateException e) {
            System.out.println("Ошибка проверки: " + e.getMessage() + ". Проверьте решение.");
        }
    }

    private static void stageTwo() {
        if (completedStage < 1) {
            System.out.println("Сначала выполните этап 1.");
            return;
        }
        if (completedStage >= 2) {
            System.out.println("Этап 2 уже выполнен.");
            return;
        }
        if (variantB != 4) {
            System.out.println("Реализован только вариант Б4.");
            return;
        }
        List<RepairRequest> done = runProcess();
        List<Integer> ids = done.stream()
                .map(RepairRequest::getId)
                .collect(Collectors.toList());
        System.out.println("ID выполненных заявок: " + ids);
        System.out.println("Остаток времени: " + queue.getMinutes());
        System.out.println("Остаток деталей: " + queue.getParts());
        try {
            variantC = StageAccess.third(n, ids, queue.getMinutes(), queue.getParts());
            completedStage = 2;
            System.out.println("Назначен вариант задания 3: В" + variantC);
        } catch (IllegalStateException e) {
            System.out.println("Ошибка проверки: " + e.getMessage() + ". Проверьте решение.");
        }
    }

    private static void stageThree() {
        if (completedStage < 2) {
            System.out.println("Сначала выполните этап 2.");
            return;
        }
        if (completedStage >= 3) {
            System.out.println("Этап 3 уже выполнен.");
            return;
        }
        if (variantC != 6) {
            System.out.println("Реализован только вариант В6.");
            return;
        }
        if (!queue.replenish(15, 2)) {
            System.out.println("Ошибка: не удалось пополнить ресурсы.");
            return;
        }
        List<RepairRequest> done = runProcess();
        List<Integer> ids = done.stream()
                .map(RepairRequest::getId)
                .collect(Collectors.toList());
        System.out.println("ID новых выполненных заявок: " + ids);
        System.out.println("Остаток времени: " + queue.getMinutes());
        System.out.println("Остаток деталей: " + queue.getParts());
        Map<Integer, Double> report = queue.getAnalyticsStream()
                .collect(Collectors.groupingBy(RepairRequest::getCustomerId,
                        Collectors.averagingDouble(r -> r.isCompleted() ? 1.0 : 0.0)));
        System.out.println("Доля выполненных заявок по клиентам: " + report);
        completedStage = 3;
    }

    private static List<RepairRequest> runProcess() {
        RepairQueue<RepairRequest> current = queue;
        int reserve = (current.getParts() + 2) / 3;
        Predicate<RepairRequest> condition = r -> current.getParts() - r.getRequiredParts() >= reserve;
        Comparator<RepairRequest> order = Comparator.comparingInt(RepairRequest::getPriority).reversed()
                .thenComparingInt(RepairRequest::getRequiredMinutes)
                .thenComparingInt(RepairRequest::getId);
        return current.process(condition, order, RepairRequest::markCompleted);
    }

    private static void showQueue() {
        if (queue == null) {
            System.out.println("Очередь ещё не сформирована.");
            return;
        }
        queue.getAnalyticsStream().forEach(r -> System.out.println(r.getDetails()));
        System.out.println("Остаток времени: " + queue.getMinutes());
        System.out.println("Остаток деталей: " + queue.getParts());
    }
}
final class StageAccess {
    private static final int[] S = {0, 3, 3, 2, 0, 1};
    private static final int[] A = {
        0x2e02a10d, 0x658e283b, 0x4bdc593a, 0xc22e0b9e, 0xbf0fca1e,
        0xa0951f33
    };
    private static final int[] B = {
        0x29657c36, 0x96d25ad4, 0xa1c42961, 0xa1c42961, 0x49f39b4a,
        0x49f39b4a, 0x49f39b4a, 0x1f4445f7, 0x3fb6878e, 0x3fb6878e,
        0x49f39b4a, 0x49f39b4a, 0x574556c0, 0xa1c42961, 0xa1c42961,
        0x49f39b4a, 0x49f39b4a, 0xa37914c6, 0xa1c42961, 0x49f39b4a,
        0x49f39b4a, 0x29657c36, 0xe3f8f103, 0xa1c42961, 0x49f39b4a,
        0x49f39b4a, 0xf3b1ab4a, 0x3fb6878e, 0x3fb6878e, 0x49f39b4a,
        0x83f6e3cb, 0x574556c0, 0x892997a2, 0x892997a2, 0x892997a2,
        0x83f6e3cb
    };
    static int initial(int n) {
        if (n < 1 || n > 36)
            throw new IllegalArgumentException("N: 1..36");
        return (n - 1) / 6 + 1;
    }
    static int second(int n, java.util.List<Integer> ids,
                      int minutes, int parts) {
        return check(n, ids, minutes, parts, A[initial(n) - 1], 0);
    }
    static int third(int n, java.util.List<Integer> ids,
                     int minutes, int parts) {
        int a = initial(n);
        int b = 1 + Math.floorMod((long) A[a - 1] + (n - 1) % 6, 6);
        return check(n, ids, minutes, parts, B[n - 1], b);
    }
    private static int check(int n, java.util.List<Integer> ids,
            int minutes, int parts, int expected, int previous) {
        if (ids == null) throw new IllegalArgumentException("ids: null");
        int actual = (ids + "|" + minutes + "|" + parts).hashCode();
        if (actual != expected)
            throw new IllegalStateException("Check stage result");
        int p = (n - 1) % 6;
        long key = previous == 0 ? (long) actual + p
            : (actual >>> 5) + 1L * p + 0L * previous + S[initial(n) - 1];
        return 1 + Math.floorMod(key, 6);
    }
}
