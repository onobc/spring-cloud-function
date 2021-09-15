/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.grpc;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 *
 * @author Oleg Zhurakousky
 * @since 3.2
 *
 */
public final class GrpcUtils {

	private GrpcUtils() {

	}

	public static GrpcMessage toGrpcMessage(byte[] payload, Map<String, String> headers) {
		return GrpcMessage.newBuilder()
				.setPayload(ByteString.copyFrom(payload))
				.putAllHeaders(headers)
				.build();
	}

	public static GrpcMessage toGrpcMessage(Message<byte[]> message) {
		Map<String, String> stringHeaders = new HashMap<>();
		message.getHeaders().forEach((k, v) -> {
			stringHeaders.put(k, v.toString());
		});
		return toGrpcMessage(message.getPayload(), stringHeaders);
	}

	public static Message<byte[]> fromGrpcMessage(GrpcMessage message) {
		return MessageBuilder.withPayload(message.getPayload().toByteArray())
				.copyHeaders(message.getHeadersMap())
				.build();
	}

	public static Message<byte[]> requestReply(Message<byte[]> inputMessage) {
		return requestReply("localhost", FunctionGrpcProperties.GRPC_PORT, inputMessage);
	}

	public static Message<byte[]> requestReply(String host, int port, Message<byte[]> inputMessage) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
				.usePlaintext().build();
		MessagingServiceGrpc.MessagingServiceBlockingStub stub = MessagingServiceGrpc
				.newBlockingStub(channel);

		GrpcMessage response = stub.requestReply(toGrpcMessage(inputMessage));
		channel.shutdown();
		return fromGrpcMessage(response);
	}
}