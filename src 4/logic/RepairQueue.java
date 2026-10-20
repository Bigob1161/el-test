package logic;

import models.RepairRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepairQueue<T extends RepairRequest> {
    private List<T> requests;
    private int minutes;
    private int parts;

    public RepairQueue(int minutes, int parts) {
        if (minutes < 0 || minutes > 1440) {
            throw new IllegalArgumentException("minutes должен быть от 0 до 1440");
        }
        if (parts < 0 || parts > 1000) {
            throw new IllegalArgumentException("parts должен быть от 0 до 1000");
        }
        this.requests = new ArrayList<>();
        this.minutes = minutes;
        this.parts = parts;
    }

    public boolean add(T request) {
        if (request == null || request.isCompleted()) {
            return false;
        }
        boolean exists = requests.stream().anyMatch(r -> r.getId() == request.getId());
        if (exists) {
            return false;
        }
        requests.add(request);
        return true;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getParts() {
        return parts;
    }

    public boolean replenish(int addMinutes, int addParts) {
        if (addMinutes < 0 || addParts < 0) {
            return false;
        }
        if (addMinutes == 0 && addParts == 0) {
            return false;
        }
        if (minutes + addMinutes > 1440 || parts + addParts > 1000) {
            return false;
        }
        minutes += addMinutes;
        parts += addParts;
        return true;
    }

    public Stream<T> getAnalyticsStream() {
        return new ArrayList<>(requests).stream();
    }

    public List<T> process(Predicate<T> condition, Comparator<T> order, Consumer<T> action) {
        List<T> pending = requests.stream()
                .filter(r -> !r.isCompleted())
                .sorted(order)
                .collect(Collectors.toList());
        List<T> done = new ArrayList<>();
        for (T request : pending) {
            int needMinutes = request.getRequiredMinutes();
            int needParts = request.getRequiredParts();
            if (needMinutes <= minutes && needParts <= parts && condition.test(request)) {
                minutes -= needMinutes;
                parts -= needParts;
                action.accept(request);
                done.add(request);
            }
        }
        return done;
    }
}
