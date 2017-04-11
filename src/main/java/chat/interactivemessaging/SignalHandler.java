package chat.interactivemessaging;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalHandler extends WebSocketServer {

    private static Logger logger = LoggerFactory.getLogger(SignalHandler.class);
//    private RoomManager roomManager = new RoomManager();

    public SignalHandler(int port) {
        super(new InetSocketAddress(port));
    }

    public SignalHandler(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        
        logger.info("onOpen: {}", handshake.getResourceDescriptor());
     
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("onClose: {}", conn.hashCode());
     //   roomManager.removeSocket(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("onMessage: {}", message);
    //    roomManager.eventDispatch(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
      //  logger.info("WebSocketServer onError: {} client {}", roomManager.getSocketClientMap().get(conn).getId(), ex);
    }

}
