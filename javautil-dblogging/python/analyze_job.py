
#!/usr/bin/python3
from mmap import mmap, PROT_READ
import re
import sys
from subprocess import call
import tempfile
import tkprof_configuration
import os
import numpy
import cx_Oracle
#
# TODO get action time
TMP_DIR="./tmp"

def asArray(list):
    return numpy.array(list)


def run_tkprof(infile_name, outfile_name, show_sys = False):
    args = list()
    args.append(tkprof_configuration.tkprof_executable)


    args.append(infile_name)
    args.append(outfile_name)
    if not show_sys:
        args.append("sys=no")
    return_code = call(args)
    #return_code = call([tkprof_configuration.tkprof_executable,infile_name,outfile_name])
    print("return code from tkprof " + str(return_code))
    print("run_tkprof created: " + outfile_name)
    print("created tkprof %s " % os.path.abspath(outfile_name))
    show_file_info(outfile_name)

def run_trcsess(infile_name,outfile_name,action_name):
    call([tkprof_configuration.trcsess_executable,
          "output="+outfile_name,
          "action=" + action_name,
          infile_name])

def fix_file_name(in_file):
    return in_file.replace(" ","~")

def show_file_info(file_name):
    print("show_file_info %s" % file_name)
    mode = os.stat(file_name)
    print("size of %s is %d " % (file_name, mode.st_size))

def get_trace_file_name_for_action(input_trace_file_name,action_name):
    concatted =  input_trace_file_name + "_" + action_name + ".trc"
    retval = fix_file_name(concatted)
    print("trace_file: '%s' action: '%s' action_trace: %s'" % 
        (input_trace_file_name, action_name, retval))
    return retval

def get_prf_file_name_for_action(input_trace_file_name,action_name):
    concatted =  input_trace_file_name + "_" + action_name + ".prf"
    retval = fix_file_name(concatted)
    print("trace_file: '%s' action: '%s' action_profile: %s'" % 
        (input_trace_file_name, action_name, retval))
    return retval


def run_tkprof_for_action_autoname(infile_name,action_name):
    action_trace_file_name = get_trace_file_name_for_action(infile_name,action_name)
    run_trcsess(infile_name,action_trace_file_name,action_name)
    action_tkprof_file_name = get_prf_file_name_for_action(infile_name,action_name)
    run_tkprof(action_trace_file_name,action_tkprof_file_name)
    print("created tkprof file '%s' for action %s" % (action_tkprof_file_name, action_name))
    show_file_info(action_tkprof_file_name)

#def run_tkprof_for_action(infile_name,outfile_name,action_name):
    #temporary_named = get_temporary_file_name()
    #run_trcsess(infile_name,temporary_named,action_name)
    #show_file_info(temporary_named)
    #outfile_name = "wtf"
    #run_tkprof(temporary_named,outfile_name)
    #print("created file '%s' for action %s" % (outfile_name, action_name))
    #show_file_info(outfile_name)
    
def action_line_generator(file_name):
    with open(file_name, 'rb') as f, mmap(f.fileno(), 0, prot=PROT_READ) as m:
        regex = ".*ACTION NAME:.*"
        for match in re.finditer((regex).encode(), m):
            string = match.group(0)
            yield string.decode('utf-8')

def get_action_lines(file_name):
    lines = list()
    for action_line in action_line_generator(file_name):
        lines.append(action_line)
    return lines

def action_generator(file_name):
   with open(file_name, 'rb') as f, mmap(f.fileno(), 0, prot=PROT_READ) as m:
        for match in re.finditer(('ACTION NAME:').encode(), m):
            string = match.group(0)
            yield string

def action_generator_2(file_name):
    with open(file_name, 'rb') as text_file:
        content = text_file.readlines()
    print('action_generator_2 read %s: ' % file_name)
    with open(file_name, 'rb') as f, mmap(f.fileno(), 0, prot=PROT_READ) as m:
        # with open(file_name, 'rb') as f:
        #      with mmap(f.fileno(), 0, prot=PROT_READ) as m:
        regex = 'ACTION NAME:\((.*)\).*'
        for match in re.finditer(regex.encode(), m):
            string = match.group(0)
            action_name = match.group(1)
            #print("action_name: %s" % action_name)
            yield action_name.decode('utf-8')



def get_actions(file_name):
    actions = set()
    for action in action_generator_2(file_name):
       # print("action: %s" % action)
        actions.add(action)
    return actions

def create_prof_per_action(file_name):
    print("processing file %s" % file_name)
    actions = get_actions(file_name)
    print("actions: %s" % actions)
    for action_name in actions:
        run_tkprof_for_action_autoname(file_name, action_name)


def show_trace_info(file_name):
    print("show_trace_info: processing %s" % file_name)
    # print("actions: %s" % get_actions(file_name)   )
    # print("action lines: %s" % get_action_lines(file_name))
    # run_tkprof(file_name,file_name + ".prf")
    # run_trcsess(file_name,file_name + ".trcsess.trc","action 1")
    # action_name = "action 1"
    # outfile_name = file_name + "." + action_name + ".prf"
    # print("outfile_name for action is: %s" % outfile_name)
    # run_tkprof_for_action(file_name,outfile_name,action_name)
    # get_temporary_file_name()
    create_prof_per_action(file_name)

def connect():
    # example sa/sales@localhost:1521:sales_reporting_pdg
    ORA_URL=os.environ['ORA_URL']
    if ORA_URL is None;
       print("environment variable 'ORA_URL' must be set and exported");
       raise Exception("environment variable 'ORA_URL' must be set and exported)

def get_trace_file_name(job_nbr):
    

if __name__ == '__main__':
    connection = 
    #file_name = sys.argv[1]
    #show_trace_info(file_name)
    #show_trace_info("dev12c_ora_16747.trc")
    show_trace_info("test_trace/dev12c_ora_32172.trc")
    #actions = get_actions("dev12c_ora_16747.trc")
