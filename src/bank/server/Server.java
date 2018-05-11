package bank.server;

import Bank.grpc.*;
import Bank.grpc.Currency;
import bank.server.ice.AccountFactoryI;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public void t1(String[] args) {
        int status = 0;
        Communicator communicator = null;
        if (args.length < 2) {
            System.err.println("Not enough arguments; program needs port number for bank service and course service");
            status = 1;
        }
        try {

            Map<Currency, Double> courseMap = new HashMap<>();

            communicator = Util.initialize();

            int port = Integer.parseInt(args[0]);

            int coursePort = Integer.parseInt(args[1]);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter", String.format("tcp -h localhost -p %d:udp -h localhost -p %d", port, port));

            f(coursePort, courseMap);

            logger.info("Server started, listening on " + port);

            AccountFactoryI accountFactory = new AccountFactoryI(courseMap);

            adapter.addDefaultServant(accountFactory, "factory");

            adapter.activate();

            System.out.println("Entering event processing loop...");

            communicator.waitForShutdown();

        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.err.println("Argument port number must be an integer");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        } finally {
            try {
                communicator.destroy();
            } catch (NullPointerException e) {
                // No communicator to destroy
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

    private void f(int port, Map<Currency, Double> courseMap) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        MoneyCourseGrpc.MoneyCourseStub moneyCourseStub = MoneyCourseGrpc.newStub(channel);
        Set<Currency> currencySet = new HashSet<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (true) {
            System.out.println("Enter one of following names to subscribe for given currency or 'x' to continue:");
            Arrays.stream(Currency.values()).forEach(value -> {
                if (!value.equals(Currency.UNRECOGNIZED))
                    System.out.println("-> " + value.name());
            });
            try {
                input = br.readLine();
            } catch (IOException e) {
                System.err.println(e.toString());
            }
            if (input.equals("x")) break;
            else {
                try {
                    currencySet.add(Currency.valueOf(input.trim()));
                } catch (NullPointerException e) {
                    System.err.println("Name not in provided list");
                }
            }
        }
        currencySet.forEach(key -> courseMap.put(key, 0.0));
        System.out.println(courseMap.keySet());
        moneyCourseStub.getCourse(CourseRequest.newBuilder().addAllCurrency(courseMap.keySet()).build(),
                new StreamObserver<CourseResponse>() {
                    @Override
                    public void onNext(CourseResponse response) {
                        synchronized (courseMap) {
                            for (Courses course : response.getCoursesList()) {
                                courseMap.put(course.getCurrency(), course.getRes());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.err.println("Error. Cannot communicate with course service.");
                        System.err.println(throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
//
                    }
                });
    }

    public static void main(String[] args) {
        Server app = new Server();
        app.t1(args);
    }
}
