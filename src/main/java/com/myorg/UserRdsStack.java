package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.CredentialsFromUsernameOptions;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.constructs.Construct;

import java.util.Collections;

public class UserRdsStack extends Stack {

	public UserRdsStack(final Construct scope, final String id, final Vpc vpc) {
		this(scope, id, null, vpc);
	}

	public UserRdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
		super(scope, id, props);

		CfnParameter cfn = CfnParameter.Builder.create(this, "password")
			.type("String")
			.description("Users-ms database password")
			.build();

		// Acesso ao banco de dados
		ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
		iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));

		DatabaseInstance database = DatabaseInstance.Builder.create(this, "UserRds")
			.instanceIdentifier("user-aws-db")
			.engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
				.version(MysqlEngineVersion.VER_8_0)
				.build()))
			.vpc(vpc)
			.credentials(Credentials.fromUsername("admin", CredentialsFromUsernameOptions.builder()
				.password(SecretValue.unsafePlainText(cfn.getValueAsString()))
				.build()))
			.instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
			.multiAz(false)
			.allocatedStorage(10)
			.securityGroups(Collections.singletonList(iSecurityGroup))
			.vpcSubnets(SubnetSelection.builder()
				.subnets(vpc.getPrivateSubnets())
				.build())
			.build();

		CfnOutput.Builder.create(this, "user-db-endpoint")
			.exportName("user-db-endpoint")
			.value(database.getDbInstanceEndpointAddress())
			.build();

		CfnOutput.Builder.create(this, "user-db-password")
			.exportName("user-db-password")
			.value(cfn.getValueAsString())
			.build();
	}
}
