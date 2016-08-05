package zzl.bestidear.wifidirect.miracast;

public class SinkPlayer {

	private String mhost;
	private int mport;

	public SinkPlayer() {
		super();
	}

	public void setHostAndPort(String host, int port) {
		mhost = host;
		mport = port;
	}

	public void startRtsp() {
		setRtspSink(mhost, mport);
	}

	public void stopRtsp() {
		stopRtspSink();
	}

	private native void setRtspSink(String host, int port);

	private native void stopRtspSink();

	static {
		System.loadLibrary("WifiDirect_Miracast");
	}

}
