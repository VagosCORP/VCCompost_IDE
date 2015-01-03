package vccompost.ide;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;
import vclibs.communication.javafx.Comunic;

public class Rutina_COM /*extends Task<Integer>*/{
	
	Comunic comunic;
	Thread th;
	String tarea = "";
	Path pathRegistro;
	PrintWriter oStReg=null;
	String pathRoutines = "C://VCCompost//Rutina//";
	String pathrutinaA = pathRoutines + "RutinaA.vcrut";
	String pathrutinaB = pathRoutines + "RutinaB.vcrut";
	
	public TabletCOM tabletCOM;
	public interface TabletCOM {
		void DataProcesed(int reactor, String pathA, String pathB);
		void DataProcesedA(int reactor, int sector, String pathA);
		void DataProcesedB(int reactor, int sector, String pathB);
	};
	public void setTabletCOM(TabletCOM inst) {
		tabletCOM = inst;
	}
	
	public Rutina_COM() {
		initServer();
	}
	
	void initServer() {
		comunic = new Comunic(2002);
		comunic.setConnectionListener(new OnConnectionListener() {
			
			@Override
			public void onConnectionstablished() {
				
			}
			
			@Override
			public void onConnectionfinished() {
				initServer();
			}
		});
		comunic.setComunicationListener(new OnComunicationListener() {
			
			@Override
			public void onDataReceived(String rcv, int[] ndato) {
				tarea += rcv;
				if(rcv.endsWith("P")) {
					procesar(tarea);
					tarea = "";
					comunic.Detener_Actividad();
				}
			}
		});
		th = new Thread(comunic);
		th.setDaemon(true);
		th.start();
	}
	
	void procesar(String data) {
		pathRegistro=Paths.get("C://VCCompost//Rutina");
		if(Files.notExists(pathRegistro)){
            try {
                Files.createFile(pathRegistro);
            } catch (IOException ex) {
                
            }
        }
		String[] secciones = data.split("W");
		String[] pointsA = secciones[0].split("/");
		String[] pointsB = secciones[1].split("/");
		int lA = pointsA.length - 1;
		try {
	        Files.createFile(Paths.get(pathrutinaA));
	        Files.createFile(Paths.get(pathrutinaB));
		} catch (IOException e) {
	        System.out.println("No se Pudieron crear los archivos de Rutina");
		}
		try {
			oStReg=new PrintWriter(new FileWriter(pathrutinaA,false));
			for (int i = 0; i < lA; i++) {
				String[] pData = pointsA[i].split("=");
				float x = (Float.parseFloat(pData[2]));
				float y = (Float.parseFloat(pData[4]));
				float r = Float.parseFloat(pData[6]);
				if(i == 0) {
					oStReg.println("y=" + y);
				}else if(i == lA - 1){
					oStReg.println("y=" + y + ";");
				}else {
					oStReg.println("x=" + x + "=y=" + y + "=r=" + r);
				}
			}
			oStReg.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		int lB = pointsB.length - 1;
		try {
			oStReg=new PrintWriter(new FileWriter(pathrutinaB,false));
			for (int i = 0; i < lB; i++) {
				String[] pData = pointsB[i].split("=");
				float x = (Float.parseFloat(pData[2]));
				float y = (Float.parseFloat(pData[4]));
				float r = Float.parseFloat(pData[6]);
				if(i == 0) {
					oStReg.println("y=" + y);
				}else if(i == lB - 1){
					oStReg.println("y=" + y + ";");
				}else {
					oStReg.println("x=" + x + "=y=" + y + "=r=" + r);
				}
			}
			oStReg.close();
			if(tabletCOM != null) {
				tabletCOM.DataProcesedA(32, 1, pathrutinaA);
				tabletCOM.DataProcesedB(32, 2, pathrutinaB);
				tabletCOM.DataProcesed(32, pathrutinaA, pathrutinaB);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
