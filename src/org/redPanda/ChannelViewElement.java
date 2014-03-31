/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import org.redPandaLib.core.Channel;

/**
 *
 * @author Tyrael
 */
public class ChannelViewElement extends Channel {

    Channel channel;

    public static ChannelViewElement getInstanceFromChannel(Channel channel) {
        ChannelViewElement channelViewElement = new ChannelViewElement();

        channelViewElement.id = channel.getId();
        channelViewElement.name = channel.getName();
        channelViewElement.key = channel.getKey();
        channelViewElement.channel = channel;
        channelViewElement.displayPriority = channel.displayPriority;

        return channelViewElement;
    }

    public int compareTo(ChannelViewElement o) {
        return (int) (displayPriority - o.displayPriority);
    }

    public void incDisplayPriority() {
        channel.displayPriority++;
        displayPriority++;
    }
}
