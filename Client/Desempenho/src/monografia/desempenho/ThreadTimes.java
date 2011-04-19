package monografia.desempenho;
import java.lang.management.*;

/*
 * http://nadeausoftware.com/articles/2008/03/java_tip_how_get_cpu_and_user_time_benchmarking#TimingamultithreadedtaskusingCPUsystemandusertime
 */

public class ThreadTimes{
	
	public ThreadTimes(){
		
	}
		 
	/** Get CPU time in nanoseconds. */
	public long getCpuTime( long ids ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    if ( ! bean.isThreadCpuTimeSupported( ) )
	        return 0L;
	    
	    long t = bean.getThreadCpuTime( ids );
	    if ( t != -1 ){
	    	return t;
	    } else{
	    	return 0L;
	    }
	}
	 
	/** Get user time in nanoseconds. */
	public long getUserTime(long ids ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    if ( ! bean.isThreadCpuTimeSupported( ) )
	        return 0L;
	    long t = bean.getThreadCpuTime( ids );
	    if ( t != -1 ){
	    	return t;
	    } else{
	    	return 0L;
	    }
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