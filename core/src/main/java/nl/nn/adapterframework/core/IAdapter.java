/*
   Copyright 2013, 2016 Nationale-Nederlanden, 2020, 2022 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.core;

import nl.nn.adapterframework.configuration.Configuration;
import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.jmx.JmxAttribute;
import nl.nn.adapterframework.receivers.Receiver;
import nl.nn.adapterframework.statistics.HasStatistics;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.util.MessageKeeper;

/**
 * The Adapter is the central manager in the framework. It has knowledge of both
 * <code>IReceiver</code>s as well as the <code>PipeLine</code> and statistics.
 * The Adapter is the class that is responsible for configuring, initializing and
 * accessing/activating IReceivers, Pipelines, statistics etc.
 *
 **/
public interface IAdapter extends IManagable, HasStatistics {

	/**
	 * Instruct the adapter to configure itself. The adapter will call the pipeline
	 * to configure itself, the pipeline will call the individual pipes to configure
	 * themselves.
	 * 
	 * @see nl.nn.adapterframework.pipes.AbstractPipe#configure()
	 * @see PipeLine#configure()
	 */
	@Override
	void configure() throws ConfigurationException;

	/**
	 * The messagekeeper is used to keep the last x messages, relevant to display in
	 * the web-functions.
	 */
	public MessageKeeper getMessageKeeper();
	public Receiver<?> getReceiverByName(String receiverName);
	public Iterable<Receiver<?>> getReceivers();
	public PipeLineResult processMessage(String messageId, Message message, PipeLineSession pipeLineSession);
	public PipeLineResult processMessageWithExceptions(String messageId, Message message, PipeLineSession pipeLineSession) throws ListenerException;

	public void setPipeLine (PipeLine pipeline) throws ConfigurationException;
	public PipeLine getPipeLine();
	public void setConfiguration(Configuration configuration);
	public Configuration getConfiguration();
	public boolean isAutoStart();

	public Message formatErrorMessage(String errorMessage, Throwable t, Message originalMessage, String messageID, INamedObject objectInError, long receivedTime);

	@JmxAttribute(description = "Return the Adapter description")
	public String getDescription();
}
