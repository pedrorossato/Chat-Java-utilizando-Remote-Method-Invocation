package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import room.IRoomChat;
import room.RoomChat;

/** Classe que representa um servidor que gerencia o chat.
* @author Pedro Henrique Vestena Rossato
* @version 1.0
*/
/**
 * @author phvro
 *
 */
/**
 * @author phvro
 *
 */
public class ServerChat extends UnicastRemoteObject implements IServerChat{
	

	final static String hostURL = "rmi://localhost:2020/Servidor";
	private static final long serialVersionUID = 1L;
	private static ArrayList<String> roomList;
	private static JFrame homeFrame;
	private static JList<String> roomlistGUI;
	private static ServerChat serverChat;
	
	/** Método principal que cria um RMIRegistry e disponibiliza o objeto remoto servidor utilizando o Naming.bind.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(2020);
			serverChat = new ServerChat();
			Naming.bind(hostURL, serverChat);
			System.out.println("Servidor funcionando...");
			showHomeGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Construtor da classe.
	 * @throws RemoteException
	 */
	public ServerChat() throws RemoteException {
		super();
		roomList = new ArrayList<String>();
	}
	
	/** Método que retorna uma lista de salas existentes no servidor .
	 * @return ArrayList<String> - lista de salas existentes
	 */
	@Override
	public ArrayList<String> getRooms() throws RemoteException{
		return roomList;
	}
	
	/** Método que cria uma sala e deixa a mesma disponível remotamente através do Naming.bind .
	 * @param roomName String - nome da sala a ser criada
	 */
	@Override
	public void createRoom(String roomName) throws RemoteException {
		if(roomList.contains(roomName))
			System.out.println("A sala " + roomName + " já existe.");
		else {
			try {
				roomList.add(roomName);
				RoomChat roomChat = new RoomChat(roomName);
				Naming.bind(hostURL+"/"+roomName, roomChat);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Sala " + roomName.replace("+", " ") + " criada com sucesso!");			
		}
	}
	
	/**Método que atualiza as salas mostradas na lista de salas da GUI.
	 */
	private static void refreshGUIRooms() {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		ArrayList<String> rooms = roomList;
		rooms.forEach((room)->listModel.addElement(room.replace("+", " ")));
		roomlistGUI.setModel(listModel);
	}
	
	/**Método que fecha a sala, removendo da lista local de nomes de salas e invocando o método 
	 * remoto closeRoom da roomChat.
	 */
	private static void fecharSala() {
		String selectedRoom = roomlistGUI.getSelectedValue().replace(" ", "+");
		roomList.remove(selectedRoom.replace(" ", "+"));
		IRoomChat roomChat;
		try {
			roomChat = (IRoomChat) Naming.lookup("rmi://localhost:2020/Servidor/" + selectedRoom);
			roomChat.closeRoom();
			Naming.unbind(hostURL+"/"+selectedRoom);
			refreshGUIRooms();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	/**Método que desenha a GUI de menu do servidor.
	 */
	private static void showHomeGUI() {
		homeFrame = new JFrame("Servidor de chat");
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		panel.setLayout(new GridLayout(1, 2));
		
		roomlistGUI = new JList<String>();
		roomlistGUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomlistGUI.setLayoutOrientation(JList.VERTICAL);
		roomlistGUI.setVisibleRowCount(-1);
		
		JScrollPane listScroller = new JScrollPane(roomlistGUI);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JButton fecharSalaButton = new JButton("Fechar sala");
		fecharSalaButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				fecharSala();
			}
		});
		JButton atualizarSalasButton = new JButton("Atualizar lista");
		atualizarSalasButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshGUIRooms();
			}
		});
		panel.add(fecharSalaButton);
		panel.add(atualizarSalasButton);
		homeFrame.add(listScroller,BorderLayout.CENTER);
		homeFrame.add(panel,BorderLayout.PAGE_END);
		homeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		homeFrame.pack();
		homeFrame.setSize(400, 500);
		homeFrame.setVisible(true);
	}
	
}
