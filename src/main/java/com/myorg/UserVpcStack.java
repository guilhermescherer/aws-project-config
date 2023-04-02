package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class UserVpcStack extends Stack {

    private final Vpc vpc;

    public UserVpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public UserVpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.vpc = Vpc.Builder.create(this, "UserVpc")
            .maxAzs(3) // Quantidade max de zonas e disponibilidade
            .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
