package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class UserServiceStack extends Stack {

    public UserServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public UserServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> auth = new HashMap<>();
        auth.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("user-db-endpoint") +
            ":3306/employee?createDatabaseIfNotExist=true");
        auth.put("SPRING_DATASOURCE_USERNAME", "admin");
        auth.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("user-db-password"));

        IRepository repository = Repository.fromRepositoryName(this, "Repository", "img-users-ms");

        // fromRepository -> Docker Hub
        // fromErcRepository - AWS ECR
        ApplicationLoadBalancedFargateService fargate = ApplicationLoadBalancedFargateService.Builder.create(this, "UserService")
            .serviceName("user-service-ola")
            .cluster(cluster)
            .cpu(512)
            .desiredCount(1)
            .listenerPort(8080)
            .assignPublicIp(true)
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .image(ContainerImage.fromEcrRepository(repository))
                    .containerName("app-ola")
                    .containerPort(8080)
                    .environment(auth)
                    .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "UserMsLogGroup")
                            .logGroupName("UserMsLog")
                            .removalPolicy(RemovalPolicy.DESTROY)
                            .build())
                        .streamPrefix("UserMS")
                        .build()))
                    .build())
            .memoryLimitMiB(1024)
            .publicLoadBalancer(true)
            .build();

        ScalableTaskCount scalableTarget = fargate.getService().autoScaleTaskCount(EnableScalingProps.builder()
            .minCapacity(1)
            .maxCapacity(3)
            .build());

        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
            .targetUtilizationPercent(70)
            .scaleInCooldown(Duration.minutes(3))
            .scaleOutCooldown(Duration.minutes(2))
            .build());

        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
            .targetUtilizationPercent(70)
            .scaleInCooldown(Duration.minutes(3))
            .scaleOutCooldown(Duration.minutes(2))
            .build());

    }
}
