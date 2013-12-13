import re

def remove_html_tags(data):
    p = re.compile(r'<.*?>')
    return p.sub('', data)

def remove_extra_spaces(data):
    p = re.compile(r'\s+')
    return p.sub(' ', data)


input_file = open ("cacm_query.txt", "r").read().split()
output_file = open ("process_query.txt", "a")
#print input_file
print len(input_file)
i = 0
query = []
while i <  len(input_file):
    if input_file[i] == "<DOC>":
        if not query:
            query = []
            i += 1
            continue
        else:
            print query
            output_file.write (" ".join(query))
            output_file.write ("\n")
            query = []
        i += 1
        continue
    elif input_file[i] == "<DOCNO>":
        i += 1
        continue
    elif input_file[i] == "</DOC>":
        i += 1
        continue
    elif input_file[i] == "</DOCNO>":
        i += 1
        continue
    else:
        #print "enter"
        query.append(str(input_file[i].strip()))
    i += 1
    
output_file.write (" ".join(query))
print "completed"
output_file.close()
