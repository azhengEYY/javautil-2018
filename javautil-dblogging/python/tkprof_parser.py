import re
from mmap import mmap, PROT_READ

# Regular Expressions
sql_id_regex='SQL ID: ([^ ]*) Plan Hash: (\d*)'
stat_regex='(Parse|Execute|Fetch) *(\d*) *([\d|\.]*) *([\d|\.]*) *(\d*) *(\d*)  *(\d*) *(\d)'
misses_regex='Misses.*: (\d*)'
optimizer_mode_regex='Optimizer mode: (\w*)'
parsing_regex='Parsing.*?: (\w*) *\(recursive depth: (\d*)\)'
plan_hdr_regex="Rows (1st) Rows (avg) Rows (max)  Row Source Operation"
plan_row_regex="*(\d*) *(\d*) *(\d*) *([\w| ]*) *\(cr=(\d*) pr=(\d*) pw=(\d*) time=(\d*) us starts=(\d*) cost=(\d*) size=(\d*) card=(\d*)\)"
  
sql_id_matcher=re.compile(sql_id_regex)
stat_matcher=re.compile(stat_regex)
#misses_matcher='Misses.*: (\d*)'
#optimizer_mode_matcher='Optimizer mode: (\w*)'
#parsing_matcher='Parsing.*?: (\w*) *\(recursive depth: (\d*)\)'
#plan_hdr_matcher="Rows (1st) Rows (avg) Rows (max)  Row Source Operation"
#plan_row_matcher=*(\d*) *(\d*) *(\d*) *([\w| ]*) *\(cr=(\d*) pr=(\d*) pw=(\d*) time=(\d*) us starts=(\d*) cost=(\d*) size=(\d*) card=(\d*)\)

def sql_id_generator(file_name):
   with open(file_name, 'rb') as f, mmap(f.fileno(), 0, prot=PROT_READ) as m:
        for match in re.finditer((sql_id_regex).encode(), m):
            
            yield match

def parse(file_name):
    for match in sql_id_generator(file_name):
        print(match.group(0))
        print("at " + str(match.pos))
        print("at position: " + str(match.start()))
        print('id: %s hash: %s' % (match.group(1),match.group(2)))

def get_sql(fp):
    line = fp.readline()
    sql_lines = []
    end_of_sql = False;
    sql = ""
    while not end_of_sql:
        print('looking for call ' + line)
        if line.startswith("call"):
            end_of_sql = True
        else:
            sql_lines.append(line)
        line = fp.readline()
    trimmed_lines = sql_lines[:-1]
    for line in trimmed_lines:
        sql += (sql + "\n")

    return sql

def parse_by_line(file_name):
    line_number = 0
    with open(file_name) as fp:
        line = fp.readline()
        while line:
            print("examining line " + str(line_number) + line)
            line_number += 1
            id_match = sql_id_matcher.match(line)
            if (id_match is not None):
                sql = get_sql(fp)
            else:
                line = fp.readline()
if __name__ == '__main__':
    #parse('job_1.prf')
    parse_by_line('job_1.prf')


