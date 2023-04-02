package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.constructs.Construct;

public class UserClusterStack extends Stack {

    private final Cluster cluster;

    public UserClusterStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public UserClusterStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

         this.cluster = Cluster.Builder.create(this, "UserCluster")
            .clusterName("cluster-user")
            .vpc(vpc)
            .build();
    }

    public Cluster getCluster() {
        return cluster;
    }
}
