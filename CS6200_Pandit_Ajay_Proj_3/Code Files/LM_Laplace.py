#!/usr/bin/env python

import string
import urllib, re
from math import log
import math
from porterStemmer import PorterStemmer
from collections import defaultdict,OrderedDict

#########################################################################

def get_doclen(docid):
    print "Hello"
    file3 = open("file3.txt", "r").readlines()
    for line in file3:
        line = line.split()
        if str (docid) == line[0]:
            return line[2]

#-----------------

def dsum(d_temp,d):
    for key,value in d_temp.iteritems():    
        if d.has_key(key):
            d[key] = d[key] + d_temp[key]
        else:
            d[key] = d_temp[key]

    return d

#-----------------

def word_dict_len_update(word_dict_len_temp,word_dict_len):
    for key,value in word_dict_len_temp.iteritems():
        if word_dict_len.has_key(key):
            continue
        else:
            word_dict_len[key] = word_dict_len_temp[key]
    return word_dict_len

#-------------------------
        
def calculate_lm_laplace(d,word_dict_len,num_docs,num_unique_terms):
    input_file = open("raw_inveted_index.txt", "r")

    while 1:
        line = input_file.readline()
        if not line:
            break
        else:
            line = line.split()
            ctf, df = float(line[0]), float(line[1])    ##take the ctf and df and convert to float
            inverted_list = map(lambda i: (int(line[2 + 3*i]),
                                   float(line[3 + 3*i]),
                                   float(line[4 + 3*i]))
                        ,range(0, (len(line) - 2)/3))

            d_temp = {}
            word_dict_len_temp = {}
            idf = math.log ((num_docs/df),2)
            for (docid,doclen,tf) in inverted_list:
                laplace_score = math.log(float((tf+1)/(doclen + num_unique_terms)),2)
                d_temp[docid] = laplace_score

            for (docid, doclen,tf) in inverted_list:
                word_dict_len_temp[docid] = doclen 

            d = dsum(d_temp,d)
            word_dict_len = word_dict_len_update(word_dict_len_temp,word_dict_len)
            return d, word_dict_len
    input_file.close()

#-------------------------

def update_leftover_values(get_dict,word_dict_len,query,num_unique_terms):
    counter = 1
    query_word_count = len(query)
    while counter < query_word_count:
        ctf = 0
        for line in file1:
            line = line.split()
            if query[counter] == line[0]:
                termid = line[1]
                start_offset = int(line[2])
                finish_offset = int(line[3])
                df = float(line[5])

                main_index = open("main_index.txt", "r")
                for i, line in enumerate(main_index):
                    if (i >= (start_offset) and (i <= finish_offset)):
                        line = line.strip()
                        line = line.split()
                        ctf += int(line[1])
                main_index.close()

                output_file = open("raw_inveted_index.txt", "w")
                output_file.write(str(ctf))
                output_file.write(" ")
                output_file.write(str(df))
                output_file.write(" ")

                main_index = open("main_index.txt", "r")
                for i, line in enumerate(main_index):
                    if (i >= (start_offset-1) and (i <= finish_offset)):
                        line = line.strip()
                        line = line.split()
                        docid = int(line[0])
                        tf = int(line[1])
                        doclen = int(line[4])
                        output_file.write(str(docid))
                        output_file.write(" ")
                        output_file.write(str(doclen))
                        output_file.write(" ")
                        output_file.write(str(tf))
                        output_file.write(" ")
                output_file.close()

        input_file = open("raw_inveted_index.txt", "r")
        while 1:
            line = input_file.readline()
            if not line:
                break
            else:
                line = line.split()
                ctf, df = float(line[0]), float(line[1])    ##take the ctf and df and convert to float
                inverted_list = map(lambda i: (int(line[2 + 3*i]),
                                       float(line[3 + 3*i]),
                                       float(line[4 + 3*i]))
                            ,range(0, (len(line) - 2)/3))
            
                d_temp = {}
                for (docid,doclen,tf) in inverted_list:
                    d_temp[docid] = doclen

                for key,value in get_dict.iteritems():
                    if d_temp.has_key(key):
                        continue
                    else:
                        get_dict[key] = get_dict[key] + math.log ((1/float(num_unique_terms + word_dict_len[key])),2)

        counter += 1
    return get_dict

#-------------------------

