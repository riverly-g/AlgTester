package prj.rivery.test;

public class Performance {
	
	private long startTime = 0;
	private long beforeMem = 0;
	
	private long time = 0;
	private long mem = 0;
	
	public void start() {
		Runtime r = Runtime.getRuntime();
		this.startTime = System.nanoTime();
		this.beforeMem = r.totalMemory() - r.freeMemory();
	}
	
	public void end() {
		Runtime r = Runtime.getRuntime();
		long after = r.totalMemory() - r.freeMemory();
		long end = System.nanoTime();
		
		this.time = end - this.startTime;
		this.mem = after - this.beforeMem;
		
		this.startTime = 0;
		this.beforeMem = 0;
	}
	
	public long getTime() {
		return this.time;
	}
	
	public long getMemory() {
		return this.mem;
	}
}
