package ru.iliar.narodmon;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarodmonSender implements Runnable {

	/** Интерфейс логера */
	private static final Logger logger = LoggerFactory.getLogger(NarodmonSender.class);
	
	/** Адрес сервера по умолчанию */
	private static final String SERVER_ADDRESS = "narodmon.ru";
	
	/** Порт сервера по умолчанию */
	private static final int SERVER_PORT = 8283;
	
	/** Интервал передачи по умолчанию (значение) */
	private static final long DELAY_VALUE = 6;
	
	/** Интервал передачи по умолчанию (единица измерения) */
	private static final TimeUnit DELAY_UNIT = TimeUnit.MINUTES;
	
	/** Используемая кодировка */
	private static final String CHARSET = "UTF-8";
	
	/** Адрес сервера */
	private String serverAddress;
	
	/** Порт сервера */
	private int serverPort;
	
	/** MAC адрес устройства */
	private String mac;
	
	/** Имя устройства */
	private String name;
	
	/** Интервал передачи (значение) */
	private long delayValue;
	
	/** Интервал передачи (единица измерения) */
	private TimeUnit delayUnit;
	
	/** Список сенсоров */
	private ArrayList<NarodmonSensor> sensorsList;
	
	/** Обработчик задач */
	private ScheduledExecutorService scheduledExecutorService;

	private class NarodmonThreadFactory implements ThreadFactory {
		
		/** MAC адрес устройства */
		private String mac;
		
		/** Номер потока */
		private int threadNum;
		
		public NarodmonThreadFactory(String mac) {
			this.mac = mac;
			this.threadNum = 0;
		}
		
		public Thread newThread(Runnable runnable) {
			threadNum++;
			Thread thread = new Thread(runnable);
			thread.setName("NarodmonSender-" + mac + "-Thread-" + threadNum);
			
			return(thread);
		}
	}

	/**
	 * @param mac адрес устройства
	 */
	public NarodmonSender(String mac) {
		this.mac = mac;	
		
		this.serverAddress = SERVER_ADDRESS;
		this.serverPort = SERVER_PORT;
		this.name = null;
		this.delayValue = DELAY_VALUE;
		this.delayUnit = DELAY_UNIT;
		
		sensorsList =  new ArrayList<NarodmonSensor>();
		
		ThreadFactory threadFactory = new NarodmonThreadFactory(mac);
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);

	}
	
	/** 
	 * Добавить сенсор в лист для отправки.
	 * @param sensor
	 */
	public void addSensor(NarodmonSensor sensor) {
		sensorsList.add(sensor);
	}
	
	/**
	 * Настроить адрес сервера для передачи. Позволяет изменить адрес сервера с целью тестирования. 
	 * @param address
	 * @param port
	 */
	public void setServer(String address, int port) {
		serverAddress = address;
		serverPort = port;
	}

	/** 
	 * Настроить интервал передачи. 
	 * @param value
	 * @param unit
	 */
	public void setSendInterval(long value, TimeUnit unit) {
		delayValue = value;
		delayUnit = unit;
	}
	
	/**
	 * Отправка данных на сервер
	 */
	public void run() {
		StringBuilder data = new StringBuilder();
		data.append("#").append(mac);
		if (name != null) {
			data.append("#").append(name);
		}
		data.append("\n");
		
		for (NarodmonSensor sensor : sensorsList) {
			data.append( sensor.toString() );
		}
		data.append("##");
		
		try (Socket socket = new Socket(serverAddress, serverPort)) {
			socket.getOutputStream().write( data.toString().getBytes(CHARSET) );
			socket.getOutputStream().flush();
			
			BufferedInputStream bufferedInputStream = new BufferedInputStream( socket.getInputStream() ); 
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			
			int readedByte;
			while ((readedByte = bufferedInputStream.read()) != -1) {
				byteArrayOutputStream.write((byte) readedByte);
			}
			String response = byteArrayOutputStream.toString(CHARSET);
				
			logger.info("Данные отправлены. Ответ сервера: " + response);
		} catch (Exception e) {
			logger.error("Ошибка отправки: ", e);
		}
	}

	/**
	 * Запус сервиса по отправке данных
	 */
	public void start() {
		scheduledExecutorService.scheduleWithFixedDelay(this, 0, delayValue, delayUnit);
	}
	
	/**
	 * Отложенный запуск сервиса по отправке данных. Используется, если на момент старта данные от сенсоров еще не готовы. Величина задержки равна стандартному интервалу передачи.
	 */
	public void delayedStart() {
		scheduledExecutorService.scheduleWithFixedDelay(this, delayValue, delayValue, delayUnit);
	}
	
	/**
	 * Отложенный запуск сервиса по отправке данных. Используется, если на момент старта данные от сенсоров еще не готовы.
	 * @param initialDelay величина задержки в единицах интервала передачи. 
	 */
	public void delayedStart(long initialDelay) {
		scheduledExecutorService.scheduleWithFixedDelay(this, initialDelay, delayValue, delayUnit);
	}
	
	/** 
	 * Остановка сервиса по отправке данных
	 */
	public void shutdown() {
		scheduledExecutorService.shutdown();
	}
}
