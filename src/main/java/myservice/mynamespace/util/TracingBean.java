package myservice.mynamespace.util;

import java.net.InetAddress;

public class TracingBean {

	private InetAddress ip;
	private String hostname;
	private String dateand_Time;
	private long time_Diffirence;
	private String request_Uri;
	private int response_Code;
	private int sr_no;
	private String request_Method;
	private String content_type_body;
	private String operation_name;
	private String class_name;

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getDateand_Time() {
		return dateand_Time;
	}

	public void setDateand_Time(String dateand_Time) {
		this.dateand_Time = dateand_Time;
	}

	public long getTime_Diffirence() {
		return time_Diffirence;
	}

	public void setTime_Diffirence(long time_Diffirence) {
		this.time_Diffirence = time_Diffirence;
	}

	public String getRequest_Uri() {
		return request_Uri;
	}

	public void setRequest_Uri(String request_Uri) {
		this.request_Uri = request_Uri;
	}

	public int getResponse_Code() {
		return response_Code;
	}

	public void setResponse_Code(int response_Code) {
		this.response_Code = response_Code;
	}

	public int getSr_no() {
		return sr_no;
	}

	public void setSr_no(int sr_no) {
		this.sr_no = sr_no;
	}

	public String getRequest_Method() {
		return request_Method;
	}

	public void setRequest_Method(String request_Method) {
		this.request_Method = request_Method;
	}

	public String getContent_type_body() {
		return content_type_body;
	}

	public void setContent_type_body(String content_type_body) {
		this.content_type_body = content_type_body;
	}

	public String getOperation_name() {
		return operation_name;
	}

	public void setOperation_name(String operation_name) {
		this.operation_name = operation_name;
	}

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

}
