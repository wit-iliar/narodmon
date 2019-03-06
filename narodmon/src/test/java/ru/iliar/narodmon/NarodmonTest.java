package ru.iliar.narodmon;

public class NarodmonTest {
	
	public static void main(String[] args) {
		NarodmonSensor sensor1 = new NarodmonSensor("T1");
		sensor1.setValue(12.3);

		NarodmonSensor sensor2 = new NarodmonSensor("T2");
		sensor2.setValue(23.4);
		
		NarodmonSensor sensor3 = new NarodmonSensor("T3");
		sensor3.setValue(34.56);
		
		NarodmonSender narodmonSender = new NarodmonSender("00-00-00-00-00-00");
		
		narodmonSender.setServer("localhost", 8283);
		
		narodmonSender.addSensor(sensor1);
		narodmonSender.addSensor(sensor2);
		narodmonSender.addSensor(sensor3);
		narodmonSender.start();
		
	}
}
