package elasticity.services;


import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.logging.Level;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import elasticity.interfaces.Service;

/**
 * The VM service, allows creating and destroying instances
 * on an EC2-like cloud.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class AmazonService extends Service {
	
	private static final String awsServiceUrl = "ec2.eu-west-1.amazonaws.com";				//"http://194.199.25.115:80/services/Cloud/";
	private static final String awsAccessKey = "AKIAJEB3ONM4SNB3UIEA";						//"admin";
	private static final String awsSecretKey = "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"; 	//"password";
	private static final String awsKeypair = "joram";
	private static final String awsInstanceType = "m1.small";
	private static final String awsSecurityGroup = "default";
	
	private String awsImageId;
	
	private AmazonEC2 ec2;
	
	/**
	 * Matches the current VM instances IP addresses with their EC2 IDs.
	 */
	private Map<String,String> ip2id = new TreeMap<String,String>();
	
	@Override
	protected void initService(Properties props) throws Exception {
		//Get the properties..
		awsImageId = props.getProperty("aws_image_id");
		
		ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKey,awsSecretKey));
		ec2.setEndpoint(awsServiceUrl);
		
		logger.log(Level.INFO,"Initialization completed.");
	}
	
	/**
	 * Runs a VM instance on an AWS compatible cloud.
	 * 
	 * @return The IP of the started instance.
	 */
	public String runInstance() throws Exception {
		logger.log(Level.INFO,"Running new instance..");
		//Prepares the run request.
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.setImageId(awsImageId);
		runInstancesRequest.setKeyName(awsKeypair);
		runInstancesRequest.setSecurityGroups(Collections.singleton(awsSecurityGroup));
		runInstancesRequest.setInstanceType(awsInstanceType);
		runInstancesRequest.setMaxCount(1);
		runInstancesRequest.setMinCount(1);

		//Executes the run request.
		String instanceId = ""; //Will be changed eventually.
		try {
			RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
			instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
			logger.log(Level.INFO,"Sent RunInstanceRequest successfully..");
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while sending RunInstanceRequest!" );
			e.printStackTrace(System.out);
			throw e; //Forwards the exception
		}
				
		//Waits for the instance to be "running".
		String instanceIp = ""; //Will be changed eventually.
		boolean done = false;
		while (!done) {
			try {
				Thread.sleep(1000);

				DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
				describeInstancesRequest.withInstanceIds(instanceId);
				DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);

				Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
				logger.log(Level.INFO,"Current instance state: " + instance.getState().getName() + "..");
				if (instance.getState().getName().equals("running")) {
					instanceIp = instance.getPrivateIpAddress();
					ip2id.put(instanceIp,instanceId);
					done = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"Error while waiting for instance to become running!" );
				throw e;
			}
		}
		
		logger.log(Level.INFO,"Instance ran with IP: " + instanceIp + ".");
		return instanceIp;
	}

	/**
	 * Terminates a VM instance.
	 * 
	 * @param instanceIp The IP address of the instance to be removed
	 */
	public void terminateInstance(String instanceIp) throws Exception {
		logger.log(Level.INFO,"Terminating instance with IP: " + instanceIp + "..");
		String instanceId = ip2id.get(instanceIp);

		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.withInstanceIds(instanceId);
		ec2.terminateInstances(terminateInstancesRequest);

		ip2id.remove(instanceIp);
		logger.log(Level.INFO,"Terminated instance successfully.");
	}
}
