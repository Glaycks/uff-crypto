package monografia.desempenho;
import java.lang.management.*;

/*
 * http://nadeausoftware.com/articles/2008/03/java_tip_how_get_cpu_and_user_time_benchmarking#TimingamultithreadedtaskusingCPUsystemandusertime
 */

public final class ThreadTimes{
	
	public ThreadTimes() {
		// TODO Auto-generated constructor stub
		
	}
	
	/** Get CPU time in nanoseconds. */
	public long getCpuTime( long ids ) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if ( ! bean.isThreadCpuTimeSupported( ) )
			return 0L;
		long time = 0L;
		long t = bean.getThreadCpuTime( ids );
		if ( t != -1 )
			time += t;
		return time;
	}

	/** Get user time in nanoseconds. */
	public long getUserTime( long ids ) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if ( ! bean.isThreadCpuTimeSupported( ) )
			return 0L;
		long time = 0L;
		long t = bean.getThreadUserTime( ids );
		if ( t != -1 )
			time += t;
		return time;
	}

	/** Get system time in nanoseconds. */
	public long getSystemTime( long ids ) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if ( ! bean.isThreadCpuTimeSupported( ) )
			return 0L;
		long time = 0L;
		long tc = bean.getThreadCpuTime(  ids );
		long tu = bean.getThreadUserTime( ids );
		if ( tc != -1 && tu != -1 )
			time += (tc - tu);
		return time;
	}
}
