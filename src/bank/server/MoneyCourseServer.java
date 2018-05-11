package bank.server;

import bank.server.grpc.MoneyCourseImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoneyCourseServer {

    private static final Logger logger = Logger.getLogger(MoneyCourseServer.class.getName());

    private int port;
    private Server server;

    public MoneyCourseServer(int port) {
        this.port = port;
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new MoneyCourseImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                MoneyCourseServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.err.println("Not enough arguments; program needs port number for service");
        } else {
            try {
                int portNumber = Integer.parseInt(args[0]);
                final MoneyCourseServer server = new MoneyCourseServer(portNumber);
                try {
                    server.start();
                } catch (IOException e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
                server.blockUntilShutdown();
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
                System.err.println("Argument port number must be an integer");
            }
        }
    }
}
