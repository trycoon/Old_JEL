/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.facade;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.ServiceAdapter;

/**
 *
 * @author trycoon
 */
public class JelStreamingAdapter extends ServiceAdapter
{
    // http://www.scribd.com/doc/3836539/Server-Push-data-push-in-Flex-using-Blaze-DS-and-Java

    @Override
    public Object invoke(Message message) {
        AsyncMessage newMessage = (AsyncMessage)message;

        newMessage.setBody("Hej hej");

        MessageService messageService = (MessageService)getDestination().getService();
        messageService.pushMessageToClients(newMessage, false);

        return null;
    }

}
