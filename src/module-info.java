module chatRMI {
	requires java.rmi;
	requires java.desktop;
	exports server;
	exports room;
	exports user;
}