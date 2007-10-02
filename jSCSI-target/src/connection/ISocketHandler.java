package connection;

import java.net.Socket;

/**
 * 
 * @author Marcus Specht
 *
 */
public interface ISocketHandler {
	
	/**
	 * Assign the Socket to another entity.
	 * @param socket
	 * @return true if the entity can handle the Socket, false else
	 */
	public boolean assignSocket(Socket socket);
		
}
