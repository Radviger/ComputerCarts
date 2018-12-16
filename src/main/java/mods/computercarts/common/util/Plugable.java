package mods.computercarts.common.util;

import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;

public interface Plugable {
    void onPlugMessage(Plug plug, Message message);

    void onPlugConnect(Plug plug, Node node);

    void onPlugDisconnect(Plug plug, Node node);
}
