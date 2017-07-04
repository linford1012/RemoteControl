package server.yf.com.remotecontrolserver_as.service.impl;


import server.yf.com.remotecontrolserver_as.CommonConstant;
import server.yf.com.remotecontrolserver_as.LanMina.LanMinaCmdManager;
import server.yf.com.remotecontrolserver_as.dao.TcpAnalyzerImpl;
import server.yf.com.remotecontrolserver_as.dao.tcpip.TCPIPServer;
import server.yf.com.remotecontrolserver_as.minamachines.MinaCmdManager;
import server.yf.com.remotecontrolserver_as.service.ZyglqBusinessService;
import server.yf.com.remotecontrolserver_as.ui.serice.MouseService;
import server.yf.com.remotecontrolserver_as.util.JsonAssistant;

public class ZyglqBusinessServiceImpl implements ZyglqBusinessService {
	private JsonAssistant jsonAssistant;
	public static final String CMD="cmd";
	@Override
	public void sendZyglq(String zyglqJson) {
		if(CommonConstant.LINE_TYPE==1){//局域网
			LanMinaCmdManager.getInstance().sendControlCmd(zyglqJson);
		}else{//互联网
			MinaCmdManager.getInstance()
					.sendControlCmd(zyglqJson);
		}
//		TCPIPServer.getInstans(MouseService.gateway, MouseService.equipment, TcpAnalyzerImpl.getInstans()).send(zyglqJson.getBytes());
	}
}
