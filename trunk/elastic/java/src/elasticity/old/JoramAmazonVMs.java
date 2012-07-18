package elasticity.old;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class JoramAmazonVMs {

	// AWS related fields.
	public static String awsImageId;
	final static String awsAccessKey = "admin";
	final static String awsSecretKey = "password";
	final static String awsServiceUrl = "http://194.199.25.115:80/services/Cloud/";
	final static String awsKeypair = "molkey";
	final static String awsInstanceType = "m1.small";

	static Logger logger;

	/**
	 * Matches the current VM instances IP addresses with their EC2 IDs.
	 */
	private static Map<String,String> ip2id = new TreeMap<String,String>();

	/**
	 * Runs a VM instance on an AWS compatible cloud.
	 * 
	 * @return the IP of the started instance.
	 */
	public static String runInstance() {
		AmazonEC2 ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKey,awsSecretKey));
		ec2.setEndpoint(awsServiceUrl);

		// Runs instance
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.setImageId(awsImageId);
		runInstancesRequest.setKeyName(awsKeypair);
		runInstancesRequest.setInstanceType(awsInstanceType);
		runInstancesRequest.setMaxCount(1);

		String instanceId = ""; // Will be changed eventually.
		boolean done = false;
		while (!done) {
			try {
				RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
				instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
				done = true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"[VM] Error while sending runInstance Request!!" );
			}
		}
		
		logger.log(Level.INFO,"[VM] runInstanceRequest sent successfully..");

		String instanceIp = ""; // Will be changed eventually.
		done = false;
		while (!done) {
			try {
				Thread.sleep(1000);

				DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
				describeInstancesRequest.withInstanceIds(instanceId);
				DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);



				Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
				logger.log(Level.INFO,"[VM] state: " + instance.getState().getName());
				if (instance.getState().getName().equals("running")) {
					instanceIp = instance.getPublicIpAddress();
					ip2id.put(instanceIp,instanceId);
					done = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"[VM] Error while waiting for instance to become running!!" );
			}
		}

		return instanceIp;
	}

	/**
	 * Terminates a VM instance.
	 * 
	 * @param instanceIp the IP address of the instance to be removed
	 */
	public static void terminateInstance(String instanceIp) {

		AmazonEC2 ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKey,awsSecretKey));
		ec2.setEndpoint(awsServiceUrl);

		String instanceId = ip2id.get(instanceIp);

		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.withInstanceIds(instanceId);
		ec2.terminateInstances(terminateInstancesRequest);

		ip2id.remove(instanceIp);
	}

	public static void main(String args[]) {
		String ip = runInstance();
		System.out.println("IP: " + ip);
		terminateInstance(ip);
	}
}
