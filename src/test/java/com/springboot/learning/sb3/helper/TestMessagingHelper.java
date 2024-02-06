package com.springboot.learning.sb3.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;

import java.util.ArrayList;
import java.util.List;

public class TestMessagingHelper {

    private static final Logger log = LoggerFactory.getLogger(TestMessagingHelper.class);

    /**
     * @param target : the target
     * @param bindingName : the binding name
     */
    public static void purgeMessages(OutputDestination target, String bindingName) {
        getMessages(target, bindingName);
    }

    /**
     * @param target : the target
     * @param bindingName : the binding name
     * @return list of {@link String}
     */
    public static List<String> getMessages(OutputDestination target, String bindingName) {

        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(target, bindingName);

            if (message == null) {
                anyMoreMessages = false;

            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    /**
     * @param target : the target
     * @param bindingName : the binding name
     * @return {@link Message}
     */
    public static Message<byte[]> getMessage(OutputDestination target, String bindingName) {

        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            // If the messageQueues member variable in the target object contains no queues when the receive method is called, it will cause a NPE to be thrown.
            // So we catch the NPE here and return null to indicate that no messages were found.
            log.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }
}
