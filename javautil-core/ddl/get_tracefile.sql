    function get_tracefile(p_file_name in varchar) 
    return clob is 
        my_ident varchar2(4000 CHAR);
        my_tmp varchar2(4000 CHAR);
        my_clob CLOB;
        my_bfile        bfile;
        my_dest_offset  INTEGER := 1;
        my_src_offset   INTEGER := 1;
        my_lang_context INTEGER := dbms_lob.default_lang_ctx;
        my_warning      INTEGER;
        my_file_name varcahr2(4000);
    BEGIN
        -- set param
    SELECT
        my_bfile := bfilename('UDUMP_DIR', p_file_name);
    
        -- convert
        dbms_lob.CreateTemporary(my_clob, FALSE, dbms_lob.CALL);
        dbms_lob.FileOpen(my_bfile);
    
        dbms_lob.LoadClobFromFile
        (
            dest_lob     => my_clob,
            src_bfile    => my_bfile,
            amount       => dbms_lob.lobmaxsize,
            dest_offset  => my_dest_offset,
            src_offset   => my_src_offset,
            bfile_csid   => dbms_lob.default_csid,
            lang_context => my_lang_context,
            warning      => my_warning
        );
    
        dbms_lob.FileClose(my_bfile);
     
        dbms_output.put_line(my_clob);
       
        return my_clob;
    END get_tracefile;
