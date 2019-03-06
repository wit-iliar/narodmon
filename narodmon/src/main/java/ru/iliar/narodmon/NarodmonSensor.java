package ru.iliar.narodmon;

public class NarodmonSensor {
	
	/** Метрика датчика */
	private String mac;
	
	/** Значение датчика */
	private double value;
	
	/** Формат показаний датчика */
	private String valueFormat;
	
	/**
	 * 
	 * @param mac - метрика датчика
	 */
	public NarodmonSensor(String mac) {
		this.mac = mac;		
		this.valueFormat = "%.2f";
	}
	
	/**
	 * Установка нового значения
	 * @param value значение датчика
	 */
	public void setValue(double value) {
		this.value = value;	
	}
	
	/**
	 * Установка формата значения датчика
	 * @param valueFormat формат значения
	 */
	public void setValueFormat(String valueFormat) {
		this.valueFormat = valueFormat;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("#").append(mac).append("#").append(String.format(valueFormat, value)).append("\n");
		return( result.toString() );
	}

}
