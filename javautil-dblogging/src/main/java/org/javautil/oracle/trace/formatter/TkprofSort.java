package org.javautil.oracle.trace.formatter;

public enum TkprofSort {
    prscnt, // number of times parse was called
    prscpu, // cpu time parsing
    prsela, // elapsed time parsing
    prsdsk, // number of disk reads during parse
    prsqry, // number of buffers for consistent read during parse
    prscu, // number of buffers for current read during parse
    prsmis, // number of misses in library cache during parse
    execnt, // number of execute was called
    execpu, // cpu time spent executing
    exeela, // elapsed time executing
    exedsk, // number of disk reads during execute
    exeqry, // number of buffers for consistent read during execute
    execu, // number of buffers for current read during execute
    exerow, // number of rows processed during execute
    exemis, // number of library cache misses during execute
    fchcnt, // number of times fetch was called
    fchcpu, // cpu time spent fetching
    fchela, // elapsed time fetching
    fchdsk, // number of disk reads during fetch
    fchqry, // number of buffers for consistent read during fetch
    fchcu, // number of buffers for current read during fetch
    fchrow, // number of rows fetched
    userid // userid of user that parsed the cursor
}
