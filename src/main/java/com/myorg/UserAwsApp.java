package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class UserAwsApp {
    public static void main(final String[] args) {
        App app = new App();

        UserVpcStack vpc = new UserVpcStack(app, "Vpc");
        UserClusterStack cluster = new UserClusterStack(app, "Cluster", vpc.getVpc());
        cluster.addDependency(vpc);

        UserRdsStack rds = new UserRdsStack(app, "Rds", vpc.getVpc());
        rds.addDependency(vpc);

        UserServiceStack service = new UserServiceStack(app, "Service", cluster.getCluster());
        service.addDependency(cluster);
        service.addDependency(rds);

        app.synth();
    }
}