def print_dict(query_num,get_dict,fout,int_ext_map_dict):
    rank = 1
    for key,value in sorted(get_dict.iteritems(),key=lambda (k,v): (v,k), reverse=True):
        key = int_ext_map_dict[str(key)]
        if rank > 1000:
            break
        else:
            eachline = []
            eachline.append (query_num.strip("."))
            eachline.append (str(Q0))
            eachline.append (str(key))
            eachline.append (str (rank))
            eachline.append (str("%f" %  value))
            eachline.append (str(Exp))
            eachline.append ("\n")
        rank += 1
        fout.write(" ".join(eachline))
    #fout.close()

#-------------------------

def int_ext_mapping(file3):
    temp = {}
    for line in file3:
        int_docid, ext_docid, doclen = line.split(" ")
        temp[int_docid] = ext_docid

    return temp
        
########################################################################

# Main Function

int_ext_map = open("file3.txt","r")
int_ext_map_dict = int_ext_mapping(int_ext_map)
int_ext_map.close()

num_docs = 3204
num_unique_terms = 11157
num_terms = 110476
avg_doclen = 46.25
avg_querylen = 14
lam = 0.2

Q0 = 0
Exp = 1

fout = open ("sample_lm_laplace_out.txt", "w")

query_file = open ("process_query.txt", "r")
while 1:
    line = query_file.readline().lower()
    #print line.lower()
    if not line:
        break

    query_hashmap = {}
    query_hashmap[1] = line.split(" ")
    #print line # printing each query before stemming and stopping
    stop_words = open ("stop_words1.txt", "r").read().split()

    for key in query_hashmap.keys():
        temp = query_hashmap[key]
        temp_string = ""
        for words in temp:
            temp_string += " " + words

        punc_type_1 = [",","-","=","/","\\","'",";","^","+","|",":","<",">","`","&","(",")"]
        punc_type_2 = [".",'"',"[","]","?","!","*","%","{","}","$"]

        for punc in punc_type_1:
            if punc in temp_string:
                temp_string = temp_string.replace (punc, " ")
        for punc in punc_type_2:
            if punc in temp_string:
                temp_string = temp_string.replace (punc, "")

        temp_string = temp_string.split()
        final_word_list = [x for x in temp_string if x not in stop_words]
        p = PorterStemmer()
        mid_list = [(p.stem(word, 0, len (word)-1)) for word in final_word_list]
        new_list = [x for x in mid_list if x not in stop_words]
        final_string = ''.join(" " + x for x in new_list)
        query_hashmap[key] = final_string.strip()
        #print query_hashmap[key] # printing each query after stopping and stemming
    
    model_dict = {}
    query_word_count = defaultdict(float)
    file1 = open ("file1.txt","r").readlines()
    
    for key in query_hashmap.keys():
        query = query_hashmap[key].split()
        print query
        query = map(str.lower,query)
        query_dict = {}
        help_dict = {}

        len_query = len(query)
        query_word_counter = 1
        get_dict = {}
        word_dict_len = {}
        while query_word_counter < len_query:
            #print query[query_word_counter]
            querylen = (len(query) - 1)
            qf = query.count(query[query_word_counter])
            ctf = 0
            for line in file1:
                line = line.split()
                if query[query_word_counter] == line[0]:
                    termid = line[1]
                    start_offset = int(line[2])
                    finish_offset = int(line[3])
                    df = float(line[5])
                    #print termid, start_offset, finish_offset, df
            
                    # calculating the ctf for a term
                    main_index = open("main_index.txt", "r")
                    for i, line in enumerate(main_index):
                        if (i >= (start_offset) and (i <= finish_offset)):
                            line = line.strip()
                            line = line.split()
                            ctf += int(line[1])
                    main_index.close()

                    output_file = open("raw_inveted_index.txt", "w")
                    output_file.write(str(ctf))
                    output_file.write(" ")
                    output_file.write(str(df))
                    output_file.write(" ")

                    main_index = open("main_index.txt", "r")
                    for i, line in enumerate(main_index):
                        if (i >= (start_offset-1) and (i <= finish_offset)):
                            line = line.strip()
                            line = line.split()
                            docid = int(line[0])
                            tf = int(line[1])
                            doclen = int(line[4])
                            output_file.write(str(docid))
                            output_file.write(" ")
                            output_file.write(str(doclen))
                            output_file.write(" ")
                            output_file.write(str(tf))
                            output_file.write(" ")
                    output_file.close()

            #print ctf, df, docid, tf, doclen
            get_dict,word_dict_len = calculate_lm_laplace(get_dict,word_dict_len,num_docs,num_unique_terms)        
            query_word_counter += 1
        get_dict = update_leftover_values(get_dict,word_dict_len,query,num_unique_terms)
        print_dict(query[0],get_dict,fout,int_ext_map_dict)
    
fout.close()
print "completed"

