package room;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import user.IUserChat;

/** Classe que representa uma sala de chat.
* @author Pedro Henrique Vestena Rossato
* @version 1.0
*/
public class RoomChat extends UnicastRemoteObject implements IRoomChat{
	
	private static final long serialVersionUID = 1L;
	private Map<String, IUserChat> userList;
	private String roomName;
	
	public RoomChat(String roomName) throws RemoteException {
		this.roomName = roomName;
		this.userList = new HashMap<String, IUserChat>();
	}
	
	
	 /**M�todo que envia mensagem para todos os usu�rios da sala usando o m�todo remoto deliverMsg dos usu�rios.
	 * @param usrName String - Nome do usu�rio que envia a mensagem.
	 * @param msg String - Mensagem a ser enviada.
	 * @return void
	 */
	@Override
	public void sendMsg(String usrName, String msg) throws RemoteException {
		userList.values().forEach((user)->{
			try {
				user.deliverMsg(usrName, msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}
	 /**M�todo que permite um usu�rio entrar em uma sala de chat.
	 * @param usrName String - Nome do usu�rio.
	 * @param user IUserChat - Interface remota do usu�rio.
	 * @return void
	 */
	@Override
	public void joinRoom(String usrName, IUserChat user) throws RemoteException {
		if(userList.containsKey(usrName))
			System.out.println("O usu�rio " + usrName + " j� se encontra nesta sala.");
		else {
			userList.put(usrName, user);
			System.out.println("O usu�rio " + usrName + " entrou com sucesso na sala " + roomName);			
		}
	}
	 /**M�todo que permite um usu�rio deixar a sala de chat.
	 * @param usrName String - nome do usu�rio.
	 * @return void
	 */
	@Override
	public void leaveRoom(String usrName) throws RemoteException {
		if(userList.containsKey(usrName)) {
			userList.remove(usrName);
			System.out.println("O usu�rio " + usrName + " saiu da sala.");
		}
	}
	 /**M�todo para fechar uma sala de chat (a��o feita pelo servidor).
	  * @return void
	 */
	@Override
	public void closeRoom() throws RemoteException {
		userList.values().forEach((user)->{
			try {
				user.deliverMsg("SERVIDOR", "Sala fechada pelo servidor.");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		
	}
	 /**M�todo que retorna o nome da sala
	 * @return String - Nome da sala
	 */
	@Override
	public String getRoomName() throws RemoteException {
		return roomName;
	}

}
