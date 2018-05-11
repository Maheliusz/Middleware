package bank.server.grpc;

import Bank.grpc.CourseRequest;
import Bank.grpc.CourseResponse;
import Bank.grpc.Courses;
import Bank.grpc.Currency;
import Bank.grpc.MoneyCourseGrpc.MoneyCourseImplBase;
import com.zeroc.IceInternal.Ex;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MoneyCourseImpl extends MoneyCourseImplBase {

    private final Map<Currency, Double> courseMap;
    private final Random random;
    private Thread courseUpdater;

    public MoneyCourseImpl() {
        random = new Random();
        courseMap = new HashMap<>();
        courseMap.put(Currency.FRANK, getRandomDouble());
        courseMap.put(Currency.DOLLAR, getRandomDouble());
        courseMap.put(Currency.BITCOIN, getRandomDouble());
        courseMap.put(Currency.TOMATO, getRandomDouble());
        courseMap.put(Currency.EURO, getRandomDouble());
        courseUpdater = new Thread(() -> {
            while(true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                }
                courseMap.keySet().forEach(course -> {
                    courseMap.put(course, getRandomDouble());
                });
            }
        });
    }

    @Override
    public void getCourse(CourseRequest request, StreamObserver<CourseResponse> responseObserver) {
        while (true) {
            List<Courses> coursesList = new ArrayList<>();
            for (Currency currency : request.getCurrencyList()) {
                coursesList.add(Courses.newBuilder().setCurrency(currency).setRes(courseMap.get(currency)).build());
            }
            CourseResponse response = CourseResponse.newBuilder().addAllCourses(coursesList).build();
            try {
                responseObserver.onNext(response);

                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private double getRandomDouble() {
        return (0.5 + (5 - 0.5) * random.nextDouble());
    }

    private synchronized Map<Currency, Double> getCourseMap() {
        return courseMap;
    }
}
