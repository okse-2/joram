/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */

package elasticity.services;

import java.io.InputStream;
import java.io.FileInputStream;
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
	
	private static final String awsPropFile = "../aws/amazon.properties";
	
	private String awsServiceUrl; //"http://194.199.25.115:80/services/Cloud/";
	private String awsAccessKey;  //"admin";
	private String awsSecretKey;  //"password";
	private String awsKeypair;
	private String awsInstanceType;
	private String awsSecurityGroup;
	private String awsImageId;
	
	private AmazonEC2 ec2;
	
	/**
	 * Matches the current VM instances IP addresses with their EC2 IDs.
	 */
	private Map<String,String> ip2id = new TreeMap<String,String>();
	
	@Override
	protected void initService(Properties props) throws Exception {
		//Get the properties..
		Properties awsProps = new Properties();
		InputStream reader;
		try {
			reader = new FileInputStream(awsPropFile);
			awsProps.load(reader);
			reader.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"ERROR while reading the AWS properties file:");
			e.printStackTrace(System.out);
			throw e;
		}
		
		awsServiceUrl = awsProps.getProperty("aws_service_url");
		awsAccessKey = awsProps.getProperty("aws_access_key");
		awsSecretKey = awsProps.getProperty("aws_secret_key");
		awsKeypair = awsProps.getProperty("aws_keypair");
		awsInstanceType = awsProps.getProperty("aws_instance_type");
		awsSecurityGroup = awsProps.getProperty("aws_security_group");
		awsImageId = awsProps.getProperty("aws_image_id");
		
		ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKey,awsSecretKey));
		ec2.setEndpoint(awsServiceUrl);
		
		logger.log(Level.INFO,"Initialization completed.");
	}
	
	/**
	 * Runs a VM instance on an EC2 compatible cloud.
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
		boolean done = false;
		while (!done) {
			try {
				RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
				instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
				done = true;
			} catch (Exception e) {
				logger.log(Level.INFO,"Error while sending RunInstanceRequest, retrying in 1s..." );
				e.printStackTrace(System.out);
				Thread.sleep(1000);
			}
		}
		
		logger.log(Level.INFO,"Sent RunInstanceRequest successfully..");
				
		//Waits for the instance to be "running".
		String instanceIp = ""; //Will be changed eventually.
		done = false;
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
				logger.log(Level.INFO,"Failed to get instance description, retrying..");
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
